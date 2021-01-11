package maxbauer.uwbrtls.tool.model

import maxbauer.uwbrtls.tool.presenter.Observer

interface Observable {
    fun addObserver(observer: Observer)
    fun deleteObserver(observer: Observer)
}