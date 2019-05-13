package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.HttpUtils;
import com.revolut.moneytransfer.JettyTestGroup;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.utils.JsonUtils;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static com.revolut.moneytransfer.assertj.CustomAssertions.assertThatHttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Test(groups = "it")
@SuppressWarnings("NewClassNamingConvention")
public class AccountEndPointIT extends JettyTestGroup {

    public void shouldCreateNewAccountWhenHolderNameIsSet() {
        String holderName = "oleg";
        HttpResponse resp = doPost(AccountsEndPoint.URI, Collections.singletonMap("holderName", holderName));
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("text/plain", "utf-8");

        UUID accountId = UUID.fromString(Objects.requireNonNull(HttpUtils.getBodyAsString(resp)));
        assertThat(accountId).isNotNull();

        Account account = findAccountById(accountId);
        assertThat(account).isNotNull();
        assertThat(account.getAccountId()).isEqualTo(accountId);
        assertThat(account.getHolderName()).isEqualTo(holderName);
        assertThat(account.getCents()).isZero();
        assertThat(account.getVersion()).isZero();
    }

    public void shouldCreateNewAccountWhenHolderNameIsNotSet() {
        HttpResponse resp = doPost(AccountsEndPoint.URI);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("text/plain", "utf-8");

        UUID accountId = UUID.fromString(Objects.requireNonNull(HttpUtils.getBodyAsString(resp)));
        assertThat(accountId).isNotNull();

        Account account = findAccountById(accountId);
        assertThat(account).isNotNull();
        assertThat(account.getAccountId()).isEqualTo(accountId);
        assertThat(account.getHolderName()).isNull();
        assertThat(account.getCents()).isZero();
        assertThat(account.getVersion()).isZero();
    }

    public void shouldCreateNewAccountWhenNotZeroCents() {
        long cents = 100;
        HttpResponse resp = doPost(AccountsEndPoint.URI, Collections.singletonMap("cents", cents));
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("text/plain", "utf-8");

        UUID accountId = UUID.fromString(Objects.requireNonNull(HttpUtils.getBodyAsString(resp)));
        assertThat(accountId).isNotNull();

        Account account = findAccountById(accountId);
        assertThat(account).isNotNull();
        assertThat(account.getAccountId()).isEqualTo(accountId);
        assertThat(account.getHolderName()).isNull();
        assertThat(account.getCents()).isEqualTo(cents);
        assertThat(account.getVersion()).isZero();
    }

    public void shouldResponseHttpStatusNotFoundWhenAccountNotFoundById() {
        UUID accountId = UUID.randomUUID();
        HttpResponse resp = doGet(AccountsEndPoint.URI + '/' + accountId);
        assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    public void shouldRetrieveExistedAccountWhenFoundById() {
        String holderName = "oleg";
        UUID accountId = createAccount(holderName, 100);

        HttpResponse resp = doGet(AccountsEndPoint.URI + '/' + accountId);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_OK);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("application/json", "utf-8");

        Account account = JsonUtils.read(HttpUtils.getBodyAsString(resp), Account.class);
        assertThat(account).isNotNull();
        assertThat(account.getAccountId()).isEqualTo(accountId);
        assertThat(account.getHolderName()).isEqualTo(holderName);
        assertThat(account.getCents()).isEqualTo(100);
    }

}
