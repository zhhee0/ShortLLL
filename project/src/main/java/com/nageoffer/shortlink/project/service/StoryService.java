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
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.StoryDO;
import com.nageoffer.shortlink.project.dto.req.StoryCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryDeleteReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryLikeReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryLikeRespDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryPageRespDTO;

/**
 * 故事服务接口
 */
public interface StoryService extends IService<StoryDO> {

    /**
     * 分页查询故事
     *
     * @param requestParam 分页查询参数
     * @return 故事分页结果
     */
    IPage<StoryPageRespDTO> pageStory(StoryPageReqDTO requestParam);

    /**
     * 创建故事
     *
     * @param requestParam 创建故事参数
     */
    void createStory(StoryCreateReqDTO requestParam);

    /**
     * 清空当前用户的故事
     */
    void clearMyStory();

    /**
     * 删除故事（管理员）
     *
     * @param requestParam 删除故事参数
     */
    void deleteStory(StoryDeleteReqDTO requestParam);

    /**
     * 点赞故事
     *
     * @param requestParam 点赞故事参数
     */
    StoryLikeRespDTO likeStory(StoryLikeReqDTO requestParam);
}
