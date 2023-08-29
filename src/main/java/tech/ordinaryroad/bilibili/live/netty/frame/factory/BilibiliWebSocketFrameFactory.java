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

package tech.ordinaryroad.bilibili.live.netty.frame.factory;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.databind.JsonNode;
import tech.ordinaryroad.bilibili.live.api.BilibiliApis;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.msg.AuthMsg;
import tech.ordinaryroad.bilibili.live.msg.HeartbeatMsg;
import tech.ordinaryroad.bilibili.live.netty.frame.AuthWebSocketFrame;
import tech.ordinaryroad.bilibili.live.netty.frame.HeartbeatWebSocketFrame;
import tech.ordinaryroad.bilibili.live.util.BilibiliCodecUtil;
import tech.ordinaryroad.bilibili.live.util.OrLiveChatCookieUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mjz
 * @date 2023/1/5
 */
public class BilibiliWebSocketFrameFactory {

    private static final ConcurrentHashMap<Long, BilibiliWebSocketFrameFactory> CACHE = new ConcurrentHashMap<>();

    /**
     * 浏览器地址中的房间id，支持短id
     */
    private final long roomId;
    /**
     * 浏览器cookie，仅用来维持登录状态
     */
    private String cookie;
    private final ProtoverEnum protover;

    private volatile static HeartbeatMsg heartbeatMsg;

    public BilibiliWebSocketFrameFactory(long roomId, ProtoverEnum protover, String cookie) {
        this.roomId = roomId;
        this.protover = protover;
        this.cookie = cookie;
    }

    public synchronized static BilibiliWebSocketFrameFactory getInstance(long roomId, ProtoverEnum protover, String cookie) {
        return CACHE.computeIfAbsent(roomId, aLong -> new BilibiliWebSocketFrameFactory(roomId, protover, cookie));
    }

    public synchronized static BilibiliWebSocketFrameFactory getInstance(long roomId, ProtoverEnum protover) {
        return getInstance(roomId, protover, null);
    }

    /**
     * 创建认证包
     *
     * @return AuthWebSocketFrame
     */
    public AuthWebSocketFrame createAuth() {
        try {
            Map<String, String> cookieMap = OrLiveChatCookieUtil.parseCookieString(cookie);
            String buvid3 = OrLiveChatCookieUtil.getCookieByName(cookieMap, "buvid3", () -> UUID.randomUUID().toString());
            String uid = OrLiveChatCookieUtil.getCookieByName(cookieMap, "DedeUserID", () -> "0");
            JsonNode data = BilibiliApis.roomInit(roomId, cookie);
            JsonNode danmuInfo = BilibiliApis.getDanmuInfo(roomId, 0, cookie);
            int realRoomId = data.get("room_id").asInt();
            AuthMsg authMsg = new AuthMsg(realRoomId, this.protover.getCode(), buvid3, danmuInfo.get("token").asText());
            authMsg.setUid(NumberUtil.parseLong(uid));
            return new AuthWebSocketFrame(BilibiliCodecUtil.encode(authMsg));
        } catch (Exception e) {
            throw new RuntimeException("认证包创建失败，请检查房间号是否正确。roomId: %d, msg: %s".formatted(roomId, e.getMessage()));
        }
    }

    public HeartbeatWebSocketFrame createHeartbeat() {
        return new HeartbeatWebSocketFrame(BilibiliCodecUtil.encode(this.getHeartbeatMsg()));
    }

    /**
     * 心跳包单例模式
     *
     * @return HeartbeatWebSocketFrame
     */
    public HeartbeatMsg getHeartbeatMsg() {
        if (heartbeatMsg == null) {
            synchronized (BilibiliWebSocketFrameFactory.this) {
                if (heartbeatMsg == null) {
                    heartbeatMsg = new HeartbeatMsg(this.protover.getCode());
                }
            }
        }
        return heartbeatMsg;
    }

}
