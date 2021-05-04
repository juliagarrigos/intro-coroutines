package tasks

import contributors.*
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

fun loadContributorsRx(
    service: GitHubService,
    req: RequestData,
    scheduler: Scheduler = Schedulers.io()
): Single<List<User>> {

    return service.getOrgReposCallRx(req.org)
        .subscribeOn(scheduler)
        .doOnSuccess { response -> logRepos(req, response) }
        .toObservable()
        .flatMapIterable()
        .flatMapSingle { repo ->
            service.getRepoContributorsCallRx(req.org, repo.name)
                .subscribeOn(scheduler)
                .doOnSuccess { users -> logUsers(repo, users) }
        }
        .toList()
        .map { it.flatten().aggregate() }
}
