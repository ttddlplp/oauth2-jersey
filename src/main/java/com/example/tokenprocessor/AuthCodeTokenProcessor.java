package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;

import javax.ws.rs.core.Response;

public class AuthCodeTokenProcessor implements TokenRequestProcessor{
    @Override
    public Response process(OAuthRequestWrapper request) {
        return null;
    }
}
