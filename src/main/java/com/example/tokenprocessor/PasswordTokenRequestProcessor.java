package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import com.example.token.AccessTokenGenerator;
import org.apache.oltu.oauth2.as.request.OAuthUnauthenticatedTokenRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.util.Optional;

public class PasswordTokenRequestProcessor implements TokenRequestProcessor{
    private Verifier verifier;
    private AccessTokenGenerator generator;

    public PasswordTokenRequestProcessor(Verifier verifier, AccessTokenGenerator generator) {
        this.verifier = verifier;
        this.generator = generator;
    }

    @Override
    public Optional<String> process(OAuthRequestWrapper request) throws InvalidRequestException, ServerErrorException {
        try {
            OAuthUnauthenticatedTokenRequest oAuthTokenRequest = new OAuthUnauthenticatedTokenRequest(request);
            if (verifier.checkUserPass(oAuthTokenRequest.getUsername(), oAuthTokenRequest.getPassword())) {
                String accessToken = generator.createAccessToken();
                return Optional.of(accessToken);
            } else {
                return Optional.empty();
            }
        } catch (OAuthProblemException e) {
            throw new InvalidRequestException();
        } catch (OAuthSystemException e) {
            throw new ServerErrorException();
        }
    }
}
