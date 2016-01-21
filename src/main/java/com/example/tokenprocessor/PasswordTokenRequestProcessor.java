package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;

import javax.ws.rs.core.Response;

public class PasswordTokenRequestProcessor implements TokenRequestProcessor{
    @Override
    public Response process(OAuthRequestWrapper request) {
        return null;
    }
}
