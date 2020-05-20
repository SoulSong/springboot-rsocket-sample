package com.shf.rsocket.log;

import io.rsocket.Payload;
import io.rsocket.plugins.RSocketInterceptor;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/20 23:29
 */
public interface RSocketLogInterceptor extends RSocketInterceptor{
    void log(Payload payload);
}
