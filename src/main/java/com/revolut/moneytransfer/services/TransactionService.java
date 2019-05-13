package com.revolut.moneytransfer.services;

import com.revolut.moneytransfer.dao.AccountDao;
import com.revolut.moneytransfer.dao.TransactionDao;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Transaction;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Slf4j
public final class TransactionService {

    private static final int RETRY_COUNT = 5;

    private final TransactionDao transactionDao;
    private final AccountDao accountDao;

    TransactionService(@NonNull TransactionDao transactionDao, @NonNull AccountDao accountDao) {
        this.transactionDao = transactionDao;
        this.accountDao = accountDao;
    }

    public Transaction findById(long transactionId) throws SQLException {
        return transactionDao.findById(transactionId);
    }

    public Transaction create(@NonNull UUID srcAccountId, @NonNull UUID destAccountId, int cents) throws SQLException {
        return transactionDao.save(srcAccountId, destAccountId, cents);
    }

    public Transaction process(long transactionId) throws SQLException {
        for (int i = 1; i <= RETRY_COUNT; i++) {
            Transaction transaction = transactionDao.findById(transactionId);

            if (transaction == null)
                throw new IllegalArgumentException("transactionId='" + transactionId + "' was not found");

            if (process(transaction, i))
                break;
        }

        return transactionDao.findById(transactionId);
    }

    private boolean process(@NonNull Transaction transaction, int retryCount) throws SQLException {
        log.debug("transactionId='{}': processing (retryCount={})", transaction.getTransactionId(), retryCount);

        int cents = transaction.getCents();
        Account src = accountDao.findById(transaction.getSrcAccountId());

        assert src != null : "accountId='" + transaction.getSrcAccountId() + "' was not found";

        if (src.getCents() >= cents)
            return transferCentsWithTransaction(transaction, src);

        String errorReason = "Account '" + src.getAccountId() + "' does not have enough cents: required '" + cents + "' cents";
        boolean statusUpdated = transactionDao.setErrorStatus(transaction.getTransactionId(), transaction.getVersion(), errorReason);

        if (statusUpdated) {
            log.error("transactionId='{}': {}", transaction.getTransactionId(), errorReason);
            return true;
        }

        log.error("transactionId='{}': transaction objects has been changed (postpone)", transaction.getTransactionId());
        return false;
    }

    @SuppressWarnings("AssignmentReplaceableWithOperatorAssignment")
    private boolean transferCentsWithTransaction(Transaction transaction, Account srcAccount) throws SQLException {
        try (Connection conn = transactionDao.getManualConnection()) {
            log.debug("transactionId='{}': start transfer", transaction.getTransactionId());

            long transactionId = transaction.getTransactionId();
            UUID srcAccountId = transaction.getSrcAccountId();
            UUID destAccountId = transaction.getDestAccountId();
            int cents = transaction.getCents();

            Account destAccount = Objects.requireNonNull(accountDao.findById(destAccountId));

            boolean success = accountDao.setCents(srcAccountId, srcAccount.getVersion(), srcAccount.getCents() - cents, conn);
            success = success && accountDao.setCents(destAccountId, destAccount.getVersion(), destAccount.getCents() + cents, conn);
            success = success && transactionDao.setAccomplishedStatus(transactionId, transaction.getVersion(), conn);

            if (success) {
                log.debug("transactionId='{}': accomplished", transaction.getTransactionId());
                conn.commit();
                return true;
            }

            log.error("transactionId='{}': accounts or transaction objects have been changed (postpone)", transaction.getTransactionId());
            conn.rollback();
            return false;
        }
    }

}
