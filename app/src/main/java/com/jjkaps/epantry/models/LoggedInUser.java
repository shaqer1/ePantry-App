package com.jjkaps.epantry.models;

import java.io.Serializable;

public class LoggedInUser implements Serializable {
    private String displayName;
    private String email;

    public LoggedInUser(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public LoggedInUser(){}
}
