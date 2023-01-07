package tech.ordinaryroad.bilibili.live.netty.frame.factory;

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

    public AuthWebSocketFrame createAuth(int roomId) {
        AuthMsg authMsg = new AuthMsg(roomId, this.protover.getCode());
        return new AuthWebSocketFrame(BilibiliCodecUtil.encode(authMsg));
    }

    public HeartbeatWebSocketFrame createHeartbeat() {
        HeartbeatMsg heartbeatMsg = new HeartbeatMsg(this.protover.getCode());
        return new HeartbeatWebSocketFrame(BilibiliCodecUtil.encode(heartbeatMsg));
    }

}
