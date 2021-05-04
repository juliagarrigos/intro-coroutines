package tasks

import contributors.*
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {
    val repos = service.getOrgReposCallSuspended(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    var users = emptyList<User>()
    repos.forEachIndexed { index, repo ->
        val repoUsers = service.getRepoContributorsCallSuspended(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        users = (users + repoUsers).aggregate()
        updateResults(users, index == repos.lastIndex)
    }
}
