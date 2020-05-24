package bachelor.test.locationapp.presenter

// Assuming we receive distance data from 4 anchors. When only 3 anchors are available, the code will fail to execute
data class DistanceData(val firstDistance: DistanceObject, val secondDistance: DistanceObject, val thirdDistance: DistanceObject, val fourthDistance: DistanceObject)