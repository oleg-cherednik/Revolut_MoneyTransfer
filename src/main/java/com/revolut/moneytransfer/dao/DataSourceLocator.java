package com.revolut.moneytransfer.dao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({ "SqlNoDataSourceInspection", "SqlResolve" })
public final class DataSourceLocator {

    private static final DataSourceLocator INSTANCE = new DataSourceLocator();

    private final DataSource dataSource = createDataSource();
    private final AccountDao accountDao = new AccountDao(dataSource);
    private final TransactionDao transactionDao = new TransactionDao(dataSource);

    @NonNull
    public static DataSourceLocator getInstance() {
        return INSTANCE;
    }

    @NonNull
    public static DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:~/rev;AUTO_SERVER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return init(dataSource);
    }

    @NonNull
    private static DataSource init(@NonNull DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement(SQL_CREATE_ACCOUNTS_TABLE).executeUpdate();
            conn.prepareStatement(SQL_CREATE_TRANSACTIONS_TABLE).executeUpdate();
            conn.prepareStatement(SQL_ADD_TRANSACTIONS_CONSTRAINT).executeUpdate();
            conn.prepareStatement(SQL_CREATE_SEQUENCE).executeUpdate();
        } catch(SQLException e) {
            log.error("Cannot init database", e);
        }

        return dataSource;
    }

    private static final String SQL_CREATE_ACCOUNTS_TABLE = "create table if not exists accounts(\n" +
            "account_id  varchar(36) primary key,\n" +
            "holder_name varchar(50),\n" +
            "cents       int not null default 0,\n" +
            "version     int not null default 0)";

    private static final String SQL_CREATE_TRANSACTIONS_TABLE = "create table if not exists transactions(\n" +
            "transaction_id  int primary key,\n" +
            "src_account_id  varchar(36) not null,\n" +
            "dest_account_id varchar(36) not null,\n" +
            "cents           int         not null check (cents > 0),\n" +
            "status          varchar(15) not null,\n" +
            "error_reason    varchar(255),\n" +
            "foreign key (src_account_id) references accounts (account_id),\n" +
            "foreign key (dest_account_id) references accounts (account_id))";

    private static final String SQL_ADD_TRANSACTIONS_CONSTRAINT = "alter table if exists transactions\n" +
            "add constraint if not exists transactions_src_dest_accounts_unique\n" +
            "check (src_account_id != dest_account_id)";

    private static final String SQL_CREATE_SEQUENCE = "create sequence if not exists transaction_id";

}
