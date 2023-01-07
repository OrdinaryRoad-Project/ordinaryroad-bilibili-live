package tech.ordinaryroad.bilibili.live.msg;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.ordinaryroad.bilibili.live.constant.CmdEnum;
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
public class SendSmsReplyMsg extends BaseBilibiliMsg {

    private Long id;

    private String name;

    private JsonNode full;

    private JsonNode half;

    private JsonNode side;

    private CmdEnum cmd;

    private JsonNode data;

    private JsonNode info;

    private JsonNode msg_common;

    private JsonNode msg_self;

    private JsonNode link_url;

    private JsonNode msg_type;

    private JsonNode shield_uid;

    private JsonNode business_id;

    private Integer protover;

    private Integer roomid;

    private Integer real_roomid;

    @Override
    public ProtoverEnum getProtoverEnum() {
        return ProtoverEnum.getByCode(this.protover);
    }

    @Override
    public OperationEnum getOperationEnum() {
        return OperationEnum.SEND_SMS_REPLY;
    }
}
