package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;

import java.util.Optional;

public interface TokenRequestProcessor {
    Optional<String> process(OAuthRequestWrapper request) throws InvalidRequestException, ServerErrorException;
}
