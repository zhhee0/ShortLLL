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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.biz.user.UserContext;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.StoryDO;
import com.nageoffer.shortlink.project.dao.entity.StoryLikeDO;
import com.nageoffer.shortlink.project.dao.mapper.StoryMapper;
import com.nageoffer.shortlink.project.dao.mapper.StoryLikeMapper;
import com.nageoffer.shortlink.project.dto.req.StoryCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.StoryPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.StoryPageRespDTO;
import com.nageoffer.shortlink.project.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 故事服务实现
 */
@Service
@RequiredArgsConstructor
public class StoryServiceImpl extends ServiceImpl<StoryMapper, StoryDO> implements StoryService {

    private static final int TITLE_MAX_LENGTH = 32;
    private static final int CONTENT_MAX_LENGTH = 1200;
    private static final int PAGE_MAX_SIZE = 200;

    private final StoryLikeMapper storyLikeMapper;

    @Override
    public void createStory(StoryCreateReqDTO requestParam) {
        String title = Optional.ofNullable(requestParam).map(StoryCreateReqDTO::getTitle).orElse("");
        String content = Optional.ofNullable(requestParam).map(StoryCreateReqDTO::getContent).orElse("");
        if (StrUtil.isBlank(title) || StrUtil.isBlank(content)) {
            throw new ClientException("标题或内容不能为空");
        }
        title = title.trim();
        content = content.trim();
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new ClientException("标题长度不能超过" + TITLE_MAX_LENGTH + "字符");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new ClientException("内容长度不能超过" + CONTENT_MAX_LENGTH + "字符");
        }
        String username = Optional.ofNullable(UserContext.getUsername()).orElseThrow(() -> new ServiceException("用户未登录"));
        StoryDO storyDO = StoryDO.builder()
                .title(title)
                .content(content)
                .username(username)
                .likeCount(0)
                .build();
        baseMapper.insert(storyDO);
    }

    @Override
    public IPage<StoryPageRespDTO> pageStory(StoryPageReqDTO requestParam) {
        if (requestParam == null) {
            requestParam = new StoryPageReqDTO();
        }
        if (requestParam.getSize() <= 0 || requestParam.getSize() > PAGE_MAX_SIZE) {
            requestParam.setSize(PAGE_MAX_SIZE);
        }
        if (requestParam.getCurrent() <= 0) {
            requestParam.setCurrent(1L);
        }
        QueryWrapper<StoryDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0).orderByDesc("like_count").orderByDesc("create_time").orderByDesc("id");
        IPage<StoryDO> pageResult = baseMapper.selectPage(requestParam, queryWrapper);
        IPage<StoryPageRespDTO> pageResp = pageResult.convert(each -> BeanUtil.toBean(each, StoryPageRespDTO.class));
        if (pageResp.getRecords() != null) {
            pageResp.getRecords().forEach(item -> item.setLiked(false));
        }
        String username = UserContext.getUsername();
        if (StrUtil.isNotBlank(username) && pageResp.getRecords() != null && !pageResp.getRecords().isEmpty()) {
            List<Long> storyIds = pageResp.getRecords().stream()
                    .map(StoryPageRespDTO::getId)
                    .collect(Collectors.toList());
            if (!storyIds.isEmpty()) {
                QueryWrapper<StoryLikeDO> likeWrapper = new QueryWrapper<>();
                likeWrapper.eq("username", username).in("story_id", storyIds);
                Set<Long> likedStoryIds = storyLikeMapper.selectList(likeWrapper).stream()
                        .map(StoryLikeDO::getStoryId)
                        .collect(Collectors.toSet());
                pageResp.getRecords().forEach(item -> item.setLiked(likedStoryIds.contains(item.getId())));
            }
        }
        return pageResp;
    }

    @Override
    public void clearMyStory() {
        String username = Optional.ofNullable(UserContext.getUsername()).orElseThrow(() -> new ServiceException("用户未登录"));
        StoryDO updateDO = new StoryDO();
        updateDO.setDelFlag(1);
        UpdateWrapper<StoryDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username).eq("del_flag", 0);
        baseMapper.update(updateDO, updateWrapper);
    }

    @Override
    public void deleteStory(Long id) {
        if (id == null) {
            throw new ClientException("故事ID不能为空");
        }
        String username = Optional.ofNullable(UserContext.getUsername()).orElseThrow(() -> new ServiceException("用户未登录"));
        if (!"admin".equals(username)) {
            throw new ClientException("无权限删除故事");
        }
        StoryDO updateDO = new StoryDO();
        updateDO.setDelFlag(1);
        UpdateWrapper<StoryDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("del_flag", 0);
        int updated = baseMapper.update(updateDO, updateWrapper);
        if (updated == 0) {
            throw new ClientException("故事不存在或已删除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer likeStory(Long id) {
        if (id == null) {
            throw new ClientException("??ID????");
        }
        String username = Optional.ofNullable(UserContext.getUsername())
                .orElseThrow(() -> new ServiceException("?????"));
        QueryWrapper<StoryDO> storyWrapper = new QueryWrapper<>();
        storyWrapper.eq("id", id).eq("del_flag", 0);
        StoryDO storyDO = baseMapper.selectOne(storyWrapper);
        if (storyDO == null) {
            throw new ClientException("?????????");
        }
        QueryWrapper<StoryLikeDO> likeWrapper = new QueryWrapper<>();
        likeWrapper.eq("story_id", id).eq("username", username);
        StoryLikeDO existed = storyLikeMapper.selectOne(likeWrapper);
        if (existed == null) {
            StoryLikeDO likeDO = StoryLikeDO.builder()
                    .storyId(id)
                    .username(username)
                    .build();
            storyLikeMapper.insert(likeDO);
            UpdateWrapper<StoryDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).eq("del_flag", 0)
                    .setSql("like_count = like_count + 1");
            baseMapper.update(null, updateWrapper);
        } else {
            storyLikeMapper.delete(likeWrapper);
            UpdateWrapper<StoryDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).eq("del_flag", 0)
                    .setSql("like_count = IF(like_count > 0, like_count - 1, 0)");
            baseMapper.update(null, updateWrapper);
        }
        StoryDO latest = baseMapper.selectOne(storyWrapper);
        return latest == null || latest.getLikeCount() == null ? 0 : latest.getLikeCount();
    }

}
