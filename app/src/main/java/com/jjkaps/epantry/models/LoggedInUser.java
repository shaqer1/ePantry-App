package com.jjkaps.epantry.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoggedInUser implements Serializable {
    private String displayName;
    private String email;
    private List<String> recentSearches;

    public LoggedInUser(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.recentSearches = new ArrayList<>();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public LoggedInUser(){}

    public List<String> getRecentSearches() {
        return recentSearches;
    }

    public void setRecentSearches(List<String> recentSearches) {
        this.recentSearches = recentSearches;
    }
}
