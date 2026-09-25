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

package com.nageoffer.shortlink.project.mq.producer;

import com.alibaba.fastjson2.JSON;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 短链接监控状态保存消息队列生产者
 * 
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${short-link.kafka.stats-topic:short-link.stats}")
    private String statsTopic;

    /**
     * 发送短链接统计消息
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        kafkaTemplate
                .send(statsTopic, statsRecord.getFullShortUrl(), JSON.toJSONString(statsRecord))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error(
                                "发送短链接统计消息失败, messageId: {}, fullShortUrl: {}",
                                statsRecord.getMessageId(),
                                statsRecord.getFullShortUrl(),
                                throwable);
                    }
                });
    }
}
