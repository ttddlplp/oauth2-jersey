package com.example.tokenprocessor;

import com.example.Common;
import com.example.Database;

import javax.inject.Inject;

public class Verifier {
    private final Database database;

    @Inject
    public Verifier(Database database) {
        this.database = database;
    }

    public boolean checkClientId(String clientId) {
        return Common.CLIENT_ID.equals(clientId);
    }

    public boolean checkClient(String clientId, String secret) {
        return Common.CLIENT_SECRET.equals(secret) && Common.CLIENT_ID.equals(clientId);
    }

    public boolean checkAuthCode(String authCode) {
        return database.isValidAuthCode(authCode);
    }

    public boolean checkUserPass(String user, String pass) {
        return Common.PASSWORD.equals(pass) && Common.USERNAME.equals(user);
    }
}
