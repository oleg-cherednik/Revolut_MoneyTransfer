package com.revolut.moneytransfer.dao;

import com.revolut.moneytransfer.model.Account;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
public final class AccountDao extends BaseDao {

    private static final String SQL_INSERT = "insert into accounts(account_id, holder_name, cents) values (?, ?, ?)";
    private static final String SQL_FIND_BY_ID = "select * from accounts where account_id = ?";
    private static final String SQL_UPDATE_CENTS = "update accounts set cents = ?, version = ? where account_id = ? and version = ?";

    AccountDao(@NonNull DataSource dataSource) {
        super(dataSource);
    }

    public Account save(String holderName, long cents) throws SQLException {
        UUID accountId = UUID.randomUUID();

        PreparedStatement ps = getConnection().prepareStatement(SQL_INSERT);
        ps.setString(1, String.valueOf(accountId));
        ps.setString(2, holderName);
        ps.setLong(3, cents);

        if (ps.executeUpdate() == 0)
            throw new SQLException("Cannot create new account");
        else
            return new Account(accountId, holderName);
    }

    public Account findById(@NonNull UUID accountId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(SQL_FIND_BY_ID);
        ps.setString(1, String.valueOf(accountId));
        ResultSet rs = ps.executeQuery();

        if (!rs.next())
            return null;

        Account account = new Account(accountId, rs.getString("holder_name"));
        account.setCents(rs.getLong("cents"));
        account.setVersion(rs.getLong("version"));

        return account;
    }

    public boolean setCents(@NonNull UUID accountId, long version, long cents, @NonNull Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_CENTS);
        ps.setLong(1, cents);
        ps.setLong(2, version + 1);
        ps.setString(3, String.valueOf(accountId));
        ps.setLong(4, version);
        return ps.executeUpdate() == 1;
    }

}
