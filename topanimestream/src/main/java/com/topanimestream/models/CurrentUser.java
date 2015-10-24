package com.topanimestream.models;

public class CurrentUser extends Account {
    //These 2 fields are only return on login and validatetoken
    private String Token;
    private String RoleName;

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getRoleName() {
        return RoleName;
    }

    public void setRoleName(String roleName) {
        RoleName = roleName;
    }
}
