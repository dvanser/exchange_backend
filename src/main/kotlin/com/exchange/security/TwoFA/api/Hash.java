package com.exchange.security.TwoFA.api;

public enum Hash {

    SHA1("HMACSHA1");

    private String hash;

    Hash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
