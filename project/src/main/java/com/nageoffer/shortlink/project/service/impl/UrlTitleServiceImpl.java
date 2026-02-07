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

import com.nageoffer.shortlink.project.service.UrlTitleService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * URL 标题接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    private static final String DEFAULT_TITLE = "未获取到标题";

    @Override
    public String getTitleByUrl(String url) {
        if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return DEFAULT_TITLE;
        }
        try {
            Connection connection = Jsoup.connect(url)
                    .timeout(3000)
                    .userAgent("Mozilla/5.0 (ShortLinkBot)")
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true);
            Connection.Response response = connection.execute();
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                Document document = response.parse();
                String title = document.title();
                return title == null || title.isBlank() ? DEFAULT_TITLE : title;
            }
        } catch (Exception ignored) {
            return DEFAULT_TITLE;
        }
        return DEFAULT_TITLE;
    }
}
