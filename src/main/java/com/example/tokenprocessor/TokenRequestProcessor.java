package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;

import javax.ws.rs.core.Response;

public interface TokenRequestProcessor {
    Response process(OAuthRequestWrapper request);
}
