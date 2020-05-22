package com.shf.rsocket.entity;

/**
 * description :
 * Flag the role of service.
 *
 * @author songhaifeng
 * @date 2020/5/22 16:38
 */
public enum RSocketRole {
    /**
     * {@link io.rsocket.core.RSocketConnector}
     */
    RSOCKET_CONNECTOR,
    /**
     * {@link io.rsocket.core.RSocketServer}
     */
    RSOCKET_SERVER;
}
