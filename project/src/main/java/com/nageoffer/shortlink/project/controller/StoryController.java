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

package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.StoryCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryDeleteReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryLikeReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryPageRespDTO;
import com.nageoffer.shortlink.project.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 故事控制层
 */
@RestController
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    /**
     * 创建故事
     */
    @PostMapping("/api/short-link/v1/story")
    public Result<Void> createStory(@RequestBody StoryCreateReqDTO requestParam) {
        storyService.createStory(requestParam);
        return Results.success();
    }

    /**
     * 分页查询故事
     */
    @GetMapping("/api/short-link/v1/story/page")
    public Result<IPage<StoryPageRespDTO>> pageStory(StoryPageReqDTO requestParam) {
        return Results.success(storyService.pageStory(requestParam));
    }

    /**
     * 清空当前用户故事
     */
    @PostMapping("/api/short-link/v1/story/clear")
    public Result<Void> clearMyStory() {
        storyService.clearMyStory();
        return Results.success();
    }

    /**
     * 删除单条故事
     */
    @PostMapping("/api/short-link/v1/story/delete")
    public Result<Void> deleteStory(@RequestBody StoryDeleteReqDTO requestParam) {
        storyService.deleteStory(requestParam.getId());
        return Results.success();
    }


    /**
     * like story
     */
    @PostMapping("/api/short-link/v1/story/like")
    public Result<Integer> likeStory(@RequestBody StoryLikeReqDTO requestParam) {
        return Results.success(storyService.likeStory(requestParam.getId()));
    }
}