package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class UserClient {

    private static final String BASE = "https://jsonplaceholder.typicode.com";
    private final Gson gson;
    private final HttpHelper http;

    public UserClient() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.http = new HttpHelper();
    }

    public List<User> getAllUsers() throws IOException {
        HttpHelper.HttpResult r = http.sendRequest("GET", BASE + "/users", null, null);
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public Optional<User> getUserById(int id) throws IOException {
        HttpHelper.HttpResult r = http.sendRequest("GET", BASE + "/users/" + id, null, null);
        if (r.status >= 200 && r.status < 300 && r.body != null && !r.body.isBlank()) {
            return Optional.of(gson.fromJson(r.body, User.class));
        }
        return Optional.empty();
    }

    public List<User> getUserByUsername(String username) throws IOException {
        String q = BASE + "/users?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        HttpHelper.HttpResult r = http.sendRequest("GET", q, null, null);
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public User createUser(User user) throws IOException {
        List<User> users = getAllUsers();
        int maxId = users.stream().mapToInt(u -> u.id).max().orElse(0);

        String body = gson.toJson(user);
        HttpHelper.HttpResult r = http.sendRequest("POST", BASE + "/users", body, null);
        User created = gson.fromJson(r.body, User.class);

        if (created != null && created.id == maxId + 1) {
            System.out.printf("User created with id = %d (max previous id %d)%n", created.id, maxId);
        } else {
            System.out.printf("Response id = %d, expected %d (jsonplaceholder is a fake API and may return other ids)%n",
                    created != null ? created.id : -1, maxId + 1);
        }
        return created;
    }

    public User updateUser(int id, User user) throws IOException {
        String body = gson.toJson(user);
        HttpHelper.HttpResult r = http.sendRequest("PUT", BASE + "/users/" + id, body, null);
        return gson.fromJson(r.body, User.class);
    }

    public boolean deleteUser(int id) throws IOException {
        HttpHelper.HttpResult r = http.sendRequest("DELETE", BASE + "/users/" + id, null, null);
        return r.is2xx();
    }
}
