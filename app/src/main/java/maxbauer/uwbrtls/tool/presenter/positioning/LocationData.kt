package maxbauer.uwbrtls.tool.presenter.positioning

import maxbauer.uwbrtls.tool.utils.StringUtil

data class LocationData(val xPos: Double, val yPos: Double, val zPos: Double) {

    override fun toString(): String {
        val xPos = StringUtil.inEuropeanNotation(xPos)
        val yPos = StringUtil.inEuropeanNotation(yPos)
        val zPos = StringUtil.inEuropeanNotation(zPos)
        return "$xPos, $yPos, $zPos"
    }
}