package maxbauer.uwbrtls.tool.presenter.positioning

class ByteArrayToLocationDataConverter {

    fun getUWBLocationFromByteArray(locationByteArray: ByteArray): LocationData {
        // Since received byte arrays are encoded in little endian,
        // reverse the order for each position
        val xByteArray = byteArrayOf(
            locationByteArray[4],
            locationByteArray[3],
            locationByteArray[2],
            locationByteArray[1])
        val xPosition = xByteArray.transformIntoSignedDouble()

        val yByteArray = byteArrayOf(
            locationByteArray[8],
            locationByteArray[7],
            locationByteArray[6],
            locationByteArray[5])
        val yPosition = yByteArray.transformIntoSignedDouble()

        val zByteArray = byteArrayOf(
            locationByteArray[12],
            locationByteArray[11],
            locationByteArray[10],
            locationByteArray[9])
        val zPosition = zByteArray.transformIntoSignedDouble()
        return LocationData(xPosition, yPosition, zPosition)
    }

    private fun ByteArray.transformIntoSignedDouble() =
        ((((this[0].toInt() and 0xFF) shl 24) or
        ((this[1].toInt() and 0xFF) shl 16) or
        ((this[2].toInt() and 0xFF) shl 8) or
        (this[3].toInt() and 0xFF))
        // Divide by 1000.0 to be in double meter units
        / 1000.0)
}