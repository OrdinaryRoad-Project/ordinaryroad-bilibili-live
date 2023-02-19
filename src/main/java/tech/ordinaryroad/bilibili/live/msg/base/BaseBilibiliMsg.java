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

package tech.ordinaryroad.bilibili.live.msg.base;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.ordinaryroad.bilibili.live.constant.OperationEnum;
import tech.ordinaryroad.bilibili.live.constant.ProtoverEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mjz
 * @date 2023/1/6
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseBilibiliMsg implements Serializable {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static int sequence = 0;

    /**
     * 未知属性都放在这
     */
    private final Map<String, JsonNode> unknownProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, JsonNode> getUnknownProperties() {
        return unknownProperties;
    }

    @JsonAnySetter
    public void setOther(String key, JsonNode value) {
        this.unknownProperties.put(key, value);
    }

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
