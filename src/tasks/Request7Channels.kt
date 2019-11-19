package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service.getOrgReposCallSuspended(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val channel = Channel<List<User>>()

        repos.map { repo ->
            launch {
                log("starting loading for ${repo.name}")
                val repoUsers = service.getRepoContributorsCallSuspended(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(repoUsers)
            }
        }

        var users = emptyList<User>()
        repeat(repos.size) { iteration ->
            val repoUsers = channel.receive()
            users = (users + repoUsers).aggregate()
            updateResults(users, iteration == repos.lastIndex)
        }
    }
}
