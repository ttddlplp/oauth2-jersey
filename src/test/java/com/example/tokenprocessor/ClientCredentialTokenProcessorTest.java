package com.example.tokenprocessor;

import com.example.Common;
import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ClientCredentialTokenProcessorTest {
    @Mock
    private OAuthRequestWrapper request;

    @Mock
    private Verifier verifier;

    private ClientCredentialTokenProcessor processor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        processor = new ClientCredentialTokenProcessor(verifier);
    }

    @Test
    public void correctClientIdAndSecrect() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(true);
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThatJson(response.getEntity()).node("access_token").isPresent();
    }

    @Test
    public void incorrectClientIdAndSecrect() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(false);
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        assertThatJson(response.getEntity()).node("access_token").isAbsent();
    }

    @Test
    public void badRequest() throws Exception {
        when(request.getParameter("client_id")).thenReturn(null);
        when(request.getParameter("client_secret")).thenReturn(null);
        when(request.getParameter("grant_type")).thenReturn(null);
        when(request.getMethod()).thenReturn(null);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        Response response = processor.process(request);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
