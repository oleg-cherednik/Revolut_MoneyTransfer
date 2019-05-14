package com.revolut.moneytransfer.services;

import com.revolut.moneytransfer.dao.AccountDao;
import com.revolut.moneytransfer.dao.DataSourceLocator;
import com.revolut.moneytransfer.dao.TransactionDao;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceLocator {

    private static final ServiceLocator INSTANCE = new ServiceLocator();

    private final DataSourceLocator dataSourceLocator = DataSourceLocator.getInstance();
    private final AccountDao accountDao = dataSourceLocator.getAccountDao();
    private final TransactionDao transactionDao = dataSourceLocator.getTransactionDao();

    private final AccountService accountService = new AccountService(accountDao);
    private final TransactionService transactionService = new TransactionService(transactionDao, accountDao);

    @NonNull
    public static ServiceLocator getInstance() {
        return INSTANCE;
    }

}
