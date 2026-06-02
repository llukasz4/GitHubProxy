package com.example.githubproxy;

class UserNotFoundException extends RuntimeException {
    UserNotFoundException(String username) {
        super("GitHub user '%s' not found".formatted(username));
    }
}
