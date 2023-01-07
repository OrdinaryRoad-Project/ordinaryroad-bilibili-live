package tech.ordinaryroad.bilibili.live.msg;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tech.ordinaryroad.bilibili.live.constant.OperationEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;
import tech.ordinaryroad.bilibili.live.msg.base.BaseBilibiliMsg;

/**
 * @author mjz
 * @date 2023/1/6
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AuthMsg extends BaseBilibiliMsg {

    /**
     * 用户uid，0代表游客
     */
    private int uid;
    /**
     * 房间id room_id，不是短id short_id
     * 可以通过将url参数id改为直播地址中的数字来查询房间真实id
     * example: <a href="https://api.live.bilibili.com/room/v1/Room/room_init?id=6">https://api.live.bilibili.com/room/v1/Room/room_init?id=6</a>
     */
    private final int roomid;
    /**
     * 协议版本
     *
     * @see ProtoverEnum#getCode()
     */
    private final int protover;
    /**
     * 平台标识
     */
    private String platform = "web";
    private int type = 2;
    /**
     * 认证秘钥
     */
    private String key = StrUtil.EMPTY;

    @Override
    public ProtoverEnum getProtoverEnum() {
        return ProtoverEnum.getByCode(this.protover);
    }

    @Override
    public OperationEnum getOperationEnum() {
        return OperationEnum.AUTH;
    }

}
