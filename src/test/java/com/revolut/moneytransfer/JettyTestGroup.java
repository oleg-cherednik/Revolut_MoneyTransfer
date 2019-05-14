package com.revolut.moneytransfer;

import com.revolut.moneytransfer.endpoints.AccountsEndPoint;
import com.revolut.moneytransfer.endpoints.GenerateTransactionIdEndPoint;
import com.revolut.moneytransfer.endpoints.TransactionsEndPoint;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Transaction;
import com.revolut.moneytransfer.utils.JsonUtils;
import lombok.NonNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.revolut.moneytransfer.Application.createServletHandler;
import static com.revolut.moneytransfer.assertj.CustomAssertions.assertThatHttpResponse;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
public class JettyTestGroup {

    private static Server server;

    @BeforeGroups("it")
    public void startJetty() throws Exception {
        server = new Server(0);
        server.setHandler(createServletHandler());
        server.start();
    }

    @AfterGroups("it")
    public void stopJetty() throws Exception {
        server.stop();
        server = null;
    }

    public static HttpResponse doGet(@NonNull String uri) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(server.getURI().resolve(uri));
            return client.execute(get);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static HttpResponse doPost(@NonNull String uri) {
        return doPost(uri, Collections.emptyMap());
    }

    protected static HttpResponse doPost(@NonNull String uri, @NonNull Map<String, Object> body) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(server.getURI().resolve(uri));
            post.setEntity(new StringEntity(JsonUtils.write(body)));
            return client.execute(post);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static UUID createAccount(String holderName, long cents) {
        Map<String, Object> body = new HashMap<>();
        body.put("holderName", holderName);
        body.put("cents", cents);

        HttpResponse resp = doPost(AccountsEndPoint.URI, body);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);
        return UUID.fromString(Objects.requireNonNull(HttpUtils.getBodyAsString(resp)));
    }

    protected static Account findAccountById(UUID accountId) {
        HttpResponse resp = doGet(AccountsEndPoint.URI + '/' + accountId);

        if (resp.getStatusLine().getStatusCode() == HttpServletResponse.SC_NOT_FOUND)
            return null;
        if (resp.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK)
            return JsonUtils.read(HttpUtils.getBodyAsString(resp), Account.class);

        throw new RuntimeException("Internal error while findAccountById");
    }

    protected static long getTransactionId() {
        HttpResponse resp = doPost(GenerateTransactionIdEndPoint.URI);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_OK);
        return Long.parseLong(Objects.requireNonNull(HttpUtils.getBodyAsString(resp)));
    }

    protected static Transaction findTransactionById(long transactionId) {
        HttpResponse resp = doGet(TransactionsEndPoint.URI + '/' + transactionId);

        if (resp.getStatusLine().getStatusCode() == HttpServletResponse.SC_NOT_FOUND)
            return null;
        if (resp.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK)
            return JsonUtils.read(HttpUtils.getBodyAsString(resp), Transaction.class);

        throw new RuntimeException("Internal error while findTransactionById");
    }

}
