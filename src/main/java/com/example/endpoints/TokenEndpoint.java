package com.example.endpoints;

import com.example.OAuthRequestWrapper;
import com.example.tokenprocessor.*;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
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
import java.util.Optional;


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
            Optional<String> token = processor.process(requestWrapper);
            if (token.isPresent()) {
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setExpiresIn("3600")
                        .setAccessToken(token.get())
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NotSupportedGrantTypException | InvalidRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ServerErrorException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
