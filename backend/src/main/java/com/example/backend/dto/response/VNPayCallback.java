package com.example.backend.dto.response;

import lombok.Data;

@Data
public class VNPayCallback {
    private String vnp_TxnRef;
    private String vnp_Amount;
    private String vnp_OrderInfo;
    private String vnp_ResponseCode;
    private String vnp_TransactionNo;
    private String vnp_BankCode;
    private String vnp_PayDate;
    private String vnp_SecureHash;
    private String vnp_CardType;
}
