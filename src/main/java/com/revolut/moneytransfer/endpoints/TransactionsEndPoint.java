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

    private static final Pattern TRANSACTIONS_FIND_BY_ID = Pattern.compile("(?i)^\\" + URI + "\\/(?<transactionId>\\d+)$");
    private static final Pattern TRANSACTIONS_CREATE = Pattern.compile("(?i)^\\" + URI + '$');

    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();

    /**
     * Retrieve transaction status by given <tt>transactionId</tt>. In case of <tt>transactionId</tt> was not found, then return http status <tt>404
     * Not Found</tt>.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());

        Matcher matcher = TRANSACTIONS_FIND_BY_ID.matcher(req.getRequestURI());

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());
        Matcher matcher = TRANSACTIONS_CREATE.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                Map<String, Object> body = HttpUtils.getBody(req);

                UUID srcAccountId = UUID.fromString((String)body.get("srcAccountId"));
                UUID destAccountId = UUID.fromString((String)body.get("destAccountId"));
                int cents = ((Number)body.get("cents")).intValue();

                Transaction transaction = serviceLocator.getTransactionService().create(srcAccountId, destAccountId, cents);
                transaction = serviceLocator.getTransactionService().process(transaction.getTransactionId());

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json;charset=utf-8");
                resp.getWriter().println(JsonUtils.write(transaction));
            } catch(IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } catch(SQLException e) {
                if (SQL_STATES.contains(e.getSQLState()))
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                else
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch(Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doPost(req, resp);
    }

    private static final String SQL_STATE_FOREIGN_KEY_VIOLATION = "23506";
    private static final String SQL_STATE_CHECK_VIOLATION = "23513";
    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private static final Set<String> SQL_STATES =
            new HashSet<>(Arrays.asList(SQL_STATE_FOREIGN_KEY_VIOLATION, SQL_STATE_CHECK_VIOLATION, SQL_STATE_UNIQUE_VIOLATION));
}
