package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.HttpUtils;
import com.revolut.moneytransfer.JettyTestGroup;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Transaction;
import com.revolut.moneytransfer.utils.JsonUtils;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.revolut.moneytransfer.assertj.CustomAssertions.assertThatHttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Test(groups = "it")
@SuppressWarnings("NewClassNamingConvention")
public class TransactionEndPointIT extends JettyTestGroup {

    public void shouldResponseHttpStatusBadRequestWhenSrcAccountNotFoundForNewTransaction() {
        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", UUID.randomUUID());
        params.put("destAccountId", createAccount("anna", 0));
        params.put("cents", 100);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void shouldResponseHttpStatusBadRequestWhenDestAccountNotFoundForNewTransaction() {
        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", createAccount("oleg", 100));
        params.put("destAccountId", UUID.randomUUID());
        params.put("cents", 100);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void shouldResponseHttpStatusBadRequestWhenSrcAndDestAccountsAreSameForNewTransaction() {
        UUID accountId = createAccount("oleg", 100);

        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", accountId);
        params.put("destAccountId", accountId);
        params.put("cents", 100);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void shouldResponseHttpStatusBadRequestWhenCentsNegativeForNewTransaction() {
        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", createAccount("oleg", 100));
        params.put("destAccountId", createAccount("anna", 0));
        params.put("cents", -100);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void shouldResponseHttpStatusBadRequestWhenCentsZeroForNewTransaction() {
        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", createAccount("oleg", 100));
        params.put("destAccountId", createAccount("anna", 0));
        params.put("cents", 0);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void shouldCreateNewTransaction() {
        UUID srcAccountId = createAccount("oleg", 100);
        UUID destAccountId = createAccount("anna", 0);
        long cents = 100;

        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", srcAccountId);
        params.put("destAccountId", destAccountId);
        params.put("cents", cents);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);
        assertThatHttpResponse(resp).hasContentTypeWithCharset("application/json", "utf-8");

        Transaction transaction = JsonUtils.read(HttpUtils.getBodyAsString(resp), Transaction.class);
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionId()).isGreaterThan(0);
        assertThat(transaction.getSrcAccountId()).isEqualTo(srcAccountId);
        assertThat(transaction.getDestAccountId()).isEqualTo(destAccountId);
        assertThat(transaction.getCents()).isEqualTo(cents);
        assertThat(transaction.getStatus()).isEqualTo(Transaction.Status.ACCOMPLISHED);
        assertThat(transaction.getVersion()).isEqualTo(1);
    }

    public void shouldTransferMoneyBetweenTwoAccountsWhenEnoughMoneyOnSrcAccount() throws InterruptedException {
        UUID srcAccountId = createAccount("oleg", 50);
        UUID destAccountId = createAccount("anna", 75);
        long cents = 25;

        Map<String, Object> params = new HashMap<>();
        params.put("srcAccountId", srcAccountId);
        params.put("destAccountId", destAccountId);
        params.put("cents", 25);

        HttpResponse resp = doPost(TransactionsEndPoint.URI, params);
        assertThatHttpResponse(resp).hasStatusCode(HttpServletResponse.SC_CREATED);

        Transaction transaction = JsonUtils.read(HttpUtils.getBodyAsString(resp), Transaction.class);
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionId()).isGreaterThan(0);
        assertThat(transaction.getSrcAccountId()).isEqualTo(srcAccountId);
        assertThat(transaction.getDestAccountId()).isEqualTo(destAccountId);
        assertThat(transaction.getCents()).isEqualTo(cents);
        assertThat(transaction.getStatus()).isEqualTo(Transaction.Status.ACCOMPLISHED);
        assertThat(transaction.getVersion()).isEqualTo(1);

        Account srcAccount = findAccountById(srcAccountId);
        assertThat(srcAccount).isNotNull();
        assertThat(srcAccount.getAccountId()).isEqualTo(srcAccountId);
        assertThat(srcAccount.getHolderName()).isEqualTo("oleg");
        assertThat(srcAccount.getCents()).isEqualTo(50 - cents);
        assertThat(srcAccount.getVersion()).isEqualTo(1);

        Account destAccount = findAccountById(destAccountId);
        assertThat(destAccount).isNotNull();
        assertThat(destAccount.getAccountId()).isEqualTo(destAccountId);
        assertThat(destAccount.getHolderName()).isEqualTo("anna");
        assertThat(destAccount.getCents()).isEqualTo(75 + cents);
        assertThat(destAccount.getVersion()).isEqualTo(1);
    }

}
