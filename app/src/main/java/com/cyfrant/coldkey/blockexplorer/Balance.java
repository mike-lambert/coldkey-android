package com.cyfrant.coldkey.blockexplorer;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public class Balance {
    private String network;
    private String address;
    @JsonAlias("confirmed_balance")
    private BigDecimal balance;
    @JsonAlias("unconfirmed_balance")
    private BigDecimal unconfirmed;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getUnconfirmed() {
        return unconfirmed;
    }

    public void setUnconfirmed(BigDecimal unconfirmed) {
        this.unconfirmed = unconfirmed;
    }
}
