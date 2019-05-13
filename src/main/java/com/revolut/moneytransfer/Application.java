package com.revolut.moneytransfer;

import com.revolut.moneytransfer.endpoints.AccountsEndPoint;
import com.revolut.moneytransfer.endpoints.TransactionsEndPoint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Application {

    private static final int PORT = 8080;

    public static void main(String... args) throws Exception {
        startJetty();
    }

    private static void startJetty() throws Exception {
        Server server = new Server(PORT);
        server.setHandler(createServletHandler());
        server.start();
        server.join();
    }

    public static ServletHandler createServletHandler() {
        ServletHandler servletHandler = new ServletHandler();

        servletHandler.addServletWithMapping(AccountsEndPoint.class, AccountsEndPoint.URI + "/*");
        servletHandler.addServletWithMapping(TransactionsEndPoint.class, TransactionsEndPoint.URI + "/*");

        return servletHandler;
    }
}
