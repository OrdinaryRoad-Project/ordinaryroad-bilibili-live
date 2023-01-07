package tech.ordinaryroad.bilibili.live.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author mjz
 * @date 2023/1/5
 */
@Getter
@RequiredArgsConstructor
public enum OperationEnum {
    HANDSHAKE(0),
    HANDSHAKE_REPLY(1),
    /**
     * 心跳包
     */
    HEARTBEAT(2),
    /**
     * 心跳包回复（人气值）
     */
    HEARTBEAT_REPLY(3),
    SEND_MSG(4),

    /**
     * 普通包（命令）
     */
    SEND_SMS_REPLY(5),
    DISCONNECT_REPLY(6),

    /**
     * 认证包
     */
    AUTH(7),

    /**
     * 认证包回复
     */
    AUTH_REPLY(8),
    RAW(9),
    PROTO_READY(10),
    PROTO_FINISH(11),
    CHANGE_ROOM(12),
    CHANGE_ROOM_REPLY(13),
    REGISTER(14),
    REGISTER_REPLY(15),
    UNREGISTER(16),
    UNREGISTER_REPLY(17),
    ;

    private final int code;

    public static OperationEnum getByCode(int code) {
        for (OperationEnum value : OperationEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}