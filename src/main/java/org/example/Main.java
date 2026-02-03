package org.example;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws Exception {
        UserClient userClient = new UserClient();
        PostCommentService commentService = new PostCommentService();
        TodoService todoService = new TodoService();

        System.out.println("Get all users:");
        List<User> users = userClient.getAllUsers();
        System.out.println("Users count: " + users.size());

        System.out.println("\nGet user by id = 1:");
        Optional<User> u1 = userClient.getUserById(1);
        u1.ifPresent(user -> System.out.println("User 1 username: " + user.username));

        System.out.println("\nGet user by username = Bret:");
        List<User> byName = userClient.getUserByUsername("Bret");
        byName.forEach(user -> System.out.println("Found user id=" + user.id + " username=" + user.username));

        System.out.println("\nCreate new user (example):");
        User newUser = new User();
        newUser.name = "John Cena";
        newUser.username = "marine123";
        newUser.email = "ucantseeme@yahoo.com";
        User created = userClient.createUser(newUser);
        System.out.println("Create response id: " + (created != null ? created.id : "null"));

        System.out.println("\nUpdate user id=1 (example):");
        User toUpdate = u1.orElseThrow();
        toUpdate.name = "John Cenless";
        User updated = userClient.updateUser(1, toUpdate);
        System.out.println("Updated name in response: " + updated.name);

        System.out.println("\nDelete user id=1 (example):");
        boolean deleted = userClient.deleteUser(1);
        System.out.println("Delete status success: " + deleted);

        System.out.println("\nSave comments of last post for user 1:");
        File saved = commentService.saveCommentsOfLastPost(1);

        System.out.println("\nOpen todos for user 1:");
        List<Todo> open = todoService.getOpenTodosByUser(1);
        System.out.println("Open todo count: " + open.size());
        open.forEach(t -> System.out.println(" - [" + t.id + "] " + t.title));
    }
}
