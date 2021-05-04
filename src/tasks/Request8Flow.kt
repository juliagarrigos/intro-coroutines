package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Response
import java.util.*

fun GitHubService.getOrgReposCallFlow(org: String): Flow<Response<List<Repo>>> {
    return flow {
        emit(getOrgReposCallSuspended(org))
    }
}

fun GitHubService.getRepoContributorsCallFlow(owner: String, repo: String): Flow<Response<List<User>>> {
    return flow {
        emit(getRepoContributorsCallSuspended(owner, repo))
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun loadContributorsFlow(
    service: GitHubService,
    req: RequestData,
): Flow<List<User>> {
    return service.getOrgReposCallFlow(req.org)
        .onEach { logRepos(req, it) }
        .map { it.bodyList() }
        .transform { repos -> repos.forEach { emit(it) } }
        .flatMapMerge() { repo -> // Change the operator to flatMapConcat to run calls sequentially.
            service.getRepoContributorsCallFlow(req.org, repo.name)
                .map {
                    logUsers(repo, it)
                    it.bodyList()
                }
        }
        .scan<List<User>, List<User>>(emptyList()) { list1, list2 -> (list1 + list2).aggregate() }
        .flowOn(Dispatchers.IO)
}
