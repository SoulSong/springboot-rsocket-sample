package com.shf.rsocket.interceptor;

import com.shf.rsocket.entity.PayloadInfo;
import io.rsocket.Payload;

import java.util.Map;

/**
 * description :
 * Extract the data and metadata in {@link Payload}.
 *
 * @author songhaifeng
 * @date 2020/5/28 1:09
 */
public class PayloadUtils {

    public static Map<String, Object> extractMetadata(PayloadExtractFunction payloadExtractFunction, Payload payload) {
        return payloadExtractFunction.extract(payload, false, true).getMetadata();
    }

    public static String extractData(PayloadExtractFunction payloadExtractFunction, Payload payload) {
        return payloadExtractFunction.extract(payload, true, false).getData();
    }


    public static PayloadInfo extractPayload(PayloadExtractFunction payloadExtractFunction, Payload payload) {
        return payloadExtractFunction.extract(payload, true, true);
    }
}
