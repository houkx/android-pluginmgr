## Android PluginManager ##
**dynamic load uninstalled apk**
## Introduction ##
 PluginManager is used to manage android applications like [eclipse](http://eclipse.org/) plugins.
you can start an activity from an uninstalled apk placed in sdcard,just like it has installed or registed in the application's `AndroidManifest.xml`.
### Version
[ ![Download](https://img.shields.io/badge/PluginManager-0.1.4-brightgreen.svg?style=plastic) ](https://github.com/houkx/android-pluginmgr/archive/master.zip)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20PluginManager-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1457)
### Support Features
- a **normal** apk is regard as **plug-in**
- start **activity** from plug-in
- start other activiy from plug activity
- plug activity with **theme**
- plug with custom `Application`
- plug with **.so**
- support android2.x

 it's easy to use:
### How to use:

- declare permission in your `AndroidManifest.xml`: 

  `<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />`

  `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />`

- regist an activity

  `<activity android:name="androidx.pluginmgr.PluginActivity" />`

- load plugin from plug apk:
  ```java
  PluginManager pluginMgr = PluginManager.getInstance(MyActivity);
  File myPlug = new File("/mnt/sdcard/Download/myplug.apk");
  PlugInfo plug = pluginMgr.loadPlugin(myPlug).iterator().next();
  ```
- start activity:
  `
  pluginMgr.startMainActivity(context, plug.getPackageName());
  `

## License
```java
/*
 * Copyright (C) 2015 HouKx <hkx.aidream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```
## About
[author's blog](http://blog.csdn.net/hkxxx/article/details/42194387)

author's email:[address1](mailto:1084940623@qq.com)
[address2](mailto:hkx.aidream@gmail.com)
