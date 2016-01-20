package com.example.tokenprocessor;

import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;

import javax.ws.rs.core.Response;

public class AuthCodeTokenProcessor implements TokenRequestProcessor{
    @Override
    public Response process(AbstractOAuthTokenRequest request) {
        return null;
    }
}
