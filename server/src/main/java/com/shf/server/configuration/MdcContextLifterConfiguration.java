package com.shf.server.configuration;

import com.shf.reactive.mdc.MdcContextLifterHook;
import com.shf.rsocket.interceptor.trace.TraceConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/6/3 18:49
 */
@Configuration
public class MdcContextLifterConfiguration {

    @Bean(initMethod = "contextOperatorHook", destroyMethod = "cleanupHook")
    public MdcContextLifterHook mdcContextLifterHook() {
        return new MdcContextLifterHook(Collections.singleton(TraceConstant.TRACE_ID));
    }
}
