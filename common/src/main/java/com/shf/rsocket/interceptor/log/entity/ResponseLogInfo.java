package com.shf.rsocket.interceptor.log.entity;

import com.shf.rsocket.entity.PayloadInfo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Map;

/**
 * description :
 * Define a response log entity for store the data and the metadata.
 *
 * @author songhaifeng
 * @date 2020/5/22 14:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ResponseLogInfo extends PayloadInfo {
    /**
     * execution time
     */
    private long spentTime;

    @Builder
    public ResponseLogInfo(String data, Map<String, Object> metadata, long spentTime) {
        super(data, metadata);
        this.spentTime = spentTime;
    }

    public void log(final String prefix) {
        log.info(">>>>>>>>>>>>>>>>Log Response>>>>>>>>>>>>>>>>>>>");
        log.info("[{}], {}", prefix, this.toString());
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "metadata");
    }
}
