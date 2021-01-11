package bachelor.test.uwbrtlstool.model

import bachelor.test.uwbrtlstool.presenter.Observer

interface Observable {
    fun addObserver(observer: Observer)
    fun deleteObserver(observer: Observer)
}