package tech.ordinaryroad.bilibili.live.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author mjz
 * @date 2023/1/5
 */
@Getter
@RequiredArgsConstructor
public enum ProtoverEnum {
    /**
     * 普通包正文不使用压缩
     */
    NORMAL_NO_COMPRESSION(0),
    /**
     * 心跳及认证包正文不使用压缩
     */
    HEARTBEAT_AUTH_NO_COMPRESSION(1),
    /**
     * 普通包正文使用zlib压缩
     */
    NORMAL_ZLIB(2),
    /**
     * 普通包正文使用brotli压缩,解压为一个带头部的协议0普通包
     */
    NORMAL_BROTLI(3),
    ;

    private final int code;


    public static ProtoverEnum getByCode(int code) {
        for (ProtoverEnum value : ProtoverEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}
