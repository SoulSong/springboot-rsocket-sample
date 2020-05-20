package com.shf.rsocket.mimetype;


import org.springframework.util.MimeType;

/**
 * Description:
 * The value of mimeTypes must be different.
 *
 * @author: songhaifeng
 * @date: 2019/11/21 14:51
 */
public interface MimeTypes {
    MimeType SECURITY_TOKEN_MIME_TYPE = MimeType.valueOf("message/x.rsocket.authentication.bearer.v0");
    MimeType REFRESH_TOKEN_MIME_TYPE = MimeType.valueOf("message/x.rsocket.authentication.bearer.v1");
    /**
     * link to RSocketStrategiesAutoConfiguration.JacksonJsonStrategyConfiguration.SUPPORTED_TYPES,
     * it supports `new MediaType("application", "*+json")`
     */
    MimeType MAP_MIME_TYPE = MimeType.valueOf("application/vnd.map.metadata+json");
    MimeType FOO_MIME_TYPE = MimeType.valueOf("application/vnd.foo.metadata+json");
    MimeType PARAMETERIZED_TYPE_MIME_TYPE = MimeType.valueOf("application/vnd.foo.2.map.metadata+json");
}
