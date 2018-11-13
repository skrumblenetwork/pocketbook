package com.skrumble.crypto;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;
import com.skrumble.crypto.model.TokenTransferObject;
import com.skrumble.crypto.public_interface.OnCompletion;
import com.skrumble.crypto.utils.Function;
import com.skrumble.crypto.utils.FunctionEncoder;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static com.skrumble.crypto.Config.BLOCK_CHAIN_ADDRESS;
import static com.skrumble.crypto.Config.TOKEN_CONTRACT;

public class Web3jHandler {

    private static Web3jHandler sInstance;
    private Web3j web3;
    private Credentials credentials;
    private RemoteCall<TransactionReceipt> transactionReceipt;

    private Web3jHandler() {
        web3 = Web3jFactory.build(new HttpService(BLOCK_CHAIN_ADDRESS));
    }

    public static Web3jHandler getInstance() {
        if (sInstance == null) {
            sInstance = new Web3jHandler();
        }

        return sInstance;
    }

    public void loadCredentials(String privateKey) throws IOException, CipherException {
        credentials = Credentials.create(privateKey);
    }

    public String getWalletAddress() {
        return credentials.getAddress();
    }

    public RemoteCall<TransactionReceipt> transaction(String address, double ethBalance) throws InterruptedException, IOException, TransactionException {
        return transactionReceipt = Transfer.sendFunds(web3, credentials, address, BigDecimal.valueOf(ethBalance), Convert.Unit.ETHER);
    }

    // *********************************************************************************************
    // region Balance

    public void getBalance(@NonNull final OnCompletion<BigDecimal> completion) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    EthGetBalance ethGetBalance = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

                    if (ethGetBalance.hasError()) {
                        completion.onCompleted(false, BigDecimal.ZERO);
                        return;
                    }

                    BigInteger balanceInBigInteger = ethGetBalance.getBalance();

                    BigDecimal balanceInBigDecimal = new BigDecimal(balanceInBigInteger);

                    BigDecimal balance = Convert.fromWei(balanceInBigDecimal, Convert.Unit.ETHER);

                    completion.onCompleted(true, balance);

                } catch (Exception e) {
                    e.printStackTrace();
                    completion.onCompleted(false, BigDecimal.ZERO);
                }
            }
        });
    }

    public void getErc20Balance(@NonNull final OnCompletion<BigDecimal> completion) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = encodeBalanceData(credentials.getAddress());

                    Transaction ethCallTransaction = Transaction.createEthCallTransaction(credentials.getAddress(), Config.TOKEN_CONTRACT, data);

                    EthCall ethCall;
                    try {
                        ethCall = web3.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).sendAsync().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();

                        completion.onCompleted(false, BigDecimal.ZERO);
                        return;
                    }

                    if (ethCall.hasError()) {
                        completion.onCompleted(false, BigDecimal.ZERO);
                        return;
                    }

                    String valueInHax = ethCall.getValue();

                    BigInteger valueInWei = Numeric.decodeQuantity(valueInHax);

                    BigDecimal decimal = new BigDecimal(valueInWei);

                    BigDecimal balance = Convert.fromWei(decimal, Convert.Unit.ETHER);

                    completion.onCompleted(true, balance);

                } catch (Exception e) {
                    e.printStackTrace();
                    completion.onCompleted(false, BigDecimal.ZERO);
                }
            }
        });
    }

    // endregion

    // *********************************************************************************************
    // region Estimate Gas

    public void estimateGas(final TokenTransferObject tokenTransferObject, @NonNull final OnCompletion<TokenTransferObject> completion) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {

                    Transaction transaction = Transaction.createEthCallTransaction(credentials.getAddress(), tokenTransferObject.getRecipientAddress(), null);

                    EthEstimateGas ethEstimateGas = web3.ethEstimateGas(transaction).sendAsync().get();

                    if (ethEstimateGas.hasError()) {
                        tokenTransferObject.setErrorMessage(ethEstimateGas.getError().getMessage());
                        completion.onCompleted(false, tokenTransferObject);
                        return;
                    }

                    BigInteger gasLimit = ethEstimateGas.getAmountUsed();

                    tokenTransferObject.setGasLimit(gasLimit.longValue());

                    completion.onCompleted(true, tokenTransferObject);

                } catch (Exception e) {
                    e.printStackTrace();
                    tokenTransferObject.setErrorMessage(e.getLocalizedMessage());
                    completion.onCompleted(false, tokenTransferObject);
                }
            }
        });
    }

    public void estimateGasForERC20Token(final TokenTransferObject tokenTransferObject, @NonNull final OnCompletion<TokenTransferObject> completion) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                try {
                    String data = encodeTransferData(tokenTransferObject.getRecipientAddress(), Convert.toWei(BigDecimal.valueOf(tokenTransferObject.getAmount()), Convert.Unit.ETHER).toBigInteger());

                    Transaction ethCallTransaction = Transaction.createEthCallTransaction(credentials.getAddress(), TOKEN_CONTRACT, data);

                    EthEstimateGas ethEstimateGas = web3.ethEstimateGas(ethCallTransaction).sendAsync().get();

                    if (ethEstimateGas.hasError()) {
                        tokenTransferObject.setErrorMessage(ethEstimateGas.getError().getMessage());
                        completion.onCompleted(false, tokenTransferObject);
                        return;
                    }

                    BigInteger gasLimit = ethEstimateGas.getAmountUsed();

                    tokenTransferObject.setGasLimit(gasLimit.longValue());

                    completion.onCompleted(true, tokenTransferObject);

                } catch (Exception e) {
                    e.printStackTrace();
                    tokenTransferObject.setErrorMessage(e.getLocalizedMessage());
                    completion.onCompleted(false, tokenTransferObject);
                }
            }
        });
    }

    // endregion

    // *********************************************************************************************
    // region Gas limit after Transaction

    public void getUsedGasInTransaction(final TokenTransferObject tokenTransferObject, @NonNull final OnCompletion<TokenTransferObject> completion) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    EthGetTransactionReceipt ethGetTransactionReceipt = web3.ethGetTransactionReceipt(tokenTransferObject.getTransactionHash()).sendAsync().get();

                    if (ethGetTransactionReceipt.hasError()) {
                        tokenTransferObject.setErrorMessage(ethGetTransactionReceipt.getError().getMessage());
                        completion.onCompleted(false, tokenTransferObject);
                        return;
                    }

                    TransactionReceipt receipt = ethGetTransactionReceipt.getResult();

                    if (receipt == null) {
                        tokenTransferObject.setErrorMessage(ethGetTransactionReceipt.getError().getMessage());
                        completion.onCompleted(false, tokenTransferObject);
                        return;
                    }

                    BigInteger gasUsed = receipt.getGasUsed();
                    tokenTransferObject.setGasLimit(gasUsed.longValue());

                    completion.onCompleted(true, tokenTransferObject);

                } catch (Exception e) {
                    e.printStackTrace();

                    tokenTransferObject.setErrorMessage(e.getLocalizedMessage());
                    completion.onCompleted(false, tokenTransferObject);
                }
            }
        });
    }

    // endregion

    // *********************************************************************************************
    // region SendToken

    @SuppressLint("StaticFieldLeak")
    public void sendToken(final String address, final double value, final long gasPrice, @NonNull final OnCompletion<String> completion) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... voids) {
                try {
                    Transaction ethCallTransaction = Transaction.createEthCallTransaction(credentials.getAddress(), address, null);

                    // Estimate Gas Limit
                    EthEstimateGas ethEstimateGas = web3.ethEstimateGas(ethCallTransaction).sendAsync().get();
                    if (ethEstimateGas.hasError()) {
                        completion.onCompleted(false, ethEstimateGas.getError().getMessage());
                        return null;
                    }

                    BigInteger gasLimit = ethEstimateGas.getAmountUsed();

                    // Nonce
                    EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
                    if (ethGetTransactionCount.hasError()) {
                        completion.onCompleted(false, ethGetTransactionCount.getError().getMessage());
                        return null;
                    }
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                    // Gas Price
                    BigInteger gasp = BigInteger.valueOf(gasPrice);

                    // Create Row Transaction
                    BigDecimal valueInSKM = Convert.toWei(BigDecimal.valueOf(value), Convert.Unit.ETHER);
                    RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasp, gasLimit, address, valueInSKM.toBigInteger());

                    // Create Transaction manager
                    RawTransactionManager rawTransactionManager = new RawTransactionManager(web3, credentials, (byte) 9011);
                    EthSendTransaction ethSendTransaction = rawTransactionManager.signAndSend(rawTransaction);

                    if (ethSendTransaction.hasError()) {
                        completion.onCompleted(false, ethSendTransaction.getError().getMessage());
                        return null;
                    }

                    boolean success = Strings.isNullOrEmpty(ethSendTransaction.getTransactionHash()) == false;
                    completion.onCompleted(success, success ? ethSendTransaction.getTransactionHash() : null);

                } catch (Exception e) {
                    e.printStackTrace();
                    completion.onCompleted(false, e.getLocalizedMessage());
                }

                return null;
            }
        }.execute(1, 2);
    }

    public void sendERC20Token(final String toAddress, final double value, final long gasPrice, @NonNull final OnCompletion<String> completion) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = encodeTransferData(toAddress, Convert.toWei(BigDecimal.valueOf(value), Convert.Unit.ETHER).toBigInteger());

                    Transaction ethCallTransaction = Transaction.createEthCallTransaction(credentials.getAddress(), Config.TOKEN_CONTRACT, data);

                    // get gas balance
                    EthEstimateGas ethEstimateGas = web3.ethEstimateGas(ethCallTransaction).sendAsync().get();
                    if (ethEstimateGas.hasError()) {
                        completion.onCompleted(false, ethEstimateGas.getError().getMessage());
                        return;
                    }

                    // Prepare transaction
                    RawTransactionManager transactionManager = new RawTransactionManager(web3, credentials);
                    EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
                    if (ethGetTransactionCount.hasError()) {
                        completion.onCompleted(false, ethGetTransactionCount.getError().getMessage());
                        return;
                    }

                    // gas price and gas limit
                    BigInteger gasp = Convert.toWei(BigDecimal.valueOf(gasPrice), Convert.Unit.GWEI).toBigInteger();
                    BigInteger gasLimit = ethEstimateGas.getAmountUsed();

                    // Send transaction and get receipt
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                    RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasp, gasLimit, Config.TOKEN_CONTRACT, data);
                    EthSendTransaction ethSendTransaction = transactionManager.signAndSend(rawTransaction);
                    String transactionHash = ethSendTransaction.getTransactionHash();

                    boolean success = (Strings.isNullOrEmpty(transactionHash) == false);
                    completion.onCompleted(success, success == false ? ethSendTransaction.getError().getMessage() : transactionHash);

                } catch (Exception e) {
                    e.printStackTrace();
                    completion.onCompleted(false, e.getLocalizedMessage());
                }
            }
        });
    }

    // endregion

    // *********************************************************************************************
    // region data function

    private static String encodeTransferData(String toAddress, BigInteger sum) {
        Function function = new Function(
                "transfer",  // function we're calling
                Arrays.<Type>asList(new Address(toAddress), new Uint256(sum)));
        return FunctionEncoder.encode(function);
    }

    private static String encodeBalanceData(String toAddress) {
        Function function = new Function(
                "balanceOf",  // function we're calling
                Collections.<Type>singletonList(new Address(toAddress)));
        return FunctionEncoder.encode(function);
    }

    // endregion

}