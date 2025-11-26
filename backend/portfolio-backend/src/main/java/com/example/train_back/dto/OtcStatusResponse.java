package com.example.train_back.dto;

import java.time.LocalDateTime;

public class OtcStatusResponse {

    private boolean opened;
    private String accountNo;
    private LocalDateTime openDate;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    // getter / setter ...
}
