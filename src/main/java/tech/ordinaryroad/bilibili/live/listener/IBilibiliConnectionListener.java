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

package tech.ordinaryroad.bilibili.live.listener;

import io.netty.channel.ChannelFuture;
import tech.ordinaryroad.bilibili.live.client.BilibiliLiveChatClient;
import tech.ordinaryroad.bilibili.live.netty.handler.BilibiliConnectionHandler;

/**
 * 连接回调
 *
 * @author mjz
 * @date 2023/8/21
 */
public interface IBilibiliConnectionListener {

    /**
     * 连接建立成功
     */
    default void onConnected() {
        // ignore
    }

    /**
     * 连接建立失败
     * @param connectionHandler
     */
    default void onConnectFailed(BilibiliConnectionHandler connectionHandler) {
        // ignore
    }

    /**
     * 连接断开，使用{@link BilibiliLiveChatClient}时内部已实现自动重连
     *
     * @param connectionHandler
     */
    default void onDisconnected(BilibiliConnectionHandler connectionHandler) {
        // ignore
    }
}
