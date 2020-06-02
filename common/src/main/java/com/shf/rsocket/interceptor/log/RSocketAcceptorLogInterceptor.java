package com.shf.rsocket.interceptor.log;

import io.rsocket.plugins.SocketAcceptorInterceptor;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/22 11:34
 */
public interface RSocketAcceptorLogInterceptor extends SocketAcceptorInterceptor, RSocketLog {
}
