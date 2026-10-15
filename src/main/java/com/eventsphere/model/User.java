package com.eventsphere.model;

import java.io.Serializable;

/** Logged-in user stored in the HTTP session (never contains the password hash). */
public class User implements Serializable {
    private final long id;
    private final String name;
    private final String email;
    private final String role;

    public User(long id, String name, String email, String role) {
        this.id = id; this.name = name; this.email = email; this.role = role;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
