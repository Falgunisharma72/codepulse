package com.codepulse.queue;

import com.codepulse.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisJobConsumer {

    private final StringRedisTemplate redisTemplate;
    private final AnalysisService analysisService;

    @Value("${codepulse.queue.channel:analysis-jobs}")
    private String queueChannel;

    @Scheduled(fixedDelay = 1000)
    public void consumeJobs() {
        try {
            String jobId = redisTemplate.opsForList().rightPop(queueChannel, Duration.ofSeconds(1));
            if (jobId != null) {
                Long analysisRunId = Long.parseLong(jobId);
                log.info("Consumed analysis job for run: {}", analysisRunId);
                analysisService.analyze(analysisRunId);
            }
        } catch (Exception e) {
            log.error("Error consuming analysis job: {}", e.getMessage(), e);
        }
    }
}
