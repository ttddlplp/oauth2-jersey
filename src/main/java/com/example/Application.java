package com.example;

import com.example.endpoints.AuthzEndpoint;
import com.example.endpoints.RedirectEndpoint;
import com.example.endpoints.ResourceEndpoint;
import com.example.endpoints.TokenEndpoint;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

/**
 * Created by gaoxiangzeng-personal on 16/1/19.
 */
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
            }
        });
    }
}
