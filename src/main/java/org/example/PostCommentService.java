package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

public class PostCommentService {

    private static final String BASE = "https://jsonplaceholder.typicode.com";
    private final Gson gson;
    private final HttpHelper http;

    public PostCommentService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.http = new HttpHelper();
    }

    public File saveCommentsOfLastPost(int userId) throws IOException {
        HttpHelper.HttpResult rPosts = http.sendRequest("GET", BASE + "/users/" + userId + "/posts", null, null);
        Type postsType = new TypeToken<List<Post>>() {}.getType();
        List<Post> posts = gson.fromJson(rPosts.body, postsType);

        if (posts == null || posts.isEmpty()) {
            throw new IllegalStateException("No posts for user " + userId);
        }

        Post last = posts.stream().max(Comparator.comparingInt(p -> p.id)).get();

        HttpHelper.HttpResult rComments = http.sendRequest("GET", BASE + "/posts/" + last.id + "/comments", null, null);
        Type commentsType = new TypeToken<List<Comment>>() {}.getType();
        List<Comment> comments = gson.fromJson(rComments.body, commentsType);

        String filename = String.format("user-%d-post-%d-comments.json", userId, last.id);
        File out = new File(filename);

        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            gson.toJson(comments, w);
        }

        System.out.println("Saved " + (comments != null ? comments.size() : 0) + " comments to " + out.getAbsolutePath());
        return out;
    }
}
