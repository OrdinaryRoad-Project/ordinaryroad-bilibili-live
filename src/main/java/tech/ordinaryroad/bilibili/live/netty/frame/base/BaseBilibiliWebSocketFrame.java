package tech.ordinaryroad.bilibili.live.netty.frame.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import tech.ordinaryroad.bilibili.live.constant.OperationEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;

/**
 * 实现Bilibili协议的BinaryWebSocketFrame
 * <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/live/message_stream.md#数据包格式">数据包格式</a>
 *
 * @author mjz
 * @date 2023/1/5
 */
public abstract class BaseBilibiliWebSocketFrame extends BinaryWebSocketFrame {

    public static int sequence = 0;

    public ProtoverEnum getProtoverEnum() {
        return ProtoverEnum.getByCode(super.content().getShort(6));
    }

    public OperationEnum getOperationEnum() {
        return OperationEnum.getByCode(super.content().getInt(8));
    }

    public BaseBilibiliWebSocketFrame(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
