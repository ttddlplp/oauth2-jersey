package com.example.tokenprocessor;

import com.example.Common;
import com.example.OAuthRequestWrapper;
import com.example.token.AccessTokenGenerator;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@PrepareForTest(OAuthASResponse.class)
@RunWith(PowerMockRunner.class)
public class AuthCodeTokenProcessorTest {
    private final static String TOKEN = "test-access-token";
    @Mock
    private OAuthRequestWrapper request;

    @Mock
    private Verifier verifier;

    @Mock
    private AccessTokenGenerator generator;

    private AuthCodeTokenProcessor processor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(generator.createAccessToken()).thenReturn(TOKEN);
        processor = new AuthCodeTokenProcessor(verifier, generator);
    }

    @Test
    public void correctClientIdAndAuthcode() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("code")).thenReturn(Common.AUTHORIZATION_CODE);
        when(request.getParameter("redirect_uri")).thenReturn("test-redirect-uri");
        when(request.getParameter("grant_type")).thenReturn(GrantType.AUTHORIZATION_CODE.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkAuthCode(anyString())).thenReturn(true);
        Optional<String> token = processor.process(request);
        assertThat(token).isPresent().contains(TOKEN);
    }

    @Test
    public void incorrectClientIdAndSecrect() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("code")).thenReturn("wrong-auth-code");
        when(request.getParameter("grant_type")).thenReturn(GrantType.AUTHORIZATION_CODE.toString());
        when(request.getParameter("redirect_uri")).thenReturn("test-redirect-uri");
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(false);
        Optional<String> token = processor.process(request);
        assertThat(token).isEmpty();
    }

    @Test(expected = InvalidRequestException.class)
    public void badRequest() throws Exception {
        when(request.getParameter("client_id")).thenReturn(null);
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("grant_type")).thenReturn(null);
        when(request.getParameter("redirect_uri")).thenReturn(null);
        when(request.getMethod()).thenReturn(null);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        processor.process(request);
    }
}
