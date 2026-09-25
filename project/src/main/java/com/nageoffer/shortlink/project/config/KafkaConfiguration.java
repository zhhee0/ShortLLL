/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nageoffer.shortlink.project.config;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka 消息队列配置
 *
 */
@Configuration
public class KafkaConfiguration {

    public static final String DEFAULT_STATS_TOPIC = "short-link.stats";
    public static final String DEFAULT_STATS_DLT_TOPIC = "short-link.stats.DLT";

    @Bean
    public NewTopics shortLinkStatsTopics(
            @Value("${short-link.kafka.stats-topic:" + DEFAULT_STATS_TOPIC + "}") String statsTopic,
            @Value("${short-link.kafka.stats-dlt-topic:" + DEFAULT_STATS_DLT_TOPIC + "}") String statsDltTopic,
            @Value("${short-link.kafka.partitions:3}") int partitions,
            @Value("${short-link.kafka.replicas:1}") int replicas) {
        return new NewTopics(
                TopicBuilder.name(statsTopic).partitions(partitions).replicas(replicas).build(),
                TopicBuilder.name(statsDltTopic).partitions(partitions).replicas(replicas).build());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${short-link.kafka.stats-dlt-topic:" + DEFAULT_STATS_DLT_TOPIC + "}") String statsDltTopic,
            @Value("${short-link.kafka.retry.interval:1000}") long retryInterval,
            @Value("${short-link.kafka.retry.max-attempts:3}") long maxAttempts) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(statsDltTopic, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(retryInterval, Math.max(0, maxAttempts - 1)));
        errorHandler.addNotRetryableExceptions(DeserializationException.class, SerializationException.class);
        return errorHandler;
    }
}
