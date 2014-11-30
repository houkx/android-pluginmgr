<pre>
android-pluginmgr
dynamic load uninstalled apk
动态加载未安装APK
============================================================================================


此框架使动态加载APK非常简单
只需要2分钟，您就可以学会它的用法：

首先，添加jar包: 
     pluginmgr-xx.jar (可以下载源码，自己用maven build)
     dexmaker-xx.jar (此时最新版为dexmaker-1.1.jar)
     到您的app的libs目录
     
然后，在您App的 AndroidManifest.xml 文件中做两件事：
1、添加读写SD卡的权限:
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
2、注册一个Activity
   <activity android:name="androidx.pluginmgr.PluginActivity" />
   
要想启动一个未安装的APK其中的Activity,您只需要掌握一个类，两个方法即可!
这个类就是androidx.pluginmgr.PluginManager
看代码：
第一步, 加载插件
   PluginManager mgr = PluginManager.getInstance(Context);//传入您的context对象
   // 第1个方法
   mgr.loadPlugin(new File(您的插件路径));//加载，路径可以是单独一个apk，或者一个包含多个apk的目录
第二步,启动插件Activity
   // 第2个方法
   mgr.startActivity(context, new Intent().setComponentName(插件包名, Activity全类名)));
   如果你只是想启动MainActivity, 只须: mgr.startMainActivity(context, 插件包名);
   
  被加载的插件apk无须引入任何额外的依赖，也可以独立运行
  怎么样，是不是很简单呢？
  
  -------------------------------------------------------------
  
  如果您感兴趣深入了解，请看下文，否则请return：
  
  额外功能:
  
  一个插件还可以启动另一个插件里的Activity,或宿主的Activity
  插件可以使用宿主里的类，比如好几个插件都想调用宿主里的某个工具包里的类
  ，那么可以把这个包抽出成jar包，供插件依赖使用，插件如果不需要独立安装运行的话，
  可以将这个jar包不要放在Android project 的 libs目录，而是change another directory,such as ‘mylib’
  
  限制
  1、插件apk里不要假定包名就是清单文件声明的包名，因为包名会在被加载后变成和宿主一样的包名
  2、暂不支持插件的service, 未来将支持切换时间运行的service
  3、不支持 activity 的 launch mode
  
</pre>
