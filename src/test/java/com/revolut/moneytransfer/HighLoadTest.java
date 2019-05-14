package com.revolut.moneytransfer;

import com.revolut.moneytransfer.endpoints.TransactionsEndPoint;
import com.revolut.moneytransfer.model.Account;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.revolut.moneytransfer.assertj.CustomAssertions.assertThatHttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.05.2019
 */
@Test(groups = "it")
public class HighLoadTest extends JettyTestGroup {

    private static final int ATTEMPTS = 500;
    private static final int THREADS = 20;

    public void shouldNotLooseCentsWhenTransferBetweenAccountsInParallel() throws InterruptedException {
        long oneCents = 500;
        long twoCents = 500;
        UUID one = createAccount("oleg", oneCents);
        UUID two = createAccount("anna", twoCents);
        Random random = new Random();

        List<Callable<Void>> tasks = IntStream.range(0, ATTEMPTS)
                                              .mapToObj(i -> transferTask(one, two, 50, random))
                                              .collect(Collectors.toList());

        ExecutorService service = Executors.newFixedThreadPool(THREADS);
        service.invokeAll(tasks);

        Account oneAccount = findAccountById(one);
        Account twoAccount = findAccountById(two);
        assertThat(oneAccount).isNotNull();
        assertThat(twoAccount).isNotNull();
        assertThat(oneAccount.getVersion()).isEqualTo(twoAccount.getVersion());
        assertThat(oneAccount.getVersion()).isGreaterThanOrEqualTo(0);
        assertThat(twoAccount.getVersion()).isGreaterThanOrEqualTo(0);
        assertThat(oneAccount.getCents() + twoAccount.getCents()).isEqualTo(oneCents + twoCents);
    }

    private static Callable<Void> transferTask(UUID one, UUID two, int maxCents, Random random) {
        boolean oneToTwo = random.nextBoolean();
        UUID src = oneToTwo ? one : two;
        UUID dest = oneToTwo ? two : one;
        int cents = getRandomPositiveCents(maxCents, random);

        return () -> {
            Map<String, Object> params = new HashMap<>();
            params.put("srcAccountId", src);
            params.put("destAccountId", dest);
            params.put("cents", cents);

            HttpResponse resp = doPost(TransactionsEndPoint.URI + '/' + getTransactionId(), params);
            assertThatHttpResponse(resp).hasNotStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return null;
        };
    }

    private static int getRandomPositiveCents(int maxCents, Random random) {
        int cents = 0;

        while (cents == 0)
            cents = random.nextInt(maxCents);

        return cents;
    }

}
