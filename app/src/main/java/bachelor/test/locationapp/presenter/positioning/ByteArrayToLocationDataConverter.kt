package bachelor.test.locationapp.presenter.positioning

class ByteArrayToLocationDataConverter {

    fun getLocationFromByteArray(locationByteArray: ByteArray): LocationData {
        // Since received byte arrays are encoded in little endian, reverse the order for each position
        val xByteArray = byteArrayOf(locationByteArray[4], locationByteArray[3], locationByteArray[2], locationByteArray[1])
        val xPosition = xByteArray.transformIntoSignedInteger().toDouble() / 1000

        val yByteArray = byteArrayOf(locationByteArray[8], locationByteArray[7], locationByteArray[6], locationByteArray[5])
        val yPosition = yByteArray.transformIntoSignedInteger().toDouble() / 1000

        val zByteArray = byteArrayOf(locationByteArray[12], locationByteArray[11], locationByteArray[10], locationByteArray[9])
        val zPosition = zByteArray.transformIntoSignedInteger().toDouble() / 1000

        val qualityFactor = locationByteArray[13].toInt()
        return LocationData(xPosition, yPosition, zPosition, qualityFactor)
    }

    private fun ByteArray.transformIntoSignedInteger() =
        ((this[0].toInt() and 0xFF) shl 24) or
                ((this[1].toInt() and 0xFF) shl 16) or
                ((this[2].toInt() and 0xFF) shl 8) or
                (this[3].toInt() and 0xFF)

}