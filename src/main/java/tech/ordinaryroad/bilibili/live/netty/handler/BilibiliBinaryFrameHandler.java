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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import tech.ordinaryroad.bilibili.live.constant.CmdEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliSendSmsReplyMsgListener;
import tech.ordinaryroad.bilibili.live.msg.SendSmsReplyMsg;
import tech.ordinaryroad.bilibili.live.msg.base.BaseBilibiliMsg;
import tech.ordinaryroad.bilibili.live.netty.frame.factory.BilibiliWebSocketFrameFactory;
import tech.ordinaryroad.bilibili.live.util.BilibiliCodecUtil;

import java.net.URI;
import java.util.concurrent.TimeUnit;


/**
 * @author mjz
 * @date 2023/1/4
 */
@Slf4j
public class BilibiliBinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    /**
     * 客户端发送心跳包
     */
    private ScheduledFuture<?> scheduledFuture = null;
    private final IBilibiliSendSmsReplyMsgListener listener;

    public BilibiliBinaryFrameHandler(IBilibiliSendSmsReplyMsgListener listener) {
        this.listener = listener;
    }

    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame message) throws Exception {
        ByteBuf byteBuf = message.content();
        BaseBilibiliMsg msg = BilibiliCodecUtil.decode(byteBuf);
        if (msg == null) {
            return;
        }

        if (msg instanceof SendSmsReplyMsg sendSmsReplyMsg) {
            CmdEnum cmd = sendSmsReplyMsg.getCmd();
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
                }
                case ONLINE_RANK_COUNT -> {
                    // TODO 高能榜数量更新
                }
                case ROOM_REAL_TIME_MESSAGE_UPDATE -> {
                    // TODO 主播粉丝信息更新
                }
                case STOP_LIVE_ROOM_LIST -> {
                    // TODO 停止的直播间信息
                }
                case ONLINE_RANK_V2 -> {
                    // TODO 高能用户排行榜 更新
                }
                default -> {
                    listener.onOtherSendSmsReplyMsg(cmd, sendSmsReplyMsg);
                }
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.error("userEventTriggered {}", evt.getClass());
        if (evt instanceof ChannelInputShutdownReadComplete) {
            // TODO
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            if (null != scheduledFuture && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
            scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
                ctx.writeAndFlush(
                        BilibiliWebSocketFrameFactory.getInstance(ProtoverEnum.NORMAL_ZLIB)
                                .createHeartbeat()
                );
                log.info("发送心跳包");
            }, 15, 30, TimeUnit.SECONDS);
        } else if (evt instanceof SslCloseCompletionEvent) {
            if (null != scheduledFuture && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
        } else {
            log.error("待处理 {}", evt.getClass());
        }
        super.userEventTriggered(ctx, evt);
    }

    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            URI websocketURI = new URI("wss://broadcastlv.chat.bilibili.com:2245/sub");

            BilibiliHandshakerHandler bilibiliHandshakerHandler = new BilibiliHandshakerHandler(WebSocketClientHandshakerFactory.newHandshaker(
                    websocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
            BilibiliBinaryFrameHandler bilibiliHandler = new BilibiliBinaryFrameHandler(new IBilibiliSendSmsReplyMsgListener() {
                @Override
                public void onDanmuMsg(SendSmsReplyMsg msg) {
                    JsonNode info = msg.getInfo();
                    JsonNode jsonNode1 = info.get(1);
                    String danmuText = jsonNode1.asText();
                    JsonNode jsonNode2 = info.get(2);
                    Integer uid = jsonNode2.get(0).asInt();
                    String uname = jsonNode2.get(1).asText();
                    log.debug("收到弹幕 {}({})：{}", uname, uid, danmuText);
                }

                @Override
                public void onSendGift(SendSmsReplyMsg msg) {
                    JsonNode data = msg.getData();
                    String action = data.get("action").asText();
                    String giftName = data.get("giftName").asText();
                    Integer num = data.get("num").asInt();
                    String uname = data.get("uname").asText();
                    Integer price = data.get("price").asInt();
                    log.debug("收到礼物 {} {} {}x{}({})", uname, action, giftName, num, price);
                }

                @Override
                public void onEnterRoom(SendSmsReplyMsg msg) {
                    log.debug("普通用户进入直播间 {}", msg.getData().get("uname").asText());
                }

                @Override
                public void onEntryEffect(SendSmsReplyMsg msg) {
                    JsonNode data = msg.getData();
                    String copyWriting = data.get("copy_writing").asText();
                    log.info("入场效果 {}", copyWriting);
                }

                @Override
                public void onOtherSendSmsReplyMsg(CmdEnum cmd, SendSmsReplyMsg msg) {
                    log.info("其他消息\n{}", cmd);
                }

                @Override
                public void onWatchedChange(SendSmsReplyMsg msg) {
                    JsonNode data = msg.getData();
                    int num = data.get("num").asInt();
                    String textSmall = data.get("text_small").asText();
                    String textLarge = data.get("text_large").asText();
                    log.debug("观看人数变化 {} {} {}", num, textSmall, textLarge);
                }

                @Override
                public void onClickLike(SendSmsReplyMsg msg) {
                    JsonNode data = msg.getData();
                    String uname = data.get("uname").asText();
                    String likeText = data.get("like_text").asText();
                    log.debug("为主播点赞 {} {}", uname, likeText);
                }

                @Override
                public void onClickUpdate(SendSmsReplyMsg msg) {
                    JsonNode data = msg.getData();
                    int clickCount = data.get("click_count").asInt();
                    log.debug("点赞数更新 {}", clickCount);
                }
            });

            //进行握手
            log.info("握手开始");
            System.out.println(websocketURI.getScheme());
            System.out.println(websocketURI.getHost());
            System.out.println(websocketURI.getPort());
//            SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            SslContext sslCtx = SslContextBuilder.forClient().build();

            Bootstrap bootstrap = new Bootstrap()
                    .group(workerGroup)
                    // 创建Channel
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // Channel配置
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 责任链
                            ChannelPipeline pipeline = ch.pipeline();

                            //放到第一位 addFirst 支持wss链接服务端
                            pipeline.addFirst(sslCtx.newHandler(ch.alloc(), websocketURI.getHost(), websocketURI.getPort()));

                            // 添加一个http的编解码器
                            pipeline.addLast(new HttpClientCodec());
                            // 添加一个用于支持大数据流的支持
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
                            pipeline.addLast(new HttpObjectAggregator(1024 * 64));

//                            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
//                            pipeline.addLast(new WebSocketServerProtocolHandler("/sub", null, true, 65536 * 10));

                            // TODO 自定义B站数据protver解码器
                            pipeline.addLast(bilibiliHandshakerHandler);
                            pipeline.addLast(bilibiliHandler);
                        }
                    });

            final Channel channel = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();

            // 阻塞等待是否握手成功
            bilibiliHandshakerHandler.handshakeFuture().sync();

            // TODO 5s内认证
            log.info("发送认证包");
            channel.writeAndFlush(
                    BilibiliWebSocketFrameFactory.getInstance(ProtoverEnum.NORMAL_ZLIB)
//                             7777
//                            .createAuth(545068)
                            .createAuth(21509476)
//                            .createAuth(7396329)
            );

            channel.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
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
