package com.skrumble.crypto;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;

public class WalletUtils {

    public static String generateTwelveWord(){
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);

        return sb.toString();
    }

    public static Credentials createCredentials(String twelveWord, String password){
        try {
            DeterministicSeed seed = new DeterministicSeed(twelveWord, null, password, 1409478661L);

            DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();

            List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");

            DeterministicKey key = chain.getKeyByPath(keyPath, true);

            String privateKey = Numeric.toHexStringNoPrefix(key.getPrivKey());

            // Web3
            Credentials credentials = Credentials.create(privateKey);

            return credentials;

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static File generateWalletFile(String directoryPath, String password, String privateKey){
        try {
            Credentials credentials = Credentials.create(privateKey);

            WalletFile wallet = Wallet.createLight(password, credentials.getEcKeyPair());
            String fileName = getWalletFileName(credentials);
            File destination = new File(directoryPath, fileName);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(destination, wallet);

            return destination;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getWalletFileName(Credentials credentials) {
        return credentials.getAddress() + ".json";
    }

    public Credentials restoreUsingFile(File file, String password){
        try {
            return org.web3j.crypto.WalletUtils.loadCredentials(password, file);
        } catch (IOException | CipherException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateSignature(String messageToSign, String privateKey){
        try {

            messageToSign = Numeric.toHexStringNoPrefix(messageToSign.getBytes());

            byte[] messageData = Numeric.hexStringToByteArray("0x" + messageToSign);

            String prefix = "\u0019Ethereum Signed Message:\n" + messageData.length;
            byte[] prefixData;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                prefixData = prefix.getBytes(StandardCharsets.US_ASCII);
            }else {
                prefixData = prefix.getBytes();
            }

            ByteArrayOutputStream wholeMsgStream = new ByteArrayOutputStream();

            wholeMsgStream.write(prefixData);
            wholeMsgStream.write(messageData);

            byte[] wholeMsg = wholeMsgStream.toByteArray();

            Credentials credentials = Credentials.create(privateKey);

            Sign.SignatureData signatureData = Sign.signMessage(wholeMsg, credentials.getEcKeyPair());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(signatureData.getR());
            outputStream.write(signatureData.getS());
            outputStream.write(signatureData.getV());

            byte[] result = outputStream.toByteArray();

            return Numeric.toHexString(result);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
