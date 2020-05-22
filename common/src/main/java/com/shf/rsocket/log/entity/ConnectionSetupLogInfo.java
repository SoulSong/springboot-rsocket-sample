package com.shf.rsocket.log.entity;

import com.shf.rsocket.entity.RSocketRole;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Map;

/**
 * description :
 * Define a connectionSetup log entity for store the data and the metadata.
 *
 * @author songhaifeng
 * @date 2020/5/22 15:34
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ConnectionSetupLogInfo extends LogInfo {

    @Builder
    private ConnectionSetupLogInfo(String data, Map<String, Object> metadata) {
        super(data, metadata);
    }

    public void log(final String prefix, final RSocketRole rSocketRole) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n>>>>>>>>>>>>>>>>Log Connection>>>>>>>>>>>>>>>>\n")
                .append("[").append(prefix).append("] ")
                .append(RSocketRole.RSOCKET_SERVER.equals(rSocketRole) ? "receive" : "send")
                .append(" a connect: ")
                .append(this.toString())
                .append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info(stringBuilder.toString());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
