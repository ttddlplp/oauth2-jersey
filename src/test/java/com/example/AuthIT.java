package com.example;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.message.GZipEncoder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AuthIT {

    private URL url;
    private Client client = JerseyClientBuilder.newClient();
    private static ApplicationBase app;

    @BeforeClass
    public static void setUpTestCase() throws Exception {
        app = new ApplicationBase();
        app.startServer();
    }

    @AfterClass
    public static void tearDownTestCase() throws Exception {
        app.stopServer();
    }

    @Before
    public void setUp() throws Exception {
        client = createClient(AuthIT.class.getName());
        url = new URL(ApplicationBase.APPLICATION_SERVER_URL);
    }

    @Test
    public void authorizationRequest() throws Exception {
        Response response = makeAuthCodeRequest();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        String authCode = getAuthCode(response);
        System.out.println("authCode:" + authCode);
        assertNotNull(authCode);
    }

    @Test
    public void authCodeTokenRequest() throws Exception {
        Response response = makeAuthCodeRequest();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        String authCode = getAuthCode(response);
        assertNotNull(authCode);
        OAuthAccessTokenResponse oauthResponse = makeTokenRequestWithAuthCode(authCode);
        assertNotNull(oauthResponse.getAccessToken());
        assertNotNull(oauthResponse.getExpiresIn());
    }

    @Test
    public void directTokenRequestWithPassword() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(url.toString() + "token")
                .setGrantType(GrantType.PASSWORD)
                .setUsername(Common.USERNAME)
                .setPassword(Common.PASSWORD)
                .setClientId(Common.CLIENT_ID)
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
        assertNotNull(oauthResponse.getAccessToken());
        assertNotNull(oauthResponse.getExpiresIn());
    }

    @Test
    public void directTokenRequestWithClientCredentials() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(url.toString() + "token")
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .setClientId(Common.CLIENT_ID)
                .setClientSecret(Common.CLIENT_SECRET)
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
        assertNotNull(oauthResponse.getAccessToken());
        assertNotNull(oauthResponse.getExpiresIn());
    }

    @Test(expected = Exception.class)
    public void directTokenRequestWithClientCredentialsFailed() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(url.toString() + "token")
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .setClientId("Random id")
                .setClientSecret("random secret")
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
        assertNotNull(oauthResponse.getAccessToken());
        assertNotNull(oauthResponse.getExpiresIn());
    }

    @Test
    public void endToEndWithAuthCode() throws Exception {
        Response response = makeAuthCodeRequest();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        String authCode = getAuthCode(response);
        assertNotNull(authCode);

        OAuthAccessTokenResponse oauthResponse = makeTokenRequestWithAuthCode(authCode);
        String accessToken = oauthResponse.getAccessToken();

        URL restUrl = new URL(url.toString() + "resource");
        WebTarget target = client.target(restUrl.toURI());
        String entity = target.request(MediaType.TEXT_HTML)
                .header(Common.HEADER_AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);
        System.out.println("Response = " + entity);
    }

    @Test(expected = OAuthProblemException.class)
    public void notSupportedGrantType() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(url.toString() + "token")
                .setClientId(Common.CLIENT_ID)
                .setClientSecret(Common.CLIENT_SECRET)
                .setGrantType(GrantType.REFRESH_TOKEN)
                .setCode(Common.AUTHORIZATION_CODE)
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
    }

    void testValidTokenResponse(HttpURLConnection httpURLConnection) throws Exception {
        InputStream inputStream;
        if (httpURLConnection.getResponseCode() == 400) {
            inputStream = httpURLConnection.getErrorStream();
        } else {
            inputStream = httpURLConnection.getInputStream();
        }
        String responseBody = OAuthUtils.saveStreamAsString(inputStream);
        assert (Common.ACCESS_TOKEN_VALID.equals(responseBody));
    }

    private Response makeAuthCodeRequest() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
                .authorizationLocation(url.toString() + "authz")
                .setClientId(Common.CLIENT_ID)
                .setRedirectURI(url.toString() + "redirect")
                .setResponseType(ResponseType.CODE.toString())
                .setState("state")
                .buildQueryMessage();
        WebTarget target = client.target(new URI(request.getLocationUri()));
        Response response = target.request(MediaType.TEXT_HTML).get();
        return response;
    }

    private String getAuthCode(Response response) throws JSONException {
        JSONObject obj = new JSONObject(response.readEntity(String.class));
        JSONObject qp = obj.getJSONObject("queryParameters");
        String authCode = null;
        if (qp != null) {
            authCode = qp.getString("code");
        }

        return authCode;
    }

    private OAuthAccessTokenResponse makeTokenRequestWithAuthCode(String authCode) throws OAuthProblemException, OAuthSystemException {
        OAuthClientRequest request = OAuthClientRequest
                .tokenLocation(url.toString() + "token")
                .setClientId(Common.CLIENT_ID)
                .setClientSecret(Common.CLIENT_SECRET)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setCode(authCode)
                .setRedirectURI(url.toString() + "redirect")
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
        return oauthResponse;
    }

    public Client createClient(String testClassName) {
        Client client = ClientBuilder.newClient();
        client.register(new LoggingFilter(
                        java.util.logging.Logger.getLogger(testClassName),
                        true)
        );

        client.register(EncodingFilter.class);
        client.register(GZipEncoder.class);
        return client;
    }

    private WebTarget target(String path) {
        return client.target(ApplicationBase.APPLICATION_SERVER_URL).path(path);
    }
}
