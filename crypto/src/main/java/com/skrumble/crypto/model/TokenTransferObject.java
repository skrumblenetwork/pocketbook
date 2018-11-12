package com.skrumble.crypto.model;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

public class TokenTransferObject {
    private String transactionHash = "";

    private long transactionDate = -1;

    // amount
    private double amount = 0;
    private long gasPrice = 5;
    private double balance = 0;
    private long gasLimit = 0;

    // sender
    private String senderAddress = "";
    private String senderId = "";

    // recipient
    private String recipientAddress = "";
    private String recipientId = "";

    private String errorMessage = "";

    public TokenTransferObject() {
        super();
        gasLimit = 21000;
    }

    public TokenTransferObject(double amount, String toAddress) {
        this();
        this.amount = amount;
        this.recipientAddress = toAddress;
    }

// *********************************************************************************************
// region Utility

    public String getAmountWithFraction(){
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }

// endregion

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// region Getter & Setter

    @NonNull
    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(@NonNull String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(long transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    // endregion
}