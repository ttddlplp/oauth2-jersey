package com.example.tokenprocessor;

import com.example.Common;
import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(OAuthASResponse.class)
@RunWith(PowerMockRunner.class)
public class PasswordTokenRequestProcessorTest {
    private final static String TOKEN = "test-access-token";
    @Mock
    private OAuthRequestWrapper request;

    @Mock
    private Verifier verifier;

    @Mock
    private AccessTokenGenerator generator;

    private PasswordTokenRequestProcessor processor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(generator.createAccessToken()).thenReturn(TOKEN);
        processor = new PasswordTokenRequestProcessor(verifier, generator);
    }

    @Test
    public void correctUsernameAndPassword() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("username")).thenReturn(Common.USERNAME);
        when(request.getParameter("password")).thenReturn(Common.PASSWORD);
        when(request.getParameter("grant_type")).thenReturn(GrantType.PASSWORD.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkUserPass(anyString(), anyString())).thenReturn(true);
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThatJson(response.getEntity()).node("access_token").isPresent().isEqualTo(TOKEN);
    }

    @Test
    public void incorrectUsernameAndPassword() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("username")).thenReturn("wrong user");
        when(request.getParameter("password")).thenReturn("wrong password");
        when(request.getParameter("grant_type")).thenReturn(GrantType.PASSWORD.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkUserPass(anyString(), anyString())).thenReturn(false);
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        assertThatJson(response.getEntity()).node("access_token").isAbsent();
    }

    @Test
    public void badRequest() throws Exception {
        when(request.getParameter("client_id")).thenReturn(null);
        when(request.getMethod()).thenReturn(null);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void correctRequestWithOauthSystemException() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("username")).thenReturn("wrong user");
        when(request.getParameter("password")).thenReturn("wrong password");
        when(request.getParameter("grant_type")).thenReturn(GrantType.PASSWORD.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkUserPass(anyString(), anyString())).thenReturn(true);
        OAuthASResponse.OAuthTokenResponseBuilder mockBuilder =
                mock(OAuthASResponse.OAuthTokenResponseBuilder.class);
        when(mockBuilder.setExpiresIn(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setAccessToken(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.buildJSONMessage()).thenThrow(OAuthSystemException.class);
        PowerMockito.mockStatic(OAuthASResponse.class);
        when(OAuthASResponse.tokenResponse(Response.Status.OK.getStatusCode())).thenReturn(mockBuilder);

        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}