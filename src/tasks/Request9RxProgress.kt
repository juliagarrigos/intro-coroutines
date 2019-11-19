package tasks

import contributors.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun loadContributorsProgressRx(
    service: GitHubService,
    req: RequestData,
    update: (List<User>, completed: Boolean) -> Unit
): Completable {

    return service.getOrgReposCallRx(req.org)
        .doOnSuccess { response -> logRepos(req, response) }
        .toObservable()
        .flatMapIterable()
        .flatMap { repo ->
            service.getRepoContributorsCallRx(req.org, repo.name).toObservable()
                .subscribeOn(Schedulers.io())
                .doOnNext { users -> logUsers(repo, users) }
                .map { it.aggregate() }
                .doOnNext { update(it, false) }
        }
        .toList().map { it.flatten().aggregate() }
        .doOnSuccess { update(it, true) }
        .ignoreElement()
}
