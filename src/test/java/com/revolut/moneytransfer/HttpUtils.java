package com.revolut.moneytransfer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

    public static String getBodyAsString(HttpResponse resp) {
        try (Scanner scan = new Scanner(resp.getEntity().getContent()).useDelimiter("\\A")) {
            return scan.hasNext() ? scan.next().trim() : null;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
