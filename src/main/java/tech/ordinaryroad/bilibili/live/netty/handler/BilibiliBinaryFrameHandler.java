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

package tech.ordinaryroad.bilibili.live.netty.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import tech.ordinaryroad.bilibili.live.client.BilibiliLiveChatClient;
import tech.ordinaryroad.bilibili.live.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.bilibili.live.constant.CmdEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliSendSmsReplyMsgListener;
import tech.ordinaryroad.bilibili.live.msg.SendSmsReplyMsg;
import tech.ordinaryroad.bilibili.live.msg.base.BaseBilibiliMsg;
import tech.ordinaryroad.bilibili.live.netty.frame.factory.BilibiliWebSocketFrameFactory;
import tech.ordinaryroad.bilibili.live.util.BilibiliCodecUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 消息处理器
 *
 * @author mjz
 * @date 2023/1/4
 */
@Slf4j
@ChannelHandler.Sharable
public class BilibiliBinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    private final IBilibiliSendSmsReplyMsgListener listener;

    public BilibiliBinaryFrameHandler(IBilibiliSendSmsReplyMsgListener listener) {
        this.listener = listener;
    }

    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame message) throws Exception {
        ByteBuf byteBuf = message.content();
        List<BaseBilibiliMsg> msgList = BilibiliCodecUtil.decode(byteBuf);
        for (BaseBilibiliMsg msg : msgList) {
            if (msg instanceof SendSmsReplyMsg sendSmsReplyMsg) {
                CmdEnum cmd = sendSmsReplyMsg.getCmdEnum();
                if (cmd == null) {
                    listener.onUnknownCmd(sendSmsReplyMsg.getCmd(), sendSmsReplyMsg);
                    return;
                }
                // log.debug("收到 {} 消息 {}", cmd, msg);
                switch (cmd) {
                    case DANMU_MSG -> listener.onDanmuMsg(sendSmsReplyMsg);
                    case SEND_GIFT -> listener.onSendGift(sendSmsReplyMsg);
                    case INTERACT_WORD -> listener.onEnterRoom(sendSmsReplyMsg);
                    case ENTRY_EFFECT -> listener.onEntryEffect(sendSmsReplyMsg);
                    case WATCHED_CHANGE -> listener.onWatchedChange(sendSmsReplyMsg);
                    case LIKE_INFO_V3_CLICK -> listener.onClickLike(sendSmsReplyMsg);
                    case LIKE_INFO_V3_UPDATE -> listener.onClickUpdate(sendSmsReplyMsg);
                    case HOT_RANK_CHANGED_V2 -> {
                        // TODO 主播实时活动排名变化
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                    case ONLINE_RANK_COUNT -> {
                        // TODO 高能榜数量更新
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                    case ROOM_REAL_TIME_MESSAGE_UPDATE -> {
                        // TODO 主播粉丝信息更新
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                    case STOP_LIVE_ROOM_LIST -> {
                        // TODO 停止直播的房间ID列表
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                    case ONLINE_RANK_V2 -> {
                        // TODO 高能用户排行榜 更新
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                    default -> {
                        listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause.getCause() instanceof UnrecognizedPropertyException) {
            log.error("缺少字段：{}", cause.getMessage());
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
