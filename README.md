# ti.optimizationpreferences [![Build Status](https://travis-ci.org/williamrijksen/ti.optimizationpreferences.svg?branch=master)](https://travis-ci.org/williamrijksen/ti.optimizationpreferences)

This project is to handle protected apps in huawei devices and to prevent your app being killed by the Samsung Smartmanager.

Inspired by:

 - [http://stackoverflow.com/questions/31638986/protected-apps-setting-on-huawei-phones-and-how-to-handle-it/35220476](http://stackoverflow.com/questions/31638986/protected-apps-setting-on-huawei-phones-and-how-to-handle-it/35220476)
 - [http://stackoverflow.com/questions/37205106/how-do-i-avoid-that-my-app-enters-optimization-on-samsung-devices](http://stackoverflow.com/questions/37205106/how-do-i-avoid-that-my-app-enters-optimization-on-samsung-devices)
 - [http://stackoverflow.com/questions/34074955/android-exact-alarm-is-always-3-minutes-off/34085645#34085645](http://stackoverflow.com/questions/34074955/android-exact-alarm-is-always-3-minutes-off/34085645#34085645)


## Follow Guide

### Setup

1. Download the latest version of the module at releases.
1. Integrate the module into the `modules` folder and define them into the `tiapp.xml` file:

    ```xml
    <modules>
      <module platform="android" version="1.1.0">ti.optimizationpreferences</module>
    </modules>
    ```

### Usage
1. Show the warning and give the end-user the possiblility to add the app to protected apps

   ```js
   if (!OS_ANDROID) {
        return;
    }

    var optimizationpreferences = require('ti.optimizationpreferences'),
		 brands = [optimizationpreferences.HUAWEI, optimizationpreferences.SAMSUNG];

    // need warning checks the huawei/samsung activities exists
    if (optimizationpreferences.needWarning(brands)) {
        optimizationpreferences.check(brands);
    }
   ```
   
