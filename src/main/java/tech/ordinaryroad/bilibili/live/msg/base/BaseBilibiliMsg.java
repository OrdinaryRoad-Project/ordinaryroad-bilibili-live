package tech.ordinaryroad.bilibili.live.msg.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.ordinaryroad.bilibili.live.constant.OperationEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;

import java.io.Serializable;

/**
 * @author mjz
 * @date 2023/1/6
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseBilibiliMsg implements Serializable {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static int sequence = 0;

    @JsonIgnore
    public abstract ProtoverEnum getProtoverEnum();

    @JsonIgnore
    public abstract OperationEnum getOperationEnum();

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
