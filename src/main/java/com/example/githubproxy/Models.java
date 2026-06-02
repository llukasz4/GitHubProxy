package com.example.githubproxy;

import java.util.List;

record RepositoryInfo(String name, String ownerLogin, List<BranchInfo> branches) {}

record BranchInfo(String name, String lastCommitSha) {}
