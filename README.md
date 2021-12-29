# HUAWEI Video Kit Sample

English | [中文](README_ZH.md)

## Table of Contents

 * [Introduction](#introduction)
 * [Installation](#installation)
 * [Configuration](#configuration)
 * [Supported Environments](#supported-environments)
 * [Sample Code](#sample-code)
 * [Result](#result)
 * [License](#license)

## Introduction
The sample code is used to implement the function of playing videos through the Video Kit player SDK.The following describes the structure of the sample code:

- activity:   UI, which contains the video list and playback screen.
- adapter:    Adapter of the video list screen.
- contract:   Stores API classes.
- control:    A class used to control UI data.
- entity:     An entity class of the playback data.
- utils:      A tool class.
- view:      A layout view displayed on the UI.

## Installation
   Decompress the sample code package "videokit-player-sample-x.x.x.xxx.zip".
Take the Android Studio 3.x version as an example. The steps to run the Video Kit player SDK sample code are as follows:

   1. You should modify the applicationId in the build.gradle.
   2. You should create an app in AppGallery Connect, and obtain the file of agconnect-services.json and add to the project.
   3. You should also generate a signing certificate fingerprint and add the certificate file to the project, and add configuration to build.gradle.
      See the [Preparations for Integrating HUAWEI HMS Core](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html?ha_source=hms1) guide to configure app in AppGallery Connect.
   4. In Android Studio, select "File > Open". In the pop-up dialog box, enter the path where the sample code is stored locally, for example: "D:\videokit-player-sample-x.x.x.xxx\VideoKitDemo\src";
   5. Select the src project to be opened, and then click "OK". In the pop-up dialog box, select "New Window" to open the project in a new window.
   6. In Android Studio, replace the URL in the video_kit_demo.txt file with the URL of the video to be played.
   7. In Android Studio, click "Run", then select your device as the target and click "OK" to launch the sample application on your device.

## Configuration
The video_kit_demo.txt file has been placed in the assets directory. Data structure in video_kit_demo.txt:

- id:          Unique ID of the video.
- urlType:     Type of the video. Two options are available: 0 (single video URL) and 1 (multiple video URLs). Use "-SPAD-" to combine multiple video URLs.
- url:         URL of the video.
- name:        Name of the video.

## Supported Environments
It is recommended that the EMUI version be 3.0 or later and the JDK version be 1.8 or later.
To play a video using the sample code, ensure that HMS Core (APK) 5.0.0.300 or a later version has been installed on your phone.

## Sample Code
   The sample code provides the following scenarios:

   1. Play a video.
   2. Switch to the specified bitrate.
   3. Enable or disable the repeat mode.
   4. Set the playback mode (audio or audio+video).
   5. Mute or unmute.
   6. Adjust the playback speed.

## Result
   <img src="result_1.jpg" width = 30% height = 30%>
   <img src="result_2.jpg" width = 30% height = 30%>
   <img src="result_3.jpg" width = 30% height = 30%>
   <img src="result_4.jpg" width = 30% height = 30%>

## Question or issues
If you want to evaluate more about HMS Core,
[r/HMSCore on Reddit](https://www.reddit.com/r/HuaweiDevelopers/) is for you to keep up with latest news about HMS Core, and to exchange insights with other developers.

If you have questions about how to use HMS samples, try the following options:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services) is the best place for any programming questions. Be sure to tag your question with 
`huawei-mobile-services`.
- [Huawei Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001?ha_source=hms1) HMS Core Module is great for general questions, or seeking recommendations and opinions.

If you run into a bug in our samples, please submit an [issue](https://github.com/HMS-Core/hms-scan-demo/issues) to the Repository. Even better you can submit a [Pull Request](https://github.com/HMS-Core/hms-scan-demo/pulls) with a fix.

## License

HUAWEI Video Kit Sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
