package bachelor.test.locationapp.positioning

// Assuming we receive distance data from 4 anchors. When only 3 anchors are available, the code will fail to execute
data class DistanceData(val first: DistanceObject, val second: DistanceObject, val third: DistanceObject, val fourth: DistanceObject)