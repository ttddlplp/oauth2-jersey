package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import com.example.token.AccessTokenGenerator;
import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;
import org.apache.oltu.oauth2.as.request.OAuthUnauthenticatedTokenRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.util.Optional;

public class AuthCodeTokenProcessor implements TokenRequestProcessor{
    private final Verifier verifier;
    private final AccessTokenGenerator accessTokenGenerator;

    public AuthCodeTokenProcessor(Verifier verifier, AccessTokenGenerator accessTokenGenerator) {
        this.verifier = verifier;
        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public Optional<String> process(OAuthRequestWrapper request) throws InvalidRequestException, ServerErrorException {
        try {
            AbstractOAuthTokenRequest oauthRequest = new OAuthUnauthenticatedTokenRequest(request);
            if (verifier.checkAuthCode(oauthRequest.getCode())) {
                return Optional.of(accessTokenGenerator.createAccessToken());
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
