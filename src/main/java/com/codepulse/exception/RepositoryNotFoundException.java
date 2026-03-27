package com.codepulse.exception;

public class RepositoryNotFoundException extends RuntimeException {
    public RepositoryNotFoundException(Long id) {
        super("Repository with id " + id + " not found");
    }

    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
