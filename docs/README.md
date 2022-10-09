# Jurisdiction Finder

![logo](logo.png)

This app is designed to take a user's current location (GPS coordinates) or a manually entered address and yield the current legal jurisdiction (county, incorporated city, township, etc).  This app was originally meant to be tied to a database of certain legal information, but the project was never seen to fruition.  However, since the code for determining legal jurisdictions was ready, it was determined that it might as well not go to waste, despite its simplicity.  The Java program itself is only one file with 260 lines of code.

## Instructions for use

Opening the downloaded app will reveal a blank input screen:

![Empty app screenshot](screen_shot_home_screen.gif)

The user may either use their GPS or enter an address.  If the user presses the GPS button, the closest address will be displayed, along with the jurisdiction.

![App screenshot](screen_shot_gps_results.gif)

If the user may also enter a point of interest, as shown in the next screenshot.

![Address entry screenshot](screen_shot_address_entry.gif)

Entering a point of interest will result in the GPS coordinates, address, and jurisdiction to be revealed.

![Address results screenshot](screen_shot_address_results.gif)

## Prerequisites

[Android Studio](https://developer.android.com/studio)

## Installing

Assuming you have Android Studio up and running, you can do the following to get a working environment up and running:

1. Open Android Studio
2. Go to `File` menu and then click `Close` if you have another project showing
3. Click `Get from Version Control`
4. Enter `https://github.com/robbie9485/Jurisdiction-Finder.git` into the `URL` field
5. Select a location on your hard drive to house the project under `Directory`
6. Click `Clone`

## High Level Overview

Because this app is fairly concise, there is no high level overview.  Instead, please see the [`MainActivity.java`](https://github.com/robbie9485/Jurisdiction-Finder/blob/master/app/src/main/java/com/rsquared/jurisdictionfinder/MainActivity.java) and read the comments for each function.  It is worth noting that an older version of this program used the Google Maps API, but the current version uses the standard location services library.  The Google Maps API version was lost.

## Deployment

[This app is available in Google Play](https://play.google.com/store/apps/details?id=com.rsquared.jurisdictionfinder)

## Built With

[Android Studio](https://developer.android.com/studio)

## Contributing

Please contact me (robbie9485) on GitHub in order to inquire more about the project.

## Versioning

`Git` functions in Android Studio were used to version the software on *GitHub*

## Authors

Robert Rutherford

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details

## Acknowledgments

* Thanks to James Sinclair for providing the motivation to create this app.