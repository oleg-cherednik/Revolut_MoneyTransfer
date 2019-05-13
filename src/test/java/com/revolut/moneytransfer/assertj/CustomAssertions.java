package com.revolut.moneytransfer.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;
import org.assertj.core.api.Assertions;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("ExtendsUtilityClass")
public final class CustomAssertions extends Assertions {

    public static HttpResponseAssert<?> assertThatHttpResponse(HttpResponse resp) {
        return new HttpResponseAssert(resp);
    }
}
