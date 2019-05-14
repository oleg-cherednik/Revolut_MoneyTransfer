package com.revolut.moneytransfer.endpoints;

import com.revolut.moneytransfer.services.ServiceLocator;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Slf4j
public final class GenerateTransactionIdEndPoint extends HttpServlet {

    private static final long serialVersionUID = -3753014491758635736L;

    public static final String URI = "/transactions/newId";

    private static final Pattern TRANSACTION_ID_CREATE = Pattern.compile("(?i)^\\" + URI + '$');

    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();

    /**
     * Generate new <tt>transactionId</tt> for a new transfer. This <tt>transactionId</tt> could be used only once.<br>
     * New <tt>transactionId</tt> should be generated each time before new transfer create.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug(req.toString());

        Matcher matcher = TRANSACTION_ID_CREATE.matcher(req.getRequestURI());

        if (matcher.matches()) {
            try {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/plain;charset=utf-8");
                resp.getWriter().println(serviceLocator.getTransactionService().generateTransactionId());
            } catch(SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else
            super.doPost(req, resp);
    }
}
