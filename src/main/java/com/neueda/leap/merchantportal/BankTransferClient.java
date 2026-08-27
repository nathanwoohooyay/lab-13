package com.neueda.leap.merchantportal;

import java.math.BigDecimal;

public interface BankTransferClient {
    void transfer(Long merchantId, BigDecimal amount) throws BankTransferException;
}
