package com.codepulse.controller;

import com.codepulse.exception.GlobalExceptionHandler;
import com.codepulse.exception.RepositoryNotFoundException;
import com.codepulse.model.dto.RepositoryDTO;
import com.codepulse.queue.AnalysisJobPublisher;
import com.codepulse.repository.AnalysisRunRepository;
import com.codepulse.repository.RepoRepository;
import com.codepulse.service.RepositoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RepositoryControllerTest {

    @Mock
    private RepositoryService repositoryService;
    @Mock
    private RepoRepository repoRepository;
    @Mock
    private AnalysisRunRepository analysisRunRepository;
    @Mock
    private AnalysisJobPublisher jobPublisher;

    @InjectMocks
    private RepositoryController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerRepository_valid_returns201() throws Exception {
        RepositoryDTO dto = RepositoryDTO.builder()
                .githubRepoUrl("https://github.com/test/repo")
                .repoName("repo")
                .build();

        RepositoryDTO created = RepositoryDTO.builder()
                .id(1L)
                .githubRepoUrl("https://github.com/test/repo")
                .repoName("repo")
                .build();

        when(repositoryService.registerRepository(any())).thenReturn(created);

        mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void registerRepository_duplicate_returns400() throws Exception {
        RepositoryDTO dto = RepositoryDTO.builder()
                .githubRepoUrl("https://github.com/test/repo")
                .repoName("repo")
                .build();

        when(repositoryService.registerRepository(any()))
                .thenThrow(new IllegalArgumentException("Already registered"));

        mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRepositories_returnsList() throws Exception {
        when(repositoryService.getAllRepositories()).thenReturn(List.of(
                RepositoryDTO.builder().id(1L).repoName("repo1").build(),
                RepositoryDTO.builder().id(2L).repoName("repo2").build()
        ));

        mockMvc.perform(get("/api/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getRepository_notFound_returns404() throws Exception {
        when(repositoryService.getRepository(99L))
                .thenThrow(new RepositoryNotFoundException(99L));

        mockMvc.perform(get("/api/repositories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRepository_returns204() throws Exception {
        doNothing().when(repositoryService).deleteRepository(1L);

        mockMvc.perform(delete("/api/repositories/1"))
                .andExpect(status().isNoContent());
    }
}
