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

package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.StoryCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryPageRespDTO;

/**
 * 故事服务
 */
public interface StoryService {

    /**
     * 创建故事
     */
    void createStory(StoryCreateReqDTO requestParam);

    /**
     * 分页查询故事
     */
    IPage<StoryPageRespDTO> pageStory(StoryPageReqDTO requestParam);

    /**
     * 清空当前用户故事
     */
    void clearMyStory();

    /**
     * 删除当前用户指定故事
     */
    void deleteStory(Long id);


    /**
     * like story
     */
    Integer likeStory(Long id);
}
