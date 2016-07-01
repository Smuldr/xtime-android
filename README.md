XTime for Android
===================================

[![Build Status](https://travis-ci.org/smuldr/xtime-android.svg?branch=master)](https://travis-ci.org/smuldr/xtime-android)

Android client for the [Xebia Time Tracking System](https://xtime.xebia.com/).


Project structure
-----------------------------------

- `app/src/` Android source code, see [Android Tools Project](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Project-Structure) for more info about the structure
- `gradle/` Gradle wrapper
- `misc/img/` Miscellaneous image source files
- `misc/signing/` Files related to signing releases
- `misc/xtime api/` Some snapshots of XTime DWR requests and responses


Release Signing
-----------------------------------

The release keystore is located in the `misc/signing/` folder. To sign
a release build you have to put the following keys in your **global**
`gradle.properties` file:

```
# These values should be part of your global gradle.properties file,
# DO NOT PUT THIS INFORMATION IN VERSION CONTROL
xtime.signing.storePassword=LE_PASSWORD
xtime.signing.keyAlias=LE_KEY
xtime.signing.keyPassword=LE_PASSWORD
```

Note: if these values are not present on the system the release APK will be
signed using the debug keystore and the Play Store will reject the APK.


Attributions
-----------------------------------

Launcher icon made by [Freepik](http://www.freepik.com) from [www.flaticon.com](http://www.flaticon.com) is licensed under [Creative Commons BY 3.0](http://creativecommons.org/licenses/by/3.0/)

[Google Material Design icons](https://design.google.com/icons/) are licensed under [Creative Commons BY 4.0](http://creativecommons.org/licenses/by/4.0/).
