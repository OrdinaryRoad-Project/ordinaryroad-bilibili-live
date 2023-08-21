/*
 * MIT License
 *
 * Copyright (c) 2023 OrdinaryRoad
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.ordinaryroad.bilibili.live.config;

import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.*;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;

/**
 * B站直播间弹幕客户端配置
 *
 * @author mjz
 * @date 2023/8/21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BilibiliLiveChatClientConfig {

    public static final long DEFAULT_HEARTBEAT_INITIAL_DELAY = 15;
    public static final long DEFAULT_HEARTBEAT_PERIOD = 25;

    /**
     * 浏览器中的Cookie
     *
     * @see tech.ordinaryroad.bilibili.live.netty.frame.factory.BilibiliWebSocketFrameFactory
     */
    private String cookie;

    /**
     * 直播间id，支持短id
     */
    private long roomId;

    /**
     * @see ProtoverEnum
     */
    @Builder.Default
    private ProtoverEnum protover = ProtoverEnum.NORMAL_ZLIB;

    /**
     * 是否启用自动重连
     */
    @Builder.Default
    private boolean autoReconnect = Boolean.TRUE;

    /**
     * 重试延迟时间（秒），默认5s后重试
     */
    @Builder.Default
    private int reconnectDelay = 5;

    /**
     * 聚合器允许的最大消息体长度，默认 64*1024 byte
     *
     * @see HttpObjectAggregator#HttpObjectAggregator(int)
     */
    @Builder.Default
    private int aggregatorMaxContentLength = 64 * 1024;

    /**
     * 首次发送心跳包的延迟时间（秒）
     */
    @Builder.Default
    private long heartbeatInitialDelay = DEFAULT_HEARTBEAT_INITIAL_DELAY;

    /**
     * 心跳包发送周期（秒）
     */
    @Builder.Default
    private long HeartbeatPeriod = DEFAULT_HEARTBEAT_PERIOD;
}
