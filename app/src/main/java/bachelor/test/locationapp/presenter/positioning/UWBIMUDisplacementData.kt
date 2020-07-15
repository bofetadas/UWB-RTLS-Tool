package bachelor.test.locationapp.presenter.positioning

data class UWBIMUDisplacementData(val uwbDisplacementData: DisplacementData, val imuDisplacementData: DisplacementData){

    override fun toString(): String {
        return "${uwbDisplacementData.xDispl}, ${uwbDisplacementData.yDispl}, ${uwbDisplacementData.zDispl}, ${imuDisplacementData.xDispl}, ${imuDisplacementData.yDispl}, ${imuDisplacementData.zDispl}"
    }
}