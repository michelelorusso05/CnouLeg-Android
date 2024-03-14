package com.test.cnouleg.api;

import com.test.cnouleg.api.Author;

public class AuthorResults {
    private Author[] users;

    public AuthorResults() {}
    public Author[] getUsers() {
        return users;
    }

    public void setUsers(Author[] users) {
        this.users = users;
    }
}
