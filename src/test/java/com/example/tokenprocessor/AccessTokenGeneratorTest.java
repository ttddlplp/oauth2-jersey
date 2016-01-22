package com.example.tokenprocessor;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessTokenGeneratorTest {
    private AccessTokenGenerator generator = new AccessTokenGenerator();

    @Test

    public void generateUUIDToken() throws Exception {
        String accessToken = generator.createAccessToken();
        assertThat(accessToken).matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }
}
