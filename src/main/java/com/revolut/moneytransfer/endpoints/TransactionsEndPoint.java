package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.model.Transaction;
import com.revolut.moneytransfer.services.ServiceLocator;
import com.revolut.moneytransfer.utils.HttpUtils;
import com.revolut.moneytransfer.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Slf4j
public final class TransactionsEndPoint extends HttpServlet {

    private static final long serialVersionUID = 2599270104879778839L;

    public static final String URI = "/transactions";

    private static final Pattern TRANSACTIONS = Pattern.compile("(?i)^\\" + URI + "\\/(?<transactionId>\\d+)$");

    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();

    /**
     * Retrieve transaction status by given <tt>transactionId</tt>.<br>
     * In case of <tt>transactionId</tt> was not found, then return http status <tt>404 Not Found</tt>.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());

        Matcher matcher = TRANSACTIONS.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                long transactionId = Long.parseLong(matcher.group("transactionId"));
                Transaction transaction = serviceLocator.getTransactionService().findById(transactionId);

                if (transaction == null)
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json;charset=utf-8");
                    resp.getWriter().println(JsonUtils.write(transaction));
                }
            } catch(IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } catch(Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doGet(req, resp);
    }

    /**
     * Create new transfer for given <tt>transferId</tt> and execute it.<br>
     * Retrieve http status <tt>200 OK</tt> in case of transaction was successfully created. In the body, retrieves {@link Transaction.Status}. In
     * case of {@link Transaction.Status#ERROR}, it means that transfer was canceled (in most cases because optimistic lock exception).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());
        Matcher matcher = TRANSACTIONS.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                long transactionId = Long.parseLong(matcher.group("transactionId"));
                Transaction transaction = createTransaction(transactionId, HttpUtils.getBody(req), resp);

                if (transaction != null) {
                    Transaction.Status status = serviceLocator.getTransactionService().process(transaction);

                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("text/plain;charset=utf-8");
                    resp.getWriter().println(status);
                }
            } catch(Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doPost(req, resp);
    }

    private Transaction createTransaction(long transactionId, Map<String, Object> body, HttpServletResponse resp) {
        try {
            UUID srcAccountId = UUID.fromString((String)body.get("srcAccountId"));
            UUID destAccountId = UUID.fromString((String)body.get("destAccountId"));
            int cents = ((Number)body.get("cents")).intValue();

            return serviceLocator.getTransactionService().create(transactionId, srcAccountId, destAccountId, cents);
        } catch(IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch(SQLException e) {
            if (SQL_STATES.contains(e.getSQLState()))
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch(Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    private static final String SQL_STATE_FOREIGN_KEY_VIOLATION = "23506";
    private static final String SQL_STATE_CHECK_VIOLATION = "23513";
    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private static final Set<String> SQL_STATES =
            new HashSet<>(Arrays.asList(SQL_STATE_FOREIGN_KEY_VIOLATION, SQL_STATE_CHECK_VIOLATION, SQL_STATE_UNIQUE_VIOLATION));
}
