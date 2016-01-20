package com.example.tokenprocessor;

import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;

import javax.ws.rs.core.Response;

public interface TokenRequestProcessor {
    Response process(AbstractOAuthTokenRequest request);
}
