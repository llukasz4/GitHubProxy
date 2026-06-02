# GitHub Proxy API

A Spring Boot application that acts as a proxy to the GitHub API, exposing a simplified endpoint to list a user's non-fork repositories with branch information.

## Requirements

- Java 25
- Gradle (wrapper included)

## Running the Application

```bash
./gradlew bootRun
```

The application starts on port `8080` by default.

## Running Tests

```bash
./gradlew test
```

## API

### List non-fork repositories

Returns all repositories for a given GitHub user that are not forks. For each repository, includes its name, owner login, and branch list with the last commit SHA per branch.

**Request**
```
GET /api/users/{username}/repositories
```

**Response `200 OK`**
```json
[
  {
    "name": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "aa218f56b14c9653891f9e74264a383fa43fefbd"
      }
    ]
  }
]
```

**Response `404 Not Found`** — when the GitHub user does not exist
```json
{
  "status": 404,
  "message": "GitHub user 'non-existing-user' not found"
}
```

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `github.api.base-url` | GitHub API base URL | `https://api.github.com` |

## Tech Stack

- Java 25
- Spring Boot 4
- Gradle with Kotlin DSL
- WireMock (integration tests)
