package com.revolut.moneytransfer.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
