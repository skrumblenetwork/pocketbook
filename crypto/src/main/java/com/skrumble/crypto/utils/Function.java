package com.skrumble.crypto.utils;

import org.web3j.abi.datatypes.Type;

import java.util.List;

public class Function {
    private String name;
    private List<Type> inputParameters;

    public Function(String name, List<Type> inputParameters) {
        this.name = name;
        this.inputParameters = inputParameters;
    }

    public String getName() {
        return name;
    }

    public List<Type> getInputParameters() {
        return inputParameters;
    }
}
