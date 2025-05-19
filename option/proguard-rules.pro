# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\android\sdk_install/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in getBuild.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#不混淆所有的classcom.app.config包下的类和这些类的所有成员变量 不包扩内部类，内部接口
-keep class com.images.config**{*;}
-keep class com.images.config.operation**{*;}
-keep class com.images.config.entity**{*;}
-keep class com.images.unmix**{*;}


