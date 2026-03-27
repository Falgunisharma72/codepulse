package com.codepulse.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisJobPublisher {

    private final StringRedisTemplate redisTemplate;

    @Value("${codepulse.queue.channel:analysis-jobs}")
    private String queueChannel;

    public void publishJob(Long analysisRunId) {
        redisTemplate.opsForList().leftPush(queueChannel, analysisRunId.toString());
        log.info("Published analysis job for run: {}", analysisRunId);
    }
}
