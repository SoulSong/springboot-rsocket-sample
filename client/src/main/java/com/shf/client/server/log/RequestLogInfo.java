package com.shf.client.server.log;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Define a request log entity for store the data and the metadata.
 *
 * @author songhaifeng
 * @date 2019/12/23 12:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestLogInfo {
    /**
     * payload data
     */
    private String data;
    /**
     * payload metadata
     */
    private Map<String, Object> metadata;

}
