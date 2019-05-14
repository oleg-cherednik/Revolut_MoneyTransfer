package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.model.Account;
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
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Slf4j
public final class AccountsEndPoint extends HttpServlet {

    private static final long serialVersionUID = -5445784765978037591L;

    public static final String URI = "/accounts";

    private static final Pattern ACCOUNTS_FIND_BY_ID =
            Pattern.compile("(?i)^\\" + URI + "\\/(?<accountId>[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$");
    private static final Pattern ACCOUNTS_CREATE = Pattern.compile("(?i)^\\" + URI + '$');

    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();

    /**
     * Find account by <tt>accountId</tt>.<br>
     * Retrieve http status <tt>200 OK</tt> if account found and account json in the response's body or <tt>404 Not Found</tt> if not found.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());

        Matcher matcher = ACCOUNTS_FIND_BY_ID.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                UUID accountId = UUID.fromString(matcher.group("accountId"));
                Account account = serviceLocator.getAccountService().findById(accountId);

                if (account == null)
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json;charset=utf-8");
                    resp.getWriter().println(JsonUtils.write(account));
                }
            } catch(SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doGet(req, resp);
    }

    /**
     * Create new account.<br>
     * Optionally query param <tt>holderName</tt> could be set.<br>
     * Retrieve http status <tt>201 Created</tt> and unique generated <tt>accountId</tt> in the response's body.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());

        Matcher matcher = ACCOUNTS_CREATE.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                Map<String, Object> body = HttpUtils.getBody(req);
                String holderName = (String)body.get("holderName");
                int cents = Integer.parseInt(String.valueOf(body.getOrDefault("cents", 0)));
                Account account = serviceLocator.getAccountService().create(holderName, cents);

                resp.setContentType("text/plain;charset=utf-8");
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println(account.getAccountId());
            } catch(IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } catch(SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doPost(req, resp);
    }
}
