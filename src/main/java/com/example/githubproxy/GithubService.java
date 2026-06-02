package com.example.githubproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryInfo> getNonForkRepositories(String username) {
        return githubClient.fetchRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .map(repo -> new RepositoryInfo(
                        repo.name(),
                        repo.owner().login(),
                        fetchBranches(username, repo.name())
                ))
                .toList();
    }

    private List<BranchInfo> fetchBranches(String username, String repoName) {
        return githubClient.fetchBranches(username, repoName).stream()
                .map(branch -> new BranchInfo(branch.name(), branch.commit().sha()))
                .toList();
    }
}
