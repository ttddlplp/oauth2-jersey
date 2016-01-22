package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.as.request.AbstractOAuthTokenRequest;
import org.apache.oltu.oauth2.as.request.OAuthUnauthenticatedTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class AuthCodeTokenProcessor implements TokenRequestProcessor{
    private final Verifier verifier;
    private final AccessTokenGenerator accessTokenGenerator;

    public AuthCodeTokenProcessor(Verifier verifier, AccessTokenGenerator accessTokenGenerator) {
        this.verifier = verifier;
        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public Response process(OAuthRequestWrapper request) {
        try {
            AbstractOAuthTokenRequest oauthRequest = new OAuthUnauthenticatedTokenRequest(request);
            if (verifier.checkAuthCode(oauthRequest.getCode())) {
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setExpiresIn("3600")
                        .setAccessToken(accessTokenGenerator.createAccessToken())
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (OAuthProblemException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request.").build();
        } catch (OAuthSystemException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
