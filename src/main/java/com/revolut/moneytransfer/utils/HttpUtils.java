package com.revolut.moneytransfer.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

    public static Map<String, Object> getBody(HttpServletRequest req) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        return JsonUtils.readMap(json);
    }

}
