package com.revolut.moneytransfer.services;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */

import com.revolut.moneytransfer.dao.DataSourceLocator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceLocator {

    private static final ServiceLocator INSTANCE = new ServiceLocator();

    private final DataSourceLocator dataSourceLocator = DataSourceLocator.getInstance();
    private final AccountService accountService = new AccountService(dataSourceLocator.getAccountDao());
    private final TransactionService transactionService =
            new TransactionService(dataSourceLocator.getTransactionDao(), dataSourceLocator.getAccountDao());

    public static ServiceLocator getInstance() {
        return INSTANCE;
    }

}
