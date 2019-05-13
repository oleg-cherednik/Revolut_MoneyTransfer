package com.revolut.moneytransfer.dao;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDao {

    @NonNull
    private final DataSource dataSource;

    public final Connection getManualConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    public final Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

}
