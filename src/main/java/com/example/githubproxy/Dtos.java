package com.example.githubproxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
record GithubRepositoryDto(
        String name,
        boolean fork,
        GithubOwnerDto owner
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GithubOwnerDto(String login) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GithubBranchDto(
        String name,
        GithubCommitDto commit
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GithubCommitDto(String sha) {}

record ErrorResponse(int status, String message) {}
