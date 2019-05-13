package com.revolut.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 12.05.2019
 */
@Getter
@Setter
public final class Account {

    private final UUID accountId;
    private final String holderName;
    private long cents;
    private long version;

    @JsonCreator
    public Account(@JsonProperty("accountId") UUID accountId, @JsonProperty("holderName") String holderName) {
        this.accountId = accountId;
        this.holderName = holderName;
    }
}
