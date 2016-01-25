package com.example.token;

import java.util.Objects;
import java.util.Set;

public class AccessTokenData {
    private String clientId;
    private String userId;
    private Set<String> roles;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public AccessTokenData withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AccessTokenData withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public AccessTokenData withRoles(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        AccessTokenData that = (AccessTokenData) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, userId, roles);
    }
}
