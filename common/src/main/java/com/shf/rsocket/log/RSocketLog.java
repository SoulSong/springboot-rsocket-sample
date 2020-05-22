package com.shf.rsocket.log;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/22 15:44
 */
public interface RSocketLog {
    String SEND = " send";
    String RECEIVE = " receive";
    MimeType METADATA_MIME_TYPE = MimeTypeUtils.parseMimeType(
            WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());
}
