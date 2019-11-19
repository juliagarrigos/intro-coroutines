package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service.getOrgReposCallSuspended(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val users = mutableListOf<User>()
    repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            service.getRepoContributorsCallSuspended(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
        .awaitAll()
        .flatten()
        .aggregate()
}