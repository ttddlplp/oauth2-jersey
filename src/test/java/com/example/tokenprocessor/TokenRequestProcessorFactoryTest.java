package com.example.tokenprocessor;

import com.example.OAuthRequestWrapper;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TokenRequestProcessorFactoryTest {
    TokenRequestProcessorFactory factory = new TokenRequestProcessorFactory();

    @Mock
    OAuthRequestWrapper requestWrapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createAuthCodeTokenProcessor() throws Exception {
        when(requestWrapper.getAuthType()).thenReturn(GrantType.AUTHORIZATION_CODE.toString());
        assertTrue(factory.createTokenProcessor(requestWrapper) instanceof AuthCodeTokenProcessor);
    }

    @Test
    public void createClientCredentialTokenProcessor() throws Exception {
        when(requestWrapper.getAuthType()).thenReturn(GrantType.CLIENT_CREDENTIALS.toString());
        assertTrue(factory.createTokenProcessor(requestWrapper) instanceof ClientCredentialTokenProcessor);
    }

    @Test
    public void createPasswordTokenProcessor() throws Exception {
        when(requestWrapper.getAuthType()).thenReturn(GrantType.PASSWORD.toString());
        assertTrue(factory.createTokenProcessor(requestWrapper) instanceof PasswordTokenRequestProcessor);
    }

    @Test (expected = NotSupportedGrantTypException.class)
    public void supplyNotExistsGrantType() throws Exception {
        when(requestWrapper.getAuthType()).thenReturn("not exists type");
        factory.createTokenProcessor(requestWrapper);
    }

    @Test (expected = NotSupportedGrantTypException.class)
    public void supplyGrantTypeWithNotAssociateProcessor() throws Exception {
        when(requestWrapper.getAuthType()).thenReturn(GrantType.REFRESH_TOKEN.toString());
        factory.createTokenProcessor(requestWrapper);
    }
}
