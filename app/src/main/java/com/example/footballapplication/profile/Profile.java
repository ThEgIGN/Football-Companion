package com.example.footballapplication.profile;

public class Profile {

    private final String name;
    private final String searchStrings;

    public Profile(String name, String searchStrings) {
        this.name = name;
        this.searchStrings = searchStrings;
    }

    @SuppressWarnings("unused")
    public Profile() {
        // Firebase requires empty constructor
        this.name = "";
        this.searchStrings = "";
    }

    public String getName() {
        return name;
    }

    public String getSearchStrings() {
        return searchStrings;
    }
}
