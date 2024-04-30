package com.test.cnouleg.api;
@SuppressWarnings("unused")
public class ProfileResults {
    private Profile[] users;

    public ProfileResults() {}
    public Profile[] getUsers() {
        return users;
    }

    public void setUsers(Profile[] users) {
        this.users = users;
    }
}
