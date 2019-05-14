package com.revolut.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 10.05.2019
 */
@Getter
@Setter
public class Transaction {

    private final long transactionId;
    @NonNull
    private final UUID srcAccountId;
    @NonNull
    private final UUID destAccountId;
    private final int cents;
    @NonNull
    private Status status = Status.IN_PROGRESS;
    private String errorReason;

    @JsonCreator
    public Transaction(
            @JsonProperty("transactionId") long transactionId,
            @NonNull @JsonProperty("srcAccountId") UUID srcAccountId,
            @NonNull @JsonProperty("destAccountId") UUID destAccountId,
            @JsonProperty("cents") int cents) {
        this.transactionId = transactionId;
        this.srcAccountId = srcAccountId;
        this.destAccountId = destAccountId;
        this.cents = cents;
    }

    public enum Status {
        IN_PROGRESS,
        ACCOMPLISHED,
        ERROR
    }

}
