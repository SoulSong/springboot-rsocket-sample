package com.shf.rsocket.interceptor;

import com.shf.rsocket.entity.PayloadInfo;
import io.rsocket.Payload;

/**
 * description :
 *
 * @author songhaifeng
 * @date 2020/5/28 1:19
 */
@FunctionalInterface
public interface PayloadExtractFunction {

    /**
     * 提取{@link Payload}中的 data 和 metadata 数据至{@link PayloadInfo}
     *
     * @param payload         带解析{@link Payload}
     * @param extractData     true表示需要提取data数据；反之不提前
     * @param extractMetadata true表示需要提取metadata数据；反之不提取
     * @return {@link PayloadInfo}
     */
    PayloadInfo extract(Payload payload, boolean extractData, boolean extractMetadata);

}
