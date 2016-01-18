/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jdlee
 */
public class Database {

    private Set<String> authCodes = new HashSet<>();
    private Set<String> tokens = new HashSet<>();

    public void addAuthCode(String authCode) {
        authCodes.add(authCode);
    }

    public boolean isValidAuthCode(String authCode) {
        return authCodes.contains(authCode);
    }

    public void addToken(String token) {
        tokens.add(token);
    }
    
    public boolean isValidToken(String token) {
        return tokens.contains(token);
    }
}
