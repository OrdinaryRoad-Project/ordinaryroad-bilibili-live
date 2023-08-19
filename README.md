# ordinaryroad-bilibili-live

![license](https://img.shields.io/github/license/1962247851/ordinaryroad-bilibili-live) ![release](https://img.shields.io/github/v/release/1962247851/ordinaryroad-bilibili-live)

使用Netty来连接B站直播间的弹幕信息流Websocket接口

- Feature 0: Netty
- Feature 1: 消息中的未知属性统一放到单独的MAP中
- Feature 2: 支持房间短id

> 适配清单
> - LOG_IN_NOTICE： 未登录观看直播一段时间后，会导致不显示用户名、uid信息问题 <img width="1512" alt="image" src="https://github.com/1962247851/ordinaryroad-bilibili-live/assets/43869694/21b38f50-aa3e-4e3e-b4b1-8064bb9bacb2">


### 1. 引入依赖

```xml

<dependency>
    <groupId>tech.ordinaryroad.bilibili.live</groupId>
    <artifactId>ordinaryroad-bilibili-live</artifactId>
    <!-- 参考github release版本，不需要前缀`v` -->
    <version>${ordinaryroad-bilibili-live.version}</version>
</dependency>
```

### 2. 开始使用

> 参考`BilibiliBinaryFrameHandlerTest`测试类

重写`IBilibiliSendSmsReplyMsgListener`中的方法，进行处理业务逻辑（耗时操作可能需要异步）

### BilibiliBinaryFrameHandlerTest

修改创建认证包方法的参数后，运行查看效果

> 创建发送认证包
![创建认证包](example/createAuth.png)

> 控制台输出示例
![控制台示例](example/console.png)

> 注：目前protover仅支持2（普通包正文使用zlib压缩）
> CmdEnum可能不全，需要根据控制台信息手动补（不影响运行）

### 相关链接

- [B站直播数据包分析连载（2018-12-11更新）_weixin_34009794的博客-CSDN博客](https://blog.csdn.net/weixin_34009794/article/details/88689474)
- [获取bilibili直播弹幕的WebSocket协议_炒鸡嗨客协管徐的博客-CSDN博客](https://blog.csdn.net/xfgryujk/article/details/80306776)
- [GitHub - SocialSisterYi/bilibili-API-collect: 哔哩哔哩-API收集整理【不断更新中....】](https://github.com/SocialSisterYi/bilibili-API-collect)
- [GitHub - LiQing-Code/BLiveDanmu: 用于获取哔哩哔哩直播间弹幕数据](https://github.com/LiQing-Code/BLiveDanmu)
- [Java 使用zlib压缩和解压字符_、Pacific的博客-CSDN博客_java zlib](https://blog.csdn.net/qq_42670703/article/details/123370008)
- [https://s1.hdslb.com/bfs/blive-engineer/live-web-player/room-player.min.js](https://s1.hdslb.com/bfs/blive-engineer/live-web-player/room-player.min.js)

