package com.skrumble.skmcrypto;

import android.net.Credentials;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skrumble.crypto.WalletUtils;
import com.skrumble.crypto.Web3jHandler;
import com.skrumble.crypto.public_interface.OnCompletion;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    Button generateWords, generatePrivateKey, generaWalletAddress;
    TextView wordsTextView, privateKeyTextView, walletTextView;

    String words, privateKey, wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateWords = findViewById(R.id.generate_words);
        generatePrivateKey = findViewById(R.id.generate_private_key);
        generaWalletAddress = findViewById(R.id.generate_wallet);

        wordsTextView = findViewById(R.id.words_text_view);
        privateKeyTextView = findViewById(R.id.private_key_text_view);
        walletTextView = findViewById(R.id.wallet_address_text_view);

        generateWords.setOnClickListener(v -> {

            words = WalletUtils.generateTwelveWord();

            wordsTextView.setText(words);
        });

        generatePrivateKey.setOnClickListener(v -> {
            privateKey = WalletUtils.createPrivateKey(words, "");
            privateKeyTextView.setText(privateKey);
        });

        generaWalletAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wallet = WalletUtils.getWalletAddress(privateKey);
                walletTextView.setText(wallet);
            }
        });

        Web3jHandler instance = Web3jHandler.getInstance();
        instance.getBalance((success, object) -> {
            // Getting Balance
        });

        instance.getErc20Balance((success, object) -> {
            // Getting Erc20 token Balance
        });
    }
}
