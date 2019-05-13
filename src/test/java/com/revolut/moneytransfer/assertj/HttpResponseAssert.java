package com.revolut.moneytransfer.assertj;

import com.revolut.moneytransfer.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.assertj.core.api.AbstractAssert;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class HttpResponseAssert<SELF extends HttpResponseAssert<SELF>> extends AbstractAssert<SELF, HttpResponse> {

    public HttpResponseAssert(HttpResponse actual) {
        super(actual, HttpResponseAssert.class);
    }

    public SELF hasStatusCode(int statusCode) {
        assertThat(actual.getStatusLine().getStatusCode()).isEqualTo(statusCode);
        return myself;
    }

    public SELF hasContentTypeWithCharset(String name, String charset) {
        assertThat(actual.getHeaders("Content-Type")).hasSize(1);

        Header header = actual.getHeaders("Content-Type")[0];
        assertThat(header.getElements()).hasSize(1);

        HeaderElement headerElement = header.getElements()[0];
        assertThat(headerElement.getName()).isEqualToIgnoringCase(name);
        assertThat(headerElement.getParameterByName("charset").getValue()).isEqualToIgnoringCase(charset);

        return myself;
    }

    public SELF isBodyUuid() {
        String body = HttpUtils.getBodyAsString(actual);
        assertThat(body).isNotNull();
        assertThat(UUID.fromString(body)).isNotNull();
        return myself;
    }

    public SELF isBodyNumber() {
        String body = HttpUtils.getBodyAsString(actual);
        assertThat(body).isNotNull();
        assertThat(Long.parseLong(body)).isGreaterThanOrEqualTo(1);
        return myself;
    }

}