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
-keep class com.lguipeng.notes.** { *;}
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

#-Keep the fields annotated with @Inject of any class that is not deleted.
-keepclassmembers class * {
  @javax.inject.* <fields>;
}
#-Keep the names of classes that have fields annotated with @Inject and the fields themselves.
-keepclasseswithmembernames class * {
  @javax.inject.* <fields>;
}

# For Guava:
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# For RxJava:
-dontwarn org.mockito.**
-dontwarn org.junit.**
-dontwarn org.robolectric.**
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keep class rx.internal.util.unsafe.** { *; }

# For evernote
-keep class * extends com.evernote.client.coon.mobile.ByteStore
-dontwarn com.evernote.**

-dontwarn org.apache.commons.codec.binary.Base64
-dontwarn javax.xml.bind.DatatypeConverter

-keep class com.tencent.** {*;}

-keepclassmembers class ** {
    public void onEvent*(**);
}

-keep public class com.lguipeng.notes.R$*{
		public static final int *;
}

-keep public class com.evernote.androidsdk.R$*{
		public static final int *;
}

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

-keepattributes SourceFile,LineNumberTable