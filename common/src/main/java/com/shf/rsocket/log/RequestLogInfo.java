package com.shf.rsocket.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
@ToString
@Slf4j
public class RequestLogInfo {
    /**
     * payload data
     */
    private String data;
    /**
     * payload metadata
     */
    private Map<String, Object> metadata;

    public void log(final String prefix) {
        log.info(">>>>>>>>>>>>>>>>Log Request>>>>>>>>>>>>>>>>>>>");
        log.info("[{}], {}", prefix, this.toString());
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
    }
}
