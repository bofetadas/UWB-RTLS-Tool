# UWB-RTLS-Tool
An Android App that was developed as an evaluation tool for an Ultra-Wideband Real-Time-Localisation System. This was done while working on my bachelor thesis, which dealt with the question how mobile Virtual Reality 3DoF systems can be extended to 6DoF, for what Ultra-Wideband Technology was chosen. A link to the corresponding VR Android app which basically includes the logic of this native app can be found here:
https://github.com/mabaue/UWB-RTLS-VR

With this App, developers can get insights into the current position of one tag inside a Real-Time-Localization System based on Decawave's DWM1001 Dev-Kits. Furthermore, it includes a Kalman Filter implementation and thus shows raw and filtered position and acceleration data. It also offers recording data at stationary positions or during movements.

Inside the measurements directory are sample measurements of my own and several evaluation python scripts. Useful ones are:
- 'all_measurements_evaluation' expects a directory as a parameter. This directory should include stationary measurements so that one can get numeric and visual information about accuracy, precision and jitter of all measurements combined.
- 'fixed_point_measurements_evaluation' is a script which evaluates only one single file. It also yields numeric results about accuracy, precision and jitter and visualizes the recorded data.
- 'measurements_plot_movement' simply plots the recorded raw and filtered data so that one can get a visual idea of how the filtering process behaves compared to raw data.
- 'dilution_of_precision' plots a map of the dilution of precision of the room where the RTLS is deployed. Before use make sure to adjust the anchor and reference positions since it is configured for the room where my RTLS was built in.

This app was tested with a Samsung Galaxy S8 running Android 9. 

Note: In order to use this app, your UWB network must have already been configured prior to use of this app. This app is no UWB configuration util! For configuration, use decawaves RTLS Manager or connect your dev kits via USB and use UART shell mode.

# Usage
Clone this repository into Android Studio and look into BluetoothService.kt.

Change TAG_MAC to the mac address of your tag and install the app on your Android device.

Note: You must have at least 3 Anchors running in order to enable the tag to calculate its position within the network.
If still no position is shown, check the positions of your anchors. They must match their position set in their configuration quite exactly. Then, move your tag inside your environment. Location data should now come in.
