package com.revolut.moneytransfer.services;

import com.revolut.moneytransfer.dao.AccountDao;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AccountService {

    private final AccountDao dao;

    public Account create(String holderName, long cents) throws SQLException {
        centsShouldNotBeNegative(cents);

        holderName = StringUtils.isBlank(holderName) ? null : holderName.trim();
        return dao.save(holderName, cents);
    }

    public Account findById(@NonNull UUID accountId) throws SQLException {
        return dao.findById(accountId);
    }

    private static void centsShouldNotBeNegative(long cents)  {
        if (cents < 0)
            throw new IllegalArgumentException("Amount of cents should not be negative: cents='" + cents + '\'');
    }

}
