package com.example;

import com.example.endpoints.AuthzEndpoint;
import com.example.endpoints.RedirectEndpoint;
import com.example.endpoints.ResourceEndpoint;
import com.example.endpoints.TokenEndpoint;
import com.example.tokenprocessor.AccessTokenGenerator;
import com.example.tokenprocessor.TokenRequestProcessorFactory;
import com.example.tokenprocessor.Verifier;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

public class Application extends ResourceConfig {
    public Application() {
        register(AuthzEndpoint.class);
        register(RedirectEndpoint.class);
        register(ResourceEndpoint.class);
        register(TokenEndpoint.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Database.class).to(Database.class).in(Singleton.class);
                bind(Verifier.class).to(Verifier.class);
                bind(AccessTokenGenerator.class).to(AccessTokenGenerator.class);
                bind(TokenRequestProcessorFactory.class).to(TokenRequestProcessorFactory.class);
            }
        });
    }
}
