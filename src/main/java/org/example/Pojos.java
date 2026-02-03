package org.example;

class User {
    int id;
    String name;
    String username;
    String email;
    Address address;
    String phone;
    String website;
    Company company;
}

class Address {
    String street;
    String suite;
    String city;
    String zipcode;
    Geo geo;
}

class Geo {
    String lat;
    String lng;
}

class Company {
    String name;
    String catchPhrase;
    String bs;
}

class Post {
    int userId;
    int id;
    String title;
    String body;
}

class Comment {
    int postId;
    int id;
    String name;
    String email;
    String body;
}

class Todo {
    int userId;
    int id;
    String title;
    boolean completed;
}