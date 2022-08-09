# Video Kit Sample Code

## Table of Contents
 * [Introduction](#Introduction)
 * [Installation](#Installation)
 * [Supported Environments](#Supported Environments)
 * [Result](#Result)
 * [Technical Support](#Technical-Support)
 * [License](#License)

## Introduction
   The sample code is used to implement the function of playing HDR Vivid videos through the HDR Vivid SDK.
   The HDR Vivid service provided by Video Kit comes with a range of image-processing features like the OETF, tone mapping, and HDR2SDR, enabling you to quickly equip your app with functions like playback and sharing of HDR Vivid videos.
   The sample code shows how to parse the video source file in HDR video format, and how to interact with SDK to realize rendering or transcoding after obtaining the video stream and metadata information.
   From the sample code, you can get the following references:
   1. Integration and call mode of Java interface.
   2. Integration and call mode of Native interface.
   3. Rendering data comes from the processing process of a Surface scenario.
   4. Processing flow of rendering data from the buffer input scenario.
   5. The SDK works in rendering mode.
   6. The SDK works in transcoding mode.

## Installation
   Decompress the sample code package "videokit-player-sample-x.x.x.xxx.zip".
Take the Android Studio 3.x version as an example. The steps to run the HDR Vivid SDK sample code are as follows:

   1. You should modify the applicationId in the build.gradle.
   2. In Android Studio, select "File > Open". In the pop-up dialog box, enter the path where the sample code is stored locally, for example: "D:\videokit-player-sample-x.x.x.xxx\VideoKitDemo\src\hdrvivid\src";
      Select the src project to be opened, and then click "OK". In the pop-up dialog box, select "New Window" to open the project in a new window.
   3. Download the HDR Vivid SDK, decompress it, and copy the .so library and header files to the specified directory.
   4. Obtain the MP4 video source file in HDR Vivid format and push it to the /sdcard/Documents/vivid/ directory on the mobile phone.
   5. In the hdrvivid directory, run the gradlew build command to edit the project.
   6. You should also generate a signing certificate fingerprint and add the certificate file to the project, and add configuration to build.gradle.
      See the [Preparations for Integrating HUAWEI HMS Core](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html) guide to configure app in AppGallery Connect.
      For more information, go to:

- [Guides](https://developer.huawei.com/consumer/en/doc/development/Media-Guides/introduction-0000001050439577?ha_source=hms1)
- [References](https://developer.huawei.com/consumer/en/doc/development/Media-References/video-description-0000001076873506?ha_source=hms1)

## Supported Environments
   It is recommended that the EMUI version be 5.0 or later and the JDK version be 1.8 or later.

## Result
   <img src="result_1.jpg" width = 30% height = 30%>

## Technical Support
You can visit the [Reddit community](https://www.reddit.com/r/HuaweiDevelopers/) to obtain the latest information about HMS Core and communicate with other developers.

If you have any questions about the sample code, try the following:
- Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services?tab=Votes), submit your questions, and tag them with `huawei-mobile-services`. Huawei experts will answer your questions.
- Visit the HMS Core section in the [HUAWEI Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001?ha_source=hms1) and communicate with other developers.

If you encounter any issues when using the sample code, submit your [issues](https://github.com/HMS-Core/hms-video-demo-android/issues) or submit a [pull request](https://github.com/HMS-Core/hms-video-demo-android/pulls).

## License
   Video Kit Android Sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
