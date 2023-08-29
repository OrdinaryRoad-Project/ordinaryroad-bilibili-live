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

import io.netty.channel.*;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ordinaryroad.bilibili.live.client.BilibiliLiveChatClient;
import tech.ordinaryroad.bilibili.live.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliConnectionListener;
import tech.ordinaryroad.bilibili.live.netty.frame.factory.BilibiliWebSocketFrameFactory;

import java.util.concurrent.TimeUnit;


/**
 * 连接处理器
 *
 * @author mjz
 * @date 2023/8/21
 */
@Slf4j
@ChannelHandler.Sharable
public class BilibiliConnectionHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final long roomId;
    private final ProtoverEnum protover;
    private String cookie;
    private final WebSocketClientHandshaker handshaker;
    @Getter
    private ChannelPromise handshakeFuture;
    private final BilibiliLiveChatClient client;
    private final IBilibiliConnectionListener listener;
    /**
     * 客户端发送心跳包
     */
    private ScheduledFuture<?> scheduledFuture = null;
    private final BilibiliWebSocketFrameFactory webSocketFrameFactory;

    public BilibiliConnectionHandler(WebSocketClientHandshaker handshaker, IBilibiliConnectionListener listener, BilibiliLiveChatClient client) {
        this.handshaker = handshaker;
        this.client = client;
        this.listener = listener;
        this.roomId = client.getConfig().getRoomId();
        this.protover = client.getConfig().getProtover();
        this.cookie = client.getConfig().getCookie();
        this.webSocketFrameFactory = BilibiliWebSocketFrameFactory.getInstance(roomId, protover, cookie);
    }

    public BilibiliConnectionHandler(WebSocketClientHandshaker handshaker, IBilibiliConnectionListener listener, long roomId, ProtoverEnum protover, String cookie) {
        this.handshaker = handshaker;
        this.client = null;
        this.listener = listener;
        this.roomId = roomId;
        this.protover = protover;
        this.cookie = cookie;
        this.webSocketFrameFactory = BilibiliWebSocketFrameFactory.getInstance(roomId, protover, cookie);
    }

    public BilibiliConnectionHandler(WebSocketClientHandshaker handshaker, IBilibiliConnectionListener listener, long roomId, ProtoverEnum protover) {
        this.handshaker = handshaker;
        this.client = null;
        this.listener = listener;
        this.roomId = roomId;
        this.protover = protover;
        this.cookie = null;
        this.webSocketFrameFactory = BilibiliWebSocketFrameFactory.getInstance(roomId, protover);
    }

    public BilibiliConnectionHandler(WebSocketClientHandshaker handshaker, long roomId, ProtoverEnum protover) {
        this.handshaker = handshaker;
        this.client = null;
        this.listener = null;
        this.roomId = roomId;
        this.protover = protover;
        this.cookie = null;
        this.webSocketFrameFactory = BilibiliWebSocketFrameFactory.getInstance(roomId, protover);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handshaker.handshake(ctx.channel());
    }

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        // 判断是否正确握手
        if (this.handshaker.isHandshakeComplete()) {
            handshakeSuccessfully(ctx, msg);
        } else {
            try {
                handshakeSuccessfully(ctx, msg);
            } catch (WebSocketHandshakeException e) {
                handshakeFailed(msg, e);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("userEventTriggered {}", evt.getClass());
        if (evt instanceof ChannelInputShutdownReadComplete) {
            // TODO
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            heartbeatCancel();
            scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
                log.debug("发送心跳包");
                ctx.writeAndFlush(
                        webSocketFrameFactory.createHeartbeat()
                ).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.debug("心跳包发送完成");
                    } else {
                        log.error("心跳包发送失败", future.cause());
                    }
                });
            }, getHeartbeatInitialDelay(), getHeartbeatPeriod(), TimeUnit.SECONDS);
            if (this.listener != null) {
                listener.onConnected();
            }
        } else if (evt instanceof SslCloseCompletionEvent) {
            heartbeatCancel();
            if (this.listener != null) {
                listener.onDisconnected(BilibiliConnectionHandler.this);
            }
        } else {
            log.error("待处理 {}", evt.getClass());
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 取消发送心跳包
     */
    private void heartbeatCancel() {
        if (null != scheduledFuture && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    private long getHeartbeatPeriod() {
        if (client == null) {
            return BilibiliLiveChatClientConfig.DEFAULT_HEARTBEAT_PERIOD;
        } else {
            return client.getConfig().getHeartbeatPeriod();
        }
    }

    private long getHeartbeatInitialDelay() {
        if (client == null) {
            return BilibiliLiveChatClientConfig.DEFAULT_HEARTBEAT_INITIAL_DELAY;
        } else {
            return client.getConfig().getHeartbeatInitialDelay();
        }
    }


    private void handshakeSuccessfully(ChannelHandlerContext ctx, FullHttpResponse msg) {
        log.debug("握手完成!");
        this.handshaker.finishHandshake(ctx.channel(), msg);
        this.handshakeFuture.setSuccess();
    }

    private void handshakeFailed(FullHttpResponse msg, WebSocketHandshakeException e) {
        log.error("握手失败！status:" + msg.status(), e);
        this.handshakeFuture.setFailure(e);
        if (listener != null) {
            this.listener.onConnectFailed(this);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught", cause);
        if (!this.handshakeFuture.isDone()) {
            this.handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
