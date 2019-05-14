package com.revolut.moneytransfer.dao;

import com.revolut.moneytransfer.model.Transaction;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
public final class TransactionDao extends BaseDao {

    private static final String SQL_NEXT_TRANSACTION_ID = "select nextval('transaction_id')";
    private static final String SQL_FIND_BY_ID = "select * from transactions where transaction_id = ?";
    private static final String SQL_INSERT = "insert into transactions(transaction_id, src_account_id, dest_account_id, cents, status) values (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_STATUS = "update transactions set status = ?, error_reason = ? where transaction_id = ?";

    TransactionDao(@NonNull DataSource dataSource) {
        super(dataSource);
    }

    public long getNextTransactionId() throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(SQL_NEXT_TRANSACTION_ID);
        ResultSet rs = ps.executeQuery();

        if (!rs.next())
            throw new SQLException("Cannot generate next transactionId");

        return rs.getLong(1);
    }

    public Transaction findById(long transactionId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(SQL_FIND_BY_ID);
        ps.setLong(1, transactionId);
        ResultSet rs = ps.executeQuery();

        if (!rs.next())
            return null;

        UUID srcAccountId = UUID.fromString(rs.getString("src_account_id"));
        UUID destAccountId = UUID.fromString(rs.getString("dest_account_id"));
        int cents = rs.getInt("cents");

        Transaction transaction = new Transaction(transactionId, srcAccountId, destAccountId, cents);
        transaction.setStatus(Transaction.Status.valueOf(rs.getString("status")));
        transaction.setErrorReason(rs.getString("error_reason"));

        return transaction;
    }

    public Transaction save(@NonNull UUID srcAccountId, @NonNull UUID destAccountId, int cents) throws SQLException {
        long transactionId = getNextTransactionId();

        PreparedStatement ps = getConnection().prepareStatement(SQL_INSERT);
        ps.setLong(1, transactionId);
        ps.setString(2, String.valueOf(srcAccountId));
        ps.setString(3, String.valueOf(destAccountId));
        ps.setLong(4, cents);
        ps.setString(5, Transaction.Status.IN_PROGRESS.name());

        if (ps.executeUpdate() == 0)
            throw new SQLException("Cannot create new account");
        else
            return new Transaction(transactionId, srcAccountId, destAccountId, cents);
    }

    public boolean setErrorStatus(long transactionId, String errorReason) throws SQLException {
        return updateStatus(transactionId, Transaction.Status.ERROR, errorReason, null) == 1;
    }

    public boolean setAccomplishedStatus(long transactionId, @NonNull Connection conn) throws SQLException {
        return updateStatus(transactionId, Transaction.Status.ACCOMPLISHED, null, conn) == 1;
    }

    private int updateStatus(long transactionId, @NonNull Transaction.Status status, String errorReason, Connection conn)
            throws SQLException {
        if (conn == null)
            conn = getConnection();

        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS);
        ps.setString(1, status.name());
        ps.setString(2, errorReason);
        ps.setLong(3, transactionId);
        return ps.executeUpdate();
    }

}
