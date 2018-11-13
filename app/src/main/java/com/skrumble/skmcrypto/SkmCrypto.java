package com.skrumble.skmcrypto;

import android.support.multidex.MultiDexApplication;

import com.skrumble.crypto.Config;

public class SkmCrypto extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Config.init("https://ropsten.infura.io/v3/86056db565c0456a9c270b98aef919fe", "0x1D02FE3a48B07712d0415b014db2DE4e3cdA77Ba");
    }
}
