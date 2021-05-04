package contributors

import io.reactivex.Single
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Response
import retrofit2.mock.Calls
import tasks.bodyList
import java.util.concurrent.TimeUnit

object MockGithubService : GitHubService {
    override fun getOrgReposCall(org: String): Call<List<Repo>> {
        return Calls.response(repos)
    }

    override fun getRepoContributorsCall(owner: String, repo: String): Call<List<User>> {
        return Calls.response(reposMap.getValue(repo).users)
    }

    override fun getOrgReposCallRx(org: String): Single<List<Repo>> {
        return Single
            .just(Response.success(repos))
            .delay(reposDelay, TimeUnit.MILLISECONDS, testScheduler)
            .map { it.bodyList() }
    }

    override fun getRepoContributorsCallRx(owner: String, repo: String): Single<List<User>> {
        val testRepo = reposMap.getValue(repo)
        return Single.just(Response.success(testRepo.users))
            .delay(testRepo.delay, TimeUnit.MILLISECONDS, testScheduler)
            .map { it.bodyList() }
    }

    override suspend fun getOrgReposCallSuspended(org: String): Response<List<Repo>> {
        delay(reposDelay)
        return Response.success(repos)
    }

    override suspend fun getRepoContributorsCallSuspended(owner: String, repo: String): Response<List<User>> {
        val testRepo = reposMap.getValue(repo)
        delay(testRepo.delay)
        return Response.success(testRepo.users)
    }
}