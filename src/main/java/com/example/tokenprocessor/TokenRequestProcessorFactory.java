package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import com.example.token.AccessTokenGenerator;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import javax.inject.Inject;

public class TokenRequestProcessorFactory {
    private final Verifier verifier;
    private final AccessTokenGenerator accessTokenGenerator;

    @Inject
    public TokenRequestProcessorFactory(Verifier verifier, AccessTokenGenerator accessTokenGenerator) {
        this.verifier = verifier;
        this.accessTokenGenerator = accessTokenGenerator;
    }

    public TokenRequestProcessor createTokenProcessor(OAuthRequestWrapper request)
            throws NotSupportedGrantTypException {
        GrantType grantType;
        try {
            grantType = GrantType.valueOf(request.getParameter("grant_type").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotSupportedGrantTypException();
        }
        switch (grantType) {
            case AUTHORIZATION_CODE : return new AuthCodeTokenProcessor(verifier, accessTokenGenerator);
            case CLIENT_CREDENTIALS : return new ClientCredentialTokenProcessor(verifier, accessTokenGenerator);
            case PASSWORD : return new PasswordTokenRequestProcessor(verifier, accessTokenGenerator);
            default : throw new NotSupportedGrantTypException();
        }
    }
}
