package com.example.tokenprocessor;

import java.util.UUID;

public class AccessTokenGenerator {
    public String createAccessToken() {
        return UUID.randomUUID().toString();
    }
}
