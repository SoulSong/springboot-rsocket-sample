package com.shf.rsocket.log.entity;

import io.rsocket.Payload;
import lombok.AllArgsConstructor;
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
public class LogInfo {

    /**
     * payload data
     */
    private String data;
    /**
     * payload metadata
     */
    private Map<String, Object> metadata;
}
