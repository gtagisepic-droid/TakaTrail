package com.takatrail;

/** Checked exception used when user-entered financial data is invalid. */
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
