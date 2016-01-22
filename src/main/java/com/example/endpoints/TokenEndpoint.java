package com.example.endpoints;

import com.example.OAuthRequestWrapper;
import com.example.tokenprocessor.NotSupportedGrantTypException;
import com.example.tokenprocessor.TokenRequestProcessor;
import com.example.tokenprocessor.TokenRequestProcessorFactory;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;


@Path("/token")
public class TokenEndpoint {
    private final TokenRequestProcessorFactory factory;

    @Inject
    public TokenEndpoint(TokenRequestProcessorFactory factory) {
        this.factory = factory;
    }

    public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request, MultivaluedMap<String, String> form)
            throws OAuthSystemException {
        try {
            OAuthRequestWrapper requestWrapper = new OAuthRequestWrapper(request, form);
            TokenRequestProcessor processor = factory.createTokenProcessor(requestWrapper);
            return processor.process(requestWrapper);
        } catch (NotSupportedGrantTypException e) {
            throw new RuntimeException(e);
        }
    }

    private Response buildInvalidClientIdResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildInvalidClientSecretResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                        .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildBadAuthCodeResponse() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT)
                .setErrorDescription("invalid authorization code")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildInvalidUserPassResponse() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT)
                .setErrorDescription("invalid username or password")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
}
