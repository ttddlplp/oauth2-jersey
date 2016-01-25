package com.example.tokenprocessor;

import com.example.Common;
import com.example.OAuthRequestWrapper;
import com.example.token.AccessTokenData;
import com.example.token.AccessTokenGenerator;
import com.example.token.TokenDao;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(OAuthASResponse.class)
@RunWith(PowerMockRunner.class)
public class ClientCredentialTokenProcessorTest {
    private final static String TOKEN = "test-access-token";
    @Mock
    private OAuthRequestWrapper request;

    @Mock
    private Verifier verifier;

    @Mock
    private AccessTokenGenerator generator;

    @Mock
    private TokenDao tokenDao;


    private ClientCredentialTokenProcessor processor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(generator.createAccessToken()).thenReturn(TOKEN);
        processor = new ClientCredentialTokenProcessor(verifier, generator, tokenDao);
    }

    @Test
    public void correctClientIdAndSecrect() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(true);
        Optional<String> token = processor.process(request);
        assertThat(token).isPresent().contains(TOKEN);
    }

    @Test
    public void incorrectClientIdAndSecrect() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(false);
        Optional<String> token = processor.process(request);
        assertThat(token).isEmpty();
    }

    @Test(expected = InvalidRequestException.class)
    public void badRequest() throws Exception {
        when(request.getParameter("client_id")).thenReturn(null);
        when(request.getParameter("client_secret")).thenReturn(null);
        when(request.getParameter("grant_type")).thenReturn(null);
        when(request.getMethod()).thenReturn(null);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        processor.process(request);
    }

    @Test(expected = ServerErrorException.class)
    @Ignore
    public void correctRequestWithOauthSystemException() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(true);
        OAuthASResponse.OAuthTokenResponseBuilder mockBuilder =
                mock(OAuthASResponse.OAuthTokenResponseBuilder.class);
        when(mockBuilder.setExpiresIn(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setAccessToken(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.buildJSONMessage()).thenThrow(OAuthSystemException.class);
        PowerMockito.mockStatic(OAuthASResponse.class);
        when(OAuthASResponse.tokenResponse(Response.Status.OK.getStatusCode())).thenReturn(mockBuilder);

        processor.process(request);
    }

    @Test
    public void saveTokenData() throws Exception {
        when(request.getParameter("client_id")).thenReturn(Common.CLIENT_ID);
        when(request.getParameter("client_secret")).thenReturn(Common.CLIENT_SECRET);
        when(request.getParameter("grant_type")).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(verifier.checkClient(anyString(), anyString())).thenReturn(true);
        when(generator.createAccessToken()).thenReturn(TOKEN);

        processor.process(request);
        AccessTokenData accessTokenData = new AccessTokenData().withClientId(Common.CLIENT_ID);
        verify(tokenDao, times(1)).save(eq(TOKEN), eq(accessTokenData));
    }
}
