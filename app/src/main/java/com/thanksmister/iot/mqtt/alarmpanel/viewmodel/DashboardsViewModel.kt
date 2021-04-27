package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DashboardDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DashboardsViewModel @Inject
constructor(application: Application, private val dataSource: DashboardDao) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    /**
     * Get the items.
     * @return a [Flowable]
     */
    fun getItems(): Flowable<List<Dashboard>> {
        return dataSource.getDashboards()
    }

    /**
     * Insert new item into the database.
     */
    fun insertItem(dashboard: Dashboard) {
        Timber.d("insertItem")
        disposable.add(Completable.fromAction {
            dataSource.insertItem(dashboard)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    /**
     * Insert new item into the database.
     */
    fun deleteItem(dashboard: Dashboard) {
        Timber.d("insertItem")
        disposable.add(Completable.fromAction {
            dataSource.deleteItem(dashboard)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }


    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }
}