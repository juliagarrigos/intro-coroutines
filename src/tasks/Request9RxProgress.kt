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

fun loadContributorsProgressRx(service: GitHubService, req: RequestData, scheduler: Scheduler = Schedulers.io()): Observable<List<User>> {

    return service.getOrgReposCallRx(req.org)
        .subscribeOn(scheduler)
        .doOnSuccess { response -> logRepos(req, response) }
        .toObservable()
        .flatMapIterable()
        .flatMap { repo ->
            service.getRepoContributorsCallRx(req.org, repo.name)
                .subscribeOn(scheduler)
                .toObservable()
                .doOnNext { users -> logUsers(repo, users) }
        }
        .scan<List<User>>(emptyList()) { list1, list2 -> (list1 + list2).aggregate() }
        .skip(1)
}
