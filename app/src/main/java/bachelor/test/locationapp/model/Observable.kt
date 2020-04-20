package bachelor.test.locationapp.model

import bachelor.test.locationapp.presenter.Observer

interface Observable {
    fun addObserver(observer: Observer)
    fun deleteObserver(observer: Observer)
}