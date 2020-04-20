# LocationApp
Simple Android app that displays the position of a tag inside a Ultra Wideband Real Time Localization System based on decawave's DWM1001-Devkits.

This app was tested with a Samsung Galaxy S8 running Android 9. 

Note: In order to use this app, your UWB network must have already been configured prior to use of this app. This app is no UWB configuration util! It only displays X, Y and Z coordinates of the tag.
For configuration, use decawaves RTLS Manager for example or connect your dev kits via USB and use UART shell mode.

# Usage
Clone this repository into Android Studio and look into app/src/main/java/bachelor/test/locationapp/model/BluetoothService.kt.

Change TAG_MAC to the mac address of your tag and install the app on your Android device.

Note: You must have at least 3 Anchors running in order to enable the tag to calculate its position within the network.
If still no position is shown, check the positions of your anchors. They must match their position set in their configuration quite exactly. Then, move your tag inside your environment. Location data should now come in.
