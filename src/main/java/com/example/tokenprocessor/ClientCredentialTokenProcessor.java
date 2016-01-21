package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class ClientCredentialTokenProcessor implements TokenRequestProcessor {
    private final Verifier verifier;

    public ClientCredentialTokenProcessor(Verifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Response process(OAuthRequestWrapper request) {
        try {
            AbstractOAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
            if (verifier.checkClient(oauthRequest.getClientId(), oauthRequest.getClientSecret())) {
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setExpiresIn("3600")
                        .setAccessToken("token")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
