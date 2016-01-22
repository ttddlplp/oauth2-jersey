package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.common.message.types.GrantType;

public class TokenRequestProcessorFactory {
    public TokenRequestProcessor createTokenProcessor(
            OAuthRequestWrapper request,
            Verifier verifier,
            AccessTokenGenerator generator)
            throws NotSupportedGrantTypException {
        GrantType grantType;
        try {
            grantType = GrantType.valueOf(request.getAuthType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotSupportedGrantTypException();
        }
        switch (grantType) {
            case AUTHORIZATION_CODE : return new AuthCodeTokenProcessor(verifier, generator);
            case CLIENT_CREDENTIALS : return new ClientCredentialTokenProcessor(verifier, generator);
            case PASSWORD : return new PasswordTokenRequestProcessor(verifier, generator);
            default : throw new NotSupportedGrantTypException();
        }
    }
}
