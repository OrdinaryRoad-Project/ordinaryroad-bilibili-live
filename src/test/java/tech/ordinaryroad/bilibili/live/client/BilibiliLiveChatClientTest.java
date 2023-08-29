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

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ordinaryroad.bilibili.live.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliConnectionListener;
import tech.ordinaryroad.bilibili.live.listener.IBilibiliSendSmsReplyMsgListener;
import tech.ordinaryroad.bilibili.live.msg.SendSmsReplyMsg;
import tech.ordinaryroad.bilibili.live.netty.handler.BilibiliBinaryFrameHandler;
import tech.ordinaryroad.bilibili.live.netty.handler.BilibiliConnectionHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author mjz
 * @date 2023/8/20
 */
@Slf4j
class BilibiliLiveChatClientTest implements IBilibiliSendSmsReplyMsgListener {

    static Object lock = new Object();
    BilibiliLiveChatClient client;

    @Test
    void example() throws InterruptedException {
        String cookie = System.getenv("cookie");
        log.error("cookie: {}", cookie);
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                // TODO 浏览器Cookie
                .cookie(cookie)
                .roomId(7777)
                .build();

        client = new BilibiliLiveChatClient(config, new IBilibiliSendSmsReplyMsgListener() {
            @Override
            public void onDanmuMsg(BilibiliBinaryFrameHandler binaryFrameHandler, SendSmsReplyMsg msg) {
                IBilibiliSendSmsReplyMsgListener.super.onDanmuMsg(binaryFrameHandler, msg);

                JsonNode info = msg.getInfo();
                JsonNode jsonNode1 = info.get(1);
                String danmuText = jsonNode1.asText();
                JsonNode jsonNode2 = info.get(2);
                Long uid = jsonNode2.get(0).asLong();
                String uname = jsonNode2.get(1).asText();
                log.info("{} 收到弹幕 {}({})：{}", binaryFrameHandler.getRoomId(), uname, uid, danmuText);
            }

            @Override
            public void onDanmuMsg(SendSmsReplyMsg msg) {
                JsonNode info = msg.getInfo();
                JsonNode jsonNode1 = info.get(1);
                String danmuText = jsonNode1.asText();
                JsonNode jsonNode2 = info.get(2);
                Long uid = jsonNode2.get(0).asLong();
                String uname = jsonNode2.get(1).asText();
                log.debug("收到弹幕 {}({})：{}", uname, uid, danmuText);
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void autoReconnect() throws Exception {
        String cookie = System.getenv("cookie");
        log.error("cookie: {}", cookie);
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                // TODO 浏览器Cookie
                .cookie(cookie)
                .roomId(7777)
                .build();

        client = new BilibiliLiveChatClient(config, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnected");
                log.info("连接成功，10s后将断开连接，模拟自动重连");
                client.getWorkerGroup().schedule(new Runnable() {
                    @Override
                    public void run() {
                        client.disconnect();
                    }
                }, 10, TimeUnit.SECONDS);
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onDisconnected");
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void disableAutoReconnect() throws InterruptedException {
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(false)
                .roomId(7777)
                .build();

        client = new BilibiliLiveChatClient(config, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnected");
                log.info("连接成功，10s后将断开连接");
                client.getWorkerGroup().schedule(new Runnable() {
                    @Override
                    public void run() {
                        client.disconnect();
                    }
                }, 10, TimeUnit.SECONDS);
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onDisconnected");
                client.destroy();
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void protover3() throws InterruptedException {
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(true)
                .roomId(7777)
                .protover(ProtoverEnum.NORMAL_BROTLI)
                .build();

        client = new BilibiliLiveChatClient(config, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnected");
            }

            @Override
            public void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnectFailed");
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onDisconnected");
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void protover1() throws InterruptedException {
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(true)
                .roomId(7777)
                .protover(ProtoverEnum.HEARTBEAT_AUTH_NO_COMPRESSION)
                .build();

        client = new BilibiliLiveChatClient(config, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnected");
            }

            @Override
            public void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnectFailed");
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onDisconnected");
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void protover0() throws InterruptedException {
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(true)
                .roomId(7777)
                .protover(ProtoverEnum.NORMAL_NO_COMPRESSION)
                .build();

        client = new BilibiliLiveChatClient(config, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnected");
            }

            @Override
            public void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
                log.error("onConnectFailed");
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("onDisconnected");
            }
        });
        client.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void cancelReconnect() throws InterruptedException {
        String cookie = System.getenv("cookie");
        log.error("cookie: {}", cookie);
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder()
                // TODO 浏览器Cookie
                .cookie(cookie)
                .roomId(7777)
                .build();

        client = new BilibiliLiveChatClient(config, new IBilibiliSendSmsReplyMsgListener() {
            @Override
            public void onDanmuMsg(BilibiliBinaryFrameHandler binaryFrameHandler, SendSmsReplyMsg msg) {
                JsonNode info = msg.getInfo();
                JsonNode jsonNode1 = info.get(1);
                String danmuText = jsonNode1.asText();
                JsonNode jsonNode2 = info.get(2);
                Long uid = jsonNode2.get(0).asLong();
                String uname = jsonNode2.get(1).asText();
                log.info("{} 收到弹幕 {}({})：{}", binaryFrameHandler.getRoomId(), uname, uid, danmuText);
            }
        });
        client.connect();

        log.error("10s后断开连接，并关闭自动重连");
        client.getWorkerGroup().schedule(() -> {
            client.disconnect(true);
            log.error("断开连接，10s后手动进行重新连接");
            client.getWorkerGroup().schedule(() -> {
                client.connect(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.error("手动重新连接成功，10s后断开链接，演示自动重连");
                            client.getWorkerGroup().schedule(() -> {
                                client.disconnect();
                            }, 10, TimeUnit.SECONDS);
                        }
                    }
                });
            }, 10, TimeUnit.SECONDS);
        }, 10, TimeUnit.SECONDS);

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Test
    void multiplyClient() throws InterruptedException {
        BilibiliLiveChatClientConfig config1 = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(true)
                .roomId(7777)
                .protover(ProtoverEnum.NORMAL_ZLIB)
                .build();

        BilibiliLiveChatClient client1 = new BilibiliLiveChatClient(config1, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onConnected", connectionHandler.getRoomId());
            }

            @Override
            public void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onConnectFailed", connectionHandler.getRoomId());
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onDisconnected", connectionHandler.getRoomId());
            }
        });
        client1.connect();

        BilibiliLiveChatClientConfig config2 = BilibiliLiveChatClientConfig.builder()
                .autoReconnect(true)
                .roomId(6)
                .protover(ProtoverEnum.NORMAL_BROTLI)
                .build();

        BilibiliLiveChatClient client2 = new BilibiliLiveChatClient(config2, this, new IBilibiliConnectionListener() {
            @Override
            public void onConnected(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onConnected", connectionHandler.getRoomId());
            }

            @Override
            public void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onConnectFailed", connectionHandler.getRoomId());
            }

            @Override
            public void onDisconnected(BilibiliConnectionHandler connectionHandler) {
                log.error("{} onDisconnected", connectionHandler.getRoomId());
            }
        });
        client2.connect();

        // 防止测试时直接退出
        while (true) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    @Override
    public void onDanmuMsg(BilibiliBinaryFrameHandler binaryFrameHandler, SendSmsReplyMsg msg) {
        JsonNode info = msg.getInfo();
        JsonNode jsonNode1 = info.get(1);
        String danmuText = jsonNode1.asText();
        JsonNode jsonNode2 = info.get(2);
        Long uid = jsonNode2.get(0).asLong();
        String uname = jsonNode2.get(1).asText();
        log.info("{} 收到弹幕 {}({})：{}", binaryFrameHandler.getRoomId(), uname, uid, danmuText);
    }
}