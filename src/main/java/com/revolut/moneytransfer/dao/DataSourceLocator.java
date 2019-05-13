package com.revolut.moneytransfer.dao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceLocator {

    private static final DataSourceLocator INSTANCE = new DataSourceLocator();

    private final DataSource dataSource = createDataSource();
    private final AccountDao accountDao = new AccountDao(dataSource);
    private final TransactionDao transactionDao = new TransactionDao(dataSource);


    private static PGSimpleDataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("revolut");
        dataSource.setUser("postgres");
        dataSource.setPassword("admin");
        return dataSource;
    }

    public static DataSourceLocator getInstance() {
        return INSTANCE;
    }

}
