package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class MyJsonClient {

    private static final String BASE = "https://jsonplaceholder.typicode.com";
    private final Gson gson;

    public MyJsonClient() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private HttpResult sendRequest(String method, String urlStr, String body, Map<String, String> headers) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);

        conn.setRequestProperty("Accept", "application/json");
        if (headers != null) {
            headers.forEach(conn::setRequestProperty);
        }

        if (body != null && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH"))) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int status = conn.getResponseCode();
        InputStream is = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();

        String resp;
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                resp = br.lines().collect(Collectors.joining("\n"));
            }
        } else {
            resp = "";
        }
        conn.disconnect();
        return new HttpResult(status, resp);
    }

    private static class HttpResult {
        final int status;
        final String body;

        HttpResult(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    public List<User> getAllUsers() throws IOException {
        HttpResult r = sendRequest("GET", BASE + "/users", null, null);
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public Optional<User> getUserById(int id) throws IOException {
        HttpResult r = sendRequest("GET", BASE + "/users/" + id, null, null);
        if (r.status >= 200 && r.status < 300 && r.body != null && !r.body.isBlank()) {
            return Optional.of(gson.fromJson(r.body, User.class));
        }
        return Optional.empty();
    }

    public List<User> getUserByUsername(String username) throws IOException {
        String q = BASE + "/users?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        HttpResult r = sendRequest("GET", q, null, null);
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public User createUser(User user) throws IOException {
        List<User> users = getAllUsers();
        int maxId = users.stream().mapToInt(u -> u.id).max().orElse(0);

        String body = gson.toJson(user);
        HttpResult r = sendRequest("POST", BASE + "/users", body, null);
        User created = gson.fromJson(r.body, User.class);

        if (created != null && created.id == maxId + 1) {
            System.out.printf("User created with id = %d (max previous id %d)%n", created.id, maxId);
        } else {
            System.out.printf("Response id = %d, expected %d (note: jsonplaceholder.typicode.com is a fake API and may return fixed ids)%n",
                    created != null ? created.id : -1, maxId + 1);
        }
        return created;
    }

    public User updateUser(int id, User user) throws IOException {
        String body = gson.toJson(user);
        HttpResult r = sendRequest("PUT", BASE + "/users/" + id, body, null);
        User updated = gson.fromJson(r.body, User.class);
        return updated;
    }

    public boolean deleteUser(int id) throws IOException {
        HttpResult r = sendRequest("DELETE", BASE + "/users/" + id, null, null);
        return r.status >= 200 && r.status < 300;
    }

    public List<Post> getPostsByUser(int userId) throws IOException {
        HttpResult r = sendRequest("GET", BASE + "/users/" + userId + "/posts", null, null);
        Type listType = new TypeToken<List<Post>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public List<Comment> getCommentsByPost(int postId) throws IOException {
        HttpResult r = sendRequest("GET", BASE + "/posts/" + postId + "/comments", null, null);
        Type listType = new TypeToken<List<Comment>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public File saveCommentsOfLastPost(int userId) throws IOException {
        List<Post> posts = getPostsByUser(userId);
        if (posts == null || posts.isEmpty()) {
            throw new IllegalStateException("No posts for user " + userId);
        }
        Post last = posts.stream().max(Comparator.comparingInt(p -> p.id)).get();
        List<Comment> comments = getCommentsByPost(last.id);

        String filename = String.format("user-%d-post-%d-comments.json", userId, last.id);
        File out = new File(filename);
        try (FileWriter fw = new FileWriter(out, StandardCharsets.UTF_8)) {
            gson.toJson(comments, fw);
        }
        System.out.println("Saved " + comments.size() + " comments to " + out.getAbsolutePath());
        return out;
    }

    public List<Todo> getTodosByUser(int userId) throws IOException {
        HttpResult r = sendRequest("GET", BASE + "/users/" + userId + "/todos", null, null);
        Type listType = new TypeToken<List<Todo>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public List<Todo> getOpenTodosByUser(int userId) throws IOException {
        List<Todo> todos = getTodosByUser(userId);
        if (todos == null) return Collections.emptyList();
        return todos.stream().filter(t -> !t.completed).collect(Collectors.toList());
    }

    public static class User {
        public int id;
        public String name;
        public String username;
        public String email;
        public Address address;
        public String phone;
        public String website;
        public Company company;
    }

    public static class Address {
        public String street;
        public String suite;
        public String city;
        public String zipcode;
        public Geo geo;
    }

    public static class Geo {
        public String lat;
        public String lng;
    }

    public static class Company {
        public String name;
        public String catchPhrase;
        public String bs;
    }

    public static class Post {
        public int userId;
        public int id;
        public String title;
        public String body;
    }

    public static class Comment {
        public int postId;
        public int id;
        public String name;
        public String email;
        public String body;
    }

    public static class Todo {
        public int userId;
        public int id;
        public String title;
        public boolean completed;
    }

    public static void main(String[] args) throws Exception {
        MyJsonClient client = new MyJsonClient();

        System.out.println("Get all users:");
        List<User> users = client.getAllUsers();
        System.out.println("Users count: " + users.size());

        System.out.println("\nGet user by id = 1:");
        Optional<User> u1 = client.getUserById(1);
        u1.ifPresent(user -> System.out.println("User 1 username: " + user.username));

        System.out.println("\nGet user by username = Bret:");
        List<User> byName = client.getUserByUsername("Bret");
        byName.forEach(user -> System.out.println("Found user id=" + user.id + " username=" + user.username));

        System.out.println("\nCreate new user (example):");
        User newUser = new User();
        newUser.name = "John Cena";
        newUser.username = "marine123";
        newUser.email = "ucantseeme@yahoo.com";
        User created = client.createUser(newUser);
        System.out.println("Create response id: " + (created != null ? created.id : "null"));

        System.out.println("\nUpdate user id=1 (example):");
        User toUpdate = u1.orElseThrow();
        toUpdate.name = "John Cenless";
        User updated = client.updateUser(1, toUpdate);
        System.out.println("Updated name in response: " + updated.name);

        System.out.println("\nDelete user id=1 (example):");
        boolean deleted = client.deleteUser(1);
        System.out.println("Delete status success: " + deleted);

        System.out.println("\nSave comments of last post for user 1:");
        File saved = client.saveCommentsOfLastPost(1);

        System.out.println("\nOpen todos for user 1:");
        List<Todo> open = client.getOpenTodosByUser(1);
        System.out.println("Open todo count: " + open.size());
        open.forEach(t -> System.out.println(" - [" + t.id + "] " + t.title));
    }
}