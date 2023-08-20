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

package tech.ordinaryroad.bilibili.live.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ordinaryroad.bilibili.live.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliSendSmsReplyMsgListener;
import tech.ordinaryroad.bilibili.live.netty.frame.factory.BilibiliWebSocketFrameFactory;
import tech.ordinaryroad.bilibili.live.netty.handler.BilibiliBinaryFrameHandler;
import tech.ordinaryroad.bilibili.live.netty.handler.BilibiliHandshakerHandler;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * B站直播间弹幕客户端
 *
 * @author mjz
 * @date 2023/8/20
 */
@Slf4j
public class BilibiliLiveChatClient {

    @Getter
    private final BilibiliLiveChatClientConfig config;
    private final IBilibiliSendSmsReplyMsgListener listener;
    private final EventLoopGroup workerGroup;

    @Getter
    private final Bootstrap bootstrap = new Bootstrap();
    private BilibiliBinaryFrameHandler bilibiliBinaryFrameHandler;
    private BilibiliHandshakerHandler bilibiliHandshakerHandler;
    private Channel channel;
    private volatile boolean initialized = false;

    public BilibiliLiveChatClient(BilibiliLiveChatClientConfig config, IBilibiliSendSmsReplyMsgListener listener, EventLoopGroup workerGroup) {
        this.config = config;
        this.listener = listener;
        this.workerGroup = workerGroup;
    }

    public BilibiliLiveChatClient(BilibiliLiveChatClientConfig config, IBilibiliSendSmsReplyMsgListener listener) {
        this(config, listener, new NioEventLoopGroup());
    }

    /**
     * 初始化，只需要执行一次
     */
    public void init() {
        if (this.initialized) {
            return;
        }
        try {
            URI websocketUri = new URI("wss://broadcastlv.chat.bilibili.com:443/sub");
            this.bilibiliHandshakerHandler = new BilibiliHandshakerHandler(WebSocketClientHandshakerFactory.newHandshaker(
                    websocketUri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
            this.bilibiliBinaryFrameHandler = new BilibiliBinaryFrameHandler(this, this.listener);
            SslContext sslCtx = SslContextBuilder.forClient().build();

            this.bootstrap.group(this.workerGroup)
                    // 创建Channel
                    .channel(NioSocketChannel.class)
                    .remoteAddress(websocketUri.getHost(), websocketUri.getPort())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // Channel配置
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 责任链
                            ChannelPipeline pipeline = ch.pipeline();

                            // 放到第一位 addFirst 支持wss链接服务端
                            pipeline.addFirst(sslCtx.newHandler(ch.alloc(), websocketUri.getHost(), websocketUri.getPort()));

                            // 添加一个http的编解码器
                            pipeline.addLast(new HttpClientCodec());
                            // 添加一个用于支持大数据流的支持
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
                            pipeline.addLast(new HttpObjectAggregator(BilibiliLiveChatClient.this.config.getAggregatorMaxContentLength()));

                            // 握手处理器
                            pipeline.addLast(BilibiliLiveChatClient.this.bilibiliHandshakerHandler);
                            // 弹幕处理器
                            pipeline.addLast(BilibiliLiveChatClient.this.bilibiliBinaryFrameHandler);
                        }
                    });
            this.initialized = true;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initCheck() {
        if (!this.initialized) {
            synchronized (BilibiliLiveChatClient.this) {
                if (!this.initialized) {
                    this.init();
                }
            }
        }
    }

    public void connect(GenericFutureListener genericFutureListener) {
        this.initCheck();
        this.bootstrap.connect().addListener((ChannelFutureListener) connectFuture -> {
            if (genericFutureListener != null) {
                genericFutureListener.operationComplete(connectFuture);
            }
            if (connectFuture.isSuccess()) {
                log.debug("连接成功！");
                this.channel = connectFuture.channel();
                // 监听是否握手成功
                this.bilibiliHandshakerHandler.getHandshakeFuture().addListener((ChannelFutureListener) handshakeFuture -> {
                    // 5s内认证
                    log.debug("发送认证包");
                    send(BilibiliWebSocketFrameFactory.getInstance(this.config.getProtover()).createAuth(this.config.getRoomId()));
                });
            } else {
                log.error("连接失败", connectFuture.cause());
            }
        });
    }

    public void connect() {
        this.connect(null);
    }

    public void send(WebSocketFrame msg) {
        this.channel.writeAndFlush(msg);
    }

}
