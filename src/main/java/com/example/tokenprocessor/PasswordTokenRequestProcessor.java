package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.as.request.OAuthUnauthenticatedTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class PasswordTokenRequestProcessor implements TokenRequestProcessor{
    private Verifier verifier;
    private AccessTokenGenerator generator;

    public PasswordTokenRequestProcessor(Verifier verifier, AccessTokenGenerator generator) {
        this.verifier = verifier;
        this.generator = generator;
    }

    @Override
    public Response process(OAuthRequestWrapper request) {
        try {
            OAuthUnauthenticatedTokenRequest oAuthTokenRequest = new OAuthUnauthenticatedTokenRequest(request);
            if (verifier.checkUserPass(oAuthTokenRequest.getUsername(), oAuthTokenRequest.getPassword())) {
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(generator.createAccessToken())
                        .setExpiresIn("3600")
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
