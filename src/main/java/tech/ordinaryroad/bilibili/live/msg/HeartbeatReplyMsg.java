package tech.ordinaryroad.bilibili.live.msg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatReplyMsg extends BaseBilibiliMsg {

    private int popularity;

    @JsonIgnore
    private int protover;

    @Override
    public ProtoverEnum getProtoverEnum() {
        return ProtoverEnum.getByCode(protover);
    }

    @Override
    public OperationEnum getOperationEnum() {
        return OperationEnum.HEARTBEAT_REPLY;
    }
}
