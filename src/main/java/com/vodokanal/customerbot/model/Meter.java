package com.vodokanal.customerbot.model;

import java.math.BigDecimal;

public class Meter {
    private String serialNumber;
    private String verificationDate;
    private BigDecimal initialValue;
    private int service;
    private String expirationDate;

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getVerificationDate() {
        return verificationDate;
    }

    public BigDecimal getInitialValue() {
        return initialValue;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setVerificationDate(String verificationDate) {
        this.verificationDate = verificationDate;
    }

    public void setInitialValue(BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
