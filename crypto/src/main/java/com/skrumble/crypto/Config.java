package com.skrumble.crypto;

public class Config {

    static String BLOCK_CHAIN_ADDRESS = "";
    static String TOKEN_CONTRACT = "";

    public static void init(String blockChainAddress, String contractAddress){
        BLOCK_CHAIN_ADDRESS = blockChainAddress;
        TOKEN_CONTRACT = contractAddress;
    }
}
