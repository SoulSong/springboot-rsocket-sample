package com.shf.rsocket.entity;

import io.rsocket.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * description :
 * The same payload details in the {@link io.rsocket.ConnectionSetupPayload} and {@link Payload}.
 *
 * @author songhaifeng
 * @date 2020/5/22 14:56
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayloadInfo {

    /**
     * payload data
     */
    private String data;
    /**
     * payload metadata
     */
    private Map<String, Object> metadata;

    public PayloadInfo withData(String data) {
        this.data = data;
        return this;
    }

    public PayloadInfo withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}
