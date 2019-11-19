package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service.getOrgReposCallSuspended(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val users = mutableListOf<User>()
    return repos.map { repo ->
        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service.getRepoContributorsCallSuspended(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
        .awaitAll()
        .flatten()
        .aggregate()
}