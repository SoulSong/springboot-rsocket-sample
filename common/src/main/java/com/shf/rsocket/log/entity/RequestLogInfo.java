package com.shf.rsocket.log.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Map;

/**
 * Description:
 * Define a request log entity for store the data and the metadata.
 *
 * @author songhaifeng
 * @date 2019/12/23 12:46
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class RequestLogInfo extends LogInfo {

    @Builder
    public RequestLogInfo(String data, Map<String, Object> metadata) {
        super(data, metadata);
    }

    public void log(final String prefix) {
        log.info(">>>>>>>>>>>>>>>>Log Request>>>>>>>>>>>>>>>>>>>");
        log.info("[{}], {}", prefix, this.toString());
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
