package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TodoService {

    private static final String BASE = "https://jsonplaceholder.typicode.com";
    private final Gson gson;
    private final HttpHelper http;

    public TodoService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.http = new HttpHelper();
    }

    public List<Todo> getTodosByUser(int userId) throws IOException {
        HttpHelper.HttpResult r = http.sendRequest("GET", BASE + "/users/" + userId + "/todos", null, null);
        Type listType = new TypeToken<List<Todo>>() {}.getType();
        return gson.fromJson(r.body, listType);
    }

    public List<Todo> getOpenTodosByUser(int userId) throws IOException {
        List<Todo> todos = getTodosByUser(userId);
        if (todos == null) return Collections.emptyList();
        return todos.stream().filter(t -> !t.completed).collect(Collectors.toList());
    }
}
