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

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.handler.CustomBlockHandler;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接控制层

 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String locFlag = httpRequest.getParameter("loc");
        String lat = httpRequest.getParameter("lat");
        String lng = httpRequest.getParameter("lng");
        String skip = httpRequest.getParameter("skip");
        if (StrUtil.isBlank(locFlag) && StrUtil.isBlank(lat) && StrUtil.isBlank(lng) && StrUtil.isBlank(skip)) {
            renderGeoPage(httpResponse);
            return;
        }
        shortLinkService.restoreUrl(shortUri, request, response);
    }

    private void renderGeoPage(HttpServletResponse response) {
        response.setStatus(200);
        response.setContentType("text/html;charset=UTF-8");
        String html = """
                <!doctype html>
                <html lang="zh-CN">
                  <head>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <link rel="icon" href="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI2NCIgaGVpZ2h0PSI2NCIgdmlld0JveD0iMCAwIDY0IDY0Ij4KICA8cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9Im5vbmUiLz4KICA8cGF0aCBkPSJNMTAgNDBjNi0xNCAyMC0yNCAzNi0yNiAyIDAgNCAyIDMgNC0yIDYtMyAxMi0zIDE4SDEweiIgZmlsbD0iI2Q2NDU0MSIvPgogIDxwYXRoIGQ9Ik0xMCA0MGgzNmMwIDYtNSAxMC0xMiAxMEgyMmMtNyAwLTEyLTQtMTItMTB6IiBmaWxsPSIjZjNmNWY3Ii8+CiAgPGNpcmNsZSBjeD0iNDYiIGN5PSIxOCIgcj0iNiIgZmlsbD0iI2YzZjVmNyIvPgo8L3N2Zz4=" />
                    <title>正在跳转</title>
                    <style>
                      body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif; background: #f5f7fb; color: #1f2a44; }
                      .shell { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; }
                      .card { max-width: 420px; width: 100%; background: #fff; border-radius: 16px; box-shadow: 0 18px 40px rgba(15, 30, 60, 0.12); padding: 28px; text-align: center; }
                      h1 { font-size: 18px; margin: 0 0 12px; }
                      p { font-size: 13px; color: #5b6b86; margin: 0 0 18px; line-height: 1.6; }
                      .actions { display: flex; gap: 10px; justify-content: center; }
                      button { border: none; border-radius: 10px; padding: 10px 16px; font-size: 14px; cursor: pointer; }
                      .primary { background: #2f7bff; color: #fff; }
                      .ghost { background: #eef2f8; color: #2a3b57; }
                      .status { margin-top: 12px; font-size: 12px; color: #8aa0b6; }
                    </style>
                  </head>
                  <body>
                    <div class="shell">
                      <div class="card">
                        <h1>即将跳转到目标页面</h1>
                        <p>为了提高访问统计的准确性，可选择允许定位（可跳过）。</p>
                        <div class="actions">
                          <button class="primary" id="allowBtn">允许定位</button>
                          <button class="ghost" id="skipBtn">跳过</button>
                        </div>
                        <div class="status" id="status">等待操作（2秒后自动跳转）...</div>
                      </div>
                    </div>
                    <script>
                      (function () {
                        const statusEl = document.getElementById('status');
                        const allowBtn = document.getElementById('allowBtn');
                        const skipBtn = document.getElementById('skipBtn');
                        const AUTO_SKIP_MS = 2000;
                        const GEO_TIMEOUT_MS = 2000;
                        let hasUserAction = false;
                        let geoResolved = false;
                        let geoPendingTimer = null;

                        function clearGeoTimer() {
                          if (geoPendingTimer) {
                            window.clearTimeout(geoPendingTimer);
                            geoPendingTimer = null;
                          }
                        }

                        function redirect(params) {
                          const url = new URL(window.location.href);
                          url.searchParams.set('loc', '1');
                          Object.keys(params || {}).forEach((key) => {
                            url.searchParams.set(key, params[key]);
                          });
                          window.location.replace(url.toString());
                        }

                        function onSuccess(pos) {
                          geoResolved = true;
                          clearGeoTimer();
                          const lat = pos.coords.latitude.toFixed(6);
                          const lng = pos.coords.longitude.toFixed(6);
                          statusEl.textContent = '定位成功，正在跳转...';
                          redirect({ lat, lng });
                        }

                        function skipAndRedirect() {
                          statusEl.textContent = '已跳过定位，正在跳转...';
                          redirect({ skip: '1' });
                        }

                        function onFail() {
                          geoResolved = true;
                          clearGeoTimer();
                          statusEl.textContent = '定位未成功，请重试或点击跳过';
                        }

                        function requestGeo() {
                          if (!navigator.geolocation) {
                            geoResolved = true;
                            clearGeoTimer();
                            statusEl.textContent = '当前浏览器不支持定位，请点击跳过';
                            return;
                          }
                          statusEl.textContent = '请求定位中...';
                          navigator.geolocation.getCurrentPosition(onSuccess, onFail, {
                            maximumAge: 60000,
                            enableHighAccuracy: true
                          });
                        }

                        const autoSkipTimer = window.setTimeout(() => {
                          if (hasUserAction) {
                            return;
                          }
                          hasUserAction = true;
                          skipAndRedirect();
                        }, AUTO_SKIP_MS);

                        allowBtn.addEventListener('click', () => {
                          hasUserAction = true;
                          window.clearTimeout(autoSkipTimer);
                          geoResolved = false;
                          clearGeoTimer();
                          geoPendingTimer = window.setTimeout(() => {
                            if (geoResolved) {
                              return;
                            }
                            geoResolved = true;
                            statusEl.textContent = '定位超时，正在跳转...';
                            skipAndRedirect();
                          }, GEO_TIMEOUT_MS);
                          requestGeo();
                        });
                        skipBtn.addEventListener('click', () => {
                          hasUserAction = true;
                          window.clearTimeout(autoSkipTimer);
                          clearGeoTimer();
                          skipAndRedirect();
                        });
                      })();
                    </script>
                  </body>
                </html>
                """;
        try {
            response.getWriter().write(html);
        } catch (Exception ignored) {
        }
    }

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 通过分布式锁创建短链接
     */
    @PostMapping("/api/short-link/v1/create/by-lock")
    public Result<ShortLinkCreateRespDTO> createShortLinkByLock(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLinkByLock(requestParam));
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
