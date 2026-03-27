package com.codepulse.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayloadDTO {

    private String ref;

    @JsonProperty("after")
    private String headCommit;

    private RepositoryInfo repository;

    private List<CommitInfo> commits;

    private PusherInfo pusher;

    @Getter @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryInfo {
        private Long id;
        @JsonProperty("full_name")
        private String fullName;
        @JsonProperty("clone_url")
        private String cloneUrl;
        @JsonProperty("html_url")
        private String htmlUrl;
    }

    @Getter @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String id;
        private String message;
        private AuthorInfo author;
    }

    @Getter @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorInfo {
        private String name;
        private String email;
    }

    @Getter @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PusherInfo {
        private String name;
    }

    public String getBranch() {
        if (ref != null && ref.startsWith("refs/heads/")) {
            return ref.substring("refs/heads/".length());
        }
        return ref;
    }

    public String getLatestCommitMessage() {
        if (commits != null && !commits.isEmpty()) {
            return commits.get(commits.size() - 1).getMessage();
        }
        return null;
    }
}
