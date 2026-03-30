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

package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.dao.entity.StoryDO;
import com.nageoffer.shortlink.project.dao.entity.StoryLikeDO;
import com.nageoffer.shortlink.project.dao.mapper.StoryLikeMapper;
import com.nageoffer.shortlink.project.dao.mapper.StoryMapper;
import com.nageoffer.shortlink.project.dto.req.StoryCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryDeleteReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryLikeReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryLikeRespDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryPageRespDTO;
import com.nageoffer.shortlink.project.service.StoryService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 故事服务实现
 */
@Service
@RequiredArgsConstructor
public class StoryServiceImpl extends ServiceImpl<StoryMapper, StoryDO> implements StoryService {

    private final StoryLikeMapper storyLikeMapper;

    @Override
    public IPage<StoryPageRespDTO> pageStory(StoryPageReqDTO requestParam) {
        LambdaQueryWrapper<StoryDO> queryWrapper = Wrappers.lambdaQuery(StoryDO.class)
                .eq(StoryDO::getDelFlag, 0)
                .orderByDesc(StoryDO::getLikeCount)
                .orderByDesc(StoryDO::getCreateTime);
        IPage<StoryDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        List<StoryDO> records = resultPage.getRecords();
        Set<Long> likedStoryIds = Collections.emptySet();
        String username = UserContext.getUsername();
        if (StrUtil.isNotBlank(username) && records != null && !records.isEmpty()) {
            List<Long> storyIds = records.stream().map(StoryDO::getId).collect(Collectors.toList());
            List<StoryLikeDO> likes = storyLikeMapper.selectList(Wrappers.lambdaQuery(StoryLikeDO.class)
                    .eq(StoryLikeDO::getUsername, username)
                    .eq(StoryLikeDO::getDelFlag, 0)
                    .in(StoryLikeDO::getStoryId, storyIds));
            likedStoryIds = likes.stream().map(StoryLikeDO::getStoryId).collect(Collectors.toSet());
        }
        Set<Long> finalLikedStoryIds = likedStoryIds;
        return resultPage.convert(each -> {
            StoryPageRespDTO respDTO = BeanUtil.toBean(each, StoryPageRespDTO.class);
            respDTO.setLiked(finalLikedStoryIds.contains(each.getId()));
            return respDTO;
        });
    }

    @Override
    public void createStory(StoryCreateReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getTitle()) || StrUtil.isBlank(requestParam.getContent())) {
            throw new ClientException("标题和内容不能为空");
        }
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("用户未登录");
        }
        StoryDO storyDO = StoryDO.builder()
                .title(requestParam.getTitle())
                .content(requestParam.getContent())
                .username(username)
                .build();
        baseMapper.insert(storyDO);
    }

    @Override
    public void clearMyStory() {
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("用户未登录");
        }
        LambdaUpdateWrapper<StoryDO> updateWrapper = Wrappers.lambdaUpdate(StoryDO.class)
                .eq(StoryDO::getUsername, username)
                .eq(StoryDO::getDelFlag, 0);
        StoryDO storyDO = new StoryDO();
        storyDO.setDelFlag(1);
        baseMapper.update(storyDO, updateWrapper);
    }

    @Override
    public void deleteStory(StoryDeleteReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getId())) {
            throw new ClientException("故事ID不能为空");
        }
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("用户未登录");
        }
        LambdaUpdateWrapper<StoryDO> updateWrapper = Wrappers.lambdaUpdate(StoryDO.class)
                .eq(StoryDO::getId, Long.parseLong(requestParam.getId()))
                .eq(StoryDO::getDelFlag, 0);
        // 非管理员只能删除自己的故事
        if (!"admin".equals(username)) {
            updateWrapper.eq(StoryDO::getUsername, username);
        }
        StoryDO storyDO = new StoryDO();
        storyDO.setDelFlag(1);
        baseMapper.update(storyDO, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoryLikeRespDTO likeStory(StoryLikeReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getId())) {
            throw new ClientException("故事ID不能为空");
        }
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("用户未登录");
        }
        Long storyId;
        try {
            storyId = Long.parseLong(requestParam.getId());
        } catch (NumberFormatException ex) {
            throw new ClientException("故事ID不合法");
        }
        StoryDO storyDO = baseMapper.selectOne(Wrappers.lambdaQuery(StoryDO.class)
                .eq(StoryDO::getId, storyId)
                .eq(StoryDO::getDelFlag, 0));
        if (storyDO == null) {
            throw new ClientException("故事不存在");
        }
        StoryLikeDO existing = storyLikeMapper.selectOne(Wrappers.lambdaQuery(StoryLikeDO.class)
                .eq(StoryLikeDO::getStoryId, storyId)
                .eq(StoryLikeDO::getUsername, username));
        boolean liked;
        if (existing == null) {
            StoryLikeDO likeDO = StoryLikeDO.builder()
                    .storyId(storyId)
                    .username(username)
                    .build();
            storyLikeMapper.insert(likeDO);
            LambdaUpdateWrapper<StoryDO> updateWrapper = Wrappers.lambdaUpdate(StoryDO.class)
                    .eq(StoryDO::getId, storyId)
                    .eq(StoryDO::getDelFlag, 0)
                    .setSql("like_count = like_count + 1");
            baseMapper.update(null, updateWrapper);
            liked = true;
        } else if (existing.getDelFlag() != null && existing.getDelFlag() == 0) {
            StoryLikeDO updateLike = new StoryLikeDO();
            updateLike.setDelFlag(1);
            storyLikeMapper.update(updateLike, Wrappers.lambdaUpdate(StoryLikeDO.class)
                    .eq(StoryLikeDO::getId, existing.getId()));
            LambdaUpdateWrapper<StoryDO> updateWrapper = Wrappers.lambdaUpdate(StoryDO.class)
                    .eq(StoryDO::getId, storyId)
                    .eq(StoryDO::getDelFlag, 0)
                    .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
            baseMapper.update(null, updateWrapper);
            liked = false;
        } else {
            StoryLikeDO updateLike = new StoryLikeDO();
            updateLike.setDelFlag(0);
            storyLikeMapper.update(updateLike, Wrappers.lambdaUpdate(StoryLikeDO.class)
                    .eq(StoryLikeDO::getId, existing.getId()));
            LambdaUpdateWrapper<StoryDO> updateWrapper = Wrappers.lambdaUpdate(StoryDO.class)
                    .eq(StoryDO::getId, storyId)
                    .eq(StoryDO::getDelFlag, 0)
                    .setSql("like_count = like_count + 1");
            baseMapper.update(null, updateWrapper);
            liked = true;
        }
        StoryDO refreshed = baseMapper.selectOne(Wrappers.lambdaQuery(StoryDO.class)
                .eq(StoryDO::getId, storyId)
                .eq(StoryDO::getDelFlag, 0));
        StoryLikeRespDTO respDTO = new StoryLikeRespDTO();
        respDTO.setLiked(liked);
        respDTO.setLikeCount(refreshed == null ? 0 : refreshed.getLikeCount());
        return respDTO;
    }
}
