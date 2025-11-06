package com.example.backend.dto.response;

import lombok.Data;

@Data
public class UserGrowth {
    private String date;
    private long guestUsers;
    private long freeUsers;
    private long premiumUsers;
}
