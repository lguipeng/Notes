# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\adt-bundle-windows-x86_64-20131030/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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

-keep class * extends android..app.Activity {*;}
-keep class * extends android.app.Fragment {*;}
-keep class android.support.design.** { *;}
-keep class android.support.v7.** { *; }
-keep class android.support.v4.** { *; }
-keep class com.lguipeng.notes.ui.** {*;}
#保护注解
-keepattributes *Annotation*
-dontwarn java.lang.invoke.*
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
-dontwarn okio.**
-dontwarn dagger.internal.codegen.**
-keep class dagger.** { *; }
#Keep the dagger annotation classes themselves
-keep @interface dagger.*,javax.inject.*
#Keep the Modules intact
-keep @dagger.Module class *

#-Keep the fields annotated with @Inject of any class that is not deleted.
-keepclassmembers class * {
  @javax.inject.* <fields>;
}
#-Keep the names of classes that have fields annotated with @Inject and the fields themselves.
-keepclasseswithmembernames class * {
  @javax.inject.* <fields>;
}
# Keep the generated classes by dagger-compile
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection

-keep class * extends com.evernote.client.coon.mobile.ByteStore
-dontwarn com.evernote.**

-dontwarn org.apache.commons.codec.binary.Base64
-dontwarn javax.xml.bind.DatatypeConverter

-keep class com.tencent.** {*;}

-keepclassmembers class ** {
    public void onEvent*(**);
}

-keep class com.lguipeng.notes.model.SNote {*;}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}