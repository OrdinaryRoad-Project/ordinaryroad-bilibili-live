package tech.ordinaryroad.bilibili.live.netty.frame;

import io.netty.buffer.ByteBuf;
import tech.ordinaryroad.bilibili.live.netty.frame.base.BaseBilibiliWebSocketFrame;

/**
 * @author mjz
 * @date 2023/1/5
 */
public class AuthWebSocketFrame extends BaseBilibiliWebSocketFrame {

    public AuthWebSocketFrame(ByteBuf byteBuf) {
        super(byteBuf);
    }

}
