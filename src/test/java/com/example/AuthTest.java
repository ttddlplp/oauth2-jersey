package com.example;

import com.example.endpoints.AuthzEndpoint;
import com.example.endpoints.RedirectEndpoint;
import com.example.endpoints.ResourceEndpoint;
import com.example.endpoints.TokenEndpoint;
import com.sun.net.httpserver.HttpServer;
import junit.framework.Assert;
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
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

/**
 *
 * @author jdlee
 */
public class AuthTest extends JerseyTest {

    private URL url;
    private Client client = JerseyClientBuilder.newClient();
/*
    @ArquillianResource
    private URL url;
    private Client client = JerseyClientBuilder.newClient();

    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "com.steeplesoft.oauth2")
                .addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/beans.xml")), "beans.xml")
                .addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")), "web.xml")
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                    .importRuntimeDependencies().resolve().withTransitivity().asFile());
        return archive;
    }*/

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = client();
        url = this.getBaseUri().toURL();
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig config = new ResourceConfig();
        config.register(AuthzEndpoint.class);
        config.register(RedirectEndpoint.class);
        config.register(ResourceEndpoint.class);
        config.register(TokenEndpoint.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Database.class).to(Database.class);
            }
        });
        return config;
    }

    @Test
    public void testName() throws Exception {
        System.out.println(target("application.wadl").request().get().readEntity(String.class));

    }

    @Test
    public void authorizationRequest() throws Exception {
        try {
            Response response = makeAuthCodeRequest();
            Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

            String authCode = getAuthCode(response);
            Assert.assertNotNull(authCode);
        } catch (OAuthSystemException | URISyntaxException | JSONException ex) {
            Logger.getLogger(AuthTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void authCodeTokenRequest() throws Exception {
        try {
            Response response = makeAuthCodeRequest();
            Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

            String authCode = getAuthCode(response);
            Assert.assertNotNull(authCode);
            OAuthAccessTokenResponse oauthResponse = makeTokenRequestWithAuthCode(authCode);
            assertNotNull(oauthResponse.getAccessToken());
            assertNotNull(oauthResponse.getExpiresIn());
        } catch (OAuthSystemException | URISyntaxException | JSONException | OAuthProblemException ex) {
            Logger.getLogger(AuthTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void directTokenRequest() {
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(url.toString() + "token")
                    .setGrantType(GrantType.PASSWORD)
                    .setClientId(Common.CLIENT_ID)
                    .setClientSecret(Common.CLIENT_SECRET)
                    .setUsername(Common.USERNAME)
                    .setPassword(Common.PASSWORD)
                    .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
            assertNotNull(oauthResponse.getAccessToken());
            assertNotNull(oauthResponse.getExpiresIn());
        } catch (OAuthSystemException | OAuthProblemException ex ) {
            Logger.getLogger(AuthTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void endToEndWithAuthCode() throws Exception {
        try {
            Response response = makeAuthCodeRequest();
            Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

            String authCode = getAuthCode(response);
            Assert.assertNotNull(authCode);
            
            OAuthAccessTokenResponse oauthResponse = makeTokenRequestWithAuthCode(authCode);
            String accessToken = oauthResponse.getAccessToken();
            
            URL restUrl = new URL(url.toString() + "resource");
            WebTarget target = client.target(restUrl.toURI());
            String entity = target.request(MediaType.TEXT_HTML)
                    .header(Common.HEADER_AUTHORIZATION, "Bearer " + accessToken)
                    .get(String.class);
            System.out.println("Response = " + entity);
        } catch (MalformedURLException | URISyntaxException | OAuthProblemException | OAuthSystemException | JSONException ex) {
            Logger.getLogger(AuthTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                .setRedirectURI(url.toString() + "api/redirect")
                .buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
        return oauthResponse;
    }
}
