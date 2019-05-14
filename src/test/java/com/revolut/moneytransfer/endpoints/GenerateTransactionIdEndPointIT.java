package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.JettyTestGroup;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

import static com.revolut.moneytransfer.assertj.CustomAssertions.assertThatHttpResponse;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Test(groups = "it")
@SuppressWarnings("NewClassNamingConvention")
public class GenerateTransactionIdEndPointIT extends JettyTestGroup {

    public void shouldGenerateNewTransactionId() {
        HttpResponse resp = doPost(GenerateTransactionIdEndPoint.URI);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_OK);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("text/plain", "utf-8");
        assertThatHttpResponse(resp).isBodyNumber();
    }
}
