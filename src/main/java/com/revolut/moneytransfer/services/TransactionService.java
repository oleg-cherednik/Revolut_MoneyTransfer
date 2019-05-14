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

    public void setErrorStatus(long transactionId, String errorReason) throws SQLException {
        transactionDao.setErrorStatus(transactionId, errorReason);
        log.error("transactionId='{}': {}", transactionId, errorReason);
    }

    public Transaction.Status process(@NonNull Transaction transaction) throws SQLException, InterruptedException {
        log.debug("transactionId='{}': processing", transaction.getTransactionId());

        int cents = transaction.getCents();
        Account src = accountDao.findById(transaction.getSrcAccountId());

        assert src != null : "accountId='" + transaction.getSrcAccountId() + "' was not found";

        if (src.getCents() < cents) {
            String errorReason = "Account '" + src.getAccountId() + "' does not have enough cents: required '" + cents + "' cents";
            setErrorStatus(transaction.getTransactionId(), errorReason);
        } else if (!transferCentsWithTransaction(transaction, src))
            setErrorStatus(transaction.getTransactionId(), "Optimistic lock exception");

        return Objects.requireNonNull(transactionDao.findById(transaction.getTransactionId())).getStatus();
    }

    @SuppressWarnings("AssignmentReplaceableWithOperatorAssignment")
    private boolean transferCentsWithTransaction(Transaction transaction, Account srcAccount) throws SQLException, InterruptedException {
        try (Connection conn = transactionDao.getManualConnection()) {
            log.debug("transactionId='{}': start transfer", transaction.getTransactionId());

            long transactionId = transaction.getTransactionId();
            UUID srcAccountId = transaction.getSrcAccountId();
            UUID destAccountId = transaction.getDestAccountId();
            int cents = transaction.getCents();

            Account destAccount = Objects.requireNonNull(accountDao.findById(destAccountId));

            boolean success = accountDao.setCents(srcAccountId, srcAccount.getVersion(), srcAccount.getCents() - cents, conn);
            success = success && accountDao.setCents(destAccountId, destAccount.getVersion(), destAccount.getCents() + cents, conn);
            success = success && transactionDao.setAccomplishedStatus(transactionId, conn);

            if (success) {
                log.debug("transactionId='{}': accomplished", transaction.getTransactionId());
                conn.commit();
                return true;
            }

            log.error("transactionId='{}': accounts or transaction objects have been changed (postpone)", transaction.getTransactionId());
            conn.rollback();

            log.debug("transactionId='{}': wait for 500ms before retry", transaction.getTransactionId());
            Thread.sleep(500);

            return false;
        }
    }

}
