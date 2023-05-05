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

import com.fasterxml.jackson.databind.JsonNode;
import tech.ordinaryroad.bilibili.live.api.BilibiliApis;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.msg.AuthMsg;
import tech.ordinaryroad.bilibili.live.msg.HeartbeatMsg;
import tech.ordinaryroad.bilibili.live.netty.frame.AuthWebSocketFrame;
import tech.ordinaryroad.bilibili.live.netty.frame.HeartbeatWebSocketFrame;
import tech.ordinaryroad.bilibili.live.util.BilibiliCodecUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mjz
 * @date 2023/1/5
 */
public class BilibiliWebSocketFrameFactory {

    private static final ConcurrentHashMap<ProtoverEnum, BilibiliWebSocketFrameFactory> CACHE = new ConcurrentHashMap<>();
    private final ProtoverEnum protover;

    public BilibiliWebSocketFrameFactory(ProtoverEnum protover) {
        this.protover = protover;
    }

    public synchronized static BilibiliWebSocketFrameFactory getInstance(ProtoverEnum protover) {
        return CACHE.computeIfAbsent(protover, BilibiliWebSocketFrameFactory::new);
    }

    /**
     * 创建认证包
     *
     * @param roomId 浏览器地址中的房间id，支持短id
     * @return AuthWebSocketFrame
     */
    public AuthWebSocketFrame createAuth(int roomId) {
        try {
            JsonNode data = BilibiliApis.roomInit(roomId);
            int realRoomId = data.get("room_id").asInt();
            AuthMsg authMsg = new AuthMsg(realRoomId, this.protover.getCode());
            return new AuthWebSocketFrame(BilibiliCodecUtil.encode(authMsg));
        } catch (Exception e) {
            throw new RuntimeException("认证包创建失败，请检查房间号是否正确。roomId: %d, msg: %s".formatted(roomId, e.getMessage()));
        }
    }

    public HeartbeatWebSocketFrame createHeartbeat() {
        HeartbeatMsg heartbeatMsg = new HeartbeatMsg(this.protover.getCode());
        return new HeartbeatWebSocketFrame(BilibiliCodecUtil.encode(heartbeatMsg));
    }

}
