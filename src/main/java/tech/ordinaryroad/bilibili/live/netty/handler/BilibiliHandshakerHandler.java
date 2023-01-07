package tech.ordinaryroad.bilibili.live.netty.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;


/**
 * @author mjz
 * @date 2023/1/4
 */
@Slf4j
public class BilibiliHandshakerHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public BilibiliHandshakerHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handshaker.handshake(ctx.channel());
    }

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse message) throws Exception {
        // 判断是否正确握手
        if (!this.handshaker.isHandshakeComplete()) {
            try {
                this.handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) message);
                log.debug("握手完成!");
                this.handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.debug("握手失败!");
                this.handshakeFuture.setFailure(e);
            }
            return;

        }
        // 握手失败响应
        if (message instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) message;
            log.error("握手失败！code:{},msg:{}", response.status(), response.content().toString(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!this.handshakeFuture.isDone()) {
            this.handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

}
