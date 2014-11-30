android-pluginmgr<br>
dynamic load uninstalled apk<br>
动态加载未安装APK<br>
============================================================================================


此框架使动态加载APK非常简单<br>
只需要2分钟，您就可以学会它的用法：<br>

首先，添加jar包: <br>
     pluginmgr-xx.jar (可以下载源码，自己用maven build)<br>
     dexmaker-xx.jar (此时最新版为dexmaker-1.1.jar)<br>
     到您的app的libs目录<br>
     
然后，在您App的 AndroidManifest.xml 文件中做两件事：<br>
1、添加读写SD卡的权限: 
   
    &lt;uses-permission android:name=&#34;android.permission.MOUNT_UNMOUNT_FILESYSTEMS&#34;/&gt; 
    &lt;uses-permission android:name=&#34;android.permission.WRITE_EXTERNAL_STORAGE&#34;/&gt;  
    
2、注册一个Activity 
 
   &lt;activity android:name=&#34;androidx.pluginmgr.PluginActivity&#34;&nbsp;/&gt; <br>
     
要想启动一个未安装的APK其中的Activity,您只需要掌握一个类，两个方法即可!<br>
这个类就是androidx.pluginmgr.PluginManager <br>
看代码：<br>
第一步, 加载插件 <br>
   PluginManager mgr = PluginManager.getInstance(Context);//传入您的context对象 <br>
   // 第1个方法  <br>
   mgr.loadPlugin(new File(您的插件路径));//加载，路径可以是单独一个apk，或者一个包含多个apk的目录 <br>
第二步,启动插件Activity <br>
   // 第2个方法 <br>
   mgr.startActivity(context, new Intent().setComponentName(插件包名, Activity全类名))); <br>
   如果你只是想启动MainActivity, 只须: mgr.startMainActivity(context, 插件包名); <br>
   
  被加载的插件apk无须引入任何额外的依赖，也可以独立运行 <br>
  怎么样，是不是很简单呢？ <br>
  <br>
  -------------------------------------------------------------
  <br>
  如果您感兴趣深入了解，请看下文，否则请return：<br>
  <br>
  额外功能:<br>
  <br>
  一个插件还可以启动另一个插件里的Activity,或宿主的Activity<br>
  插件可以使用宿主里的类，比如好几个插件都想调用宿主里的某个工具包里的类<br>
  ，那么可以把这个包抽出成jar包，供插件依赖使用，插件如果不需要独立安装运行的话，<br>
  可以将这个jar包不要放在Android project 的 libs目录，而是change another directory,such as ‘mylib’<br>
  <br>
  限制<br>
  1、插件apk里不要假定包名就是清单文件声明的包名，因为包名会在被加载后变成和宿主一样的包名<br>
  2、暂不支持插件的service, 未来将支持切换时间运行的service<br>
  3、不支持 activity 的 launch mode<br>
  

