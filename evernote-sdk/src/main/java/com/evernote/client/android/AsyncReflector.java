package com.evernote.client.android;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that uses reflection to asynchronously wrap Client methods.
 */
@SuppressWarnings({"deprecation", "unchecked"})
@Deprecated
/*package*/ final class AsyncReflector {

  private AsyncReflector() {

  }

  /**
   * List of primitives to convert from autoboxed method calls.
   */
  @Deprecated
  public static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = new HashMap<>();
  static {
    PRIMITIVE_MAP.put(Boolean.class, boolean.class);
    PRIMITIVE_MAP.put(Byte.class, byte.class);
    PRIMITIVE_MAP.put(Short.class, short.class);
    PRIMITIVE_MAP.put(Character.class, char.class);
    PRIMITIVE_MAP.put(Integer.class, int.class);
    PRIMITIVE_MAP.put(Long.class, long.class);
    PRIMITIVE_MAP.put(Float.class, float.class);
    PRIMITIVE_MAP.put(Double.class, double.class);
  }

  /**
   * Singled threaded Executor for async work.
   */
  private static ExecutorService sThreadExecutor = Executors.newSingleThreadExecutor();

  /**
   * Reflection to run Asynchronous methods.
   */
  @Deprecated
  static <T> void execute(final Object receiver, final OnClientCallback<T> callback, final String function, final Object... args) {
    final Handler handler = new Handler(Looper.getMainLooper());
    sThreadExecutor.execute(new Runnable() {
      public void run() {
        try {
          Class[] classes = new Class[args.length];
          for (int i = 0; i < args.length; i++) {
            //Convert Autoboxed primitives to actual primitives (ex: Integer.class to int.class)
            if (PRIMITIVE_MAP.containsKey(args[i].getClass())) {
              classes[i] = PRIMITIVE_MAP.get(args[i].getClass());
            } else {
              classes[i] = args[i].getClass();
            }
          }

          Method method;
          if (receiver instanceof Class) {
            //Can receive a class if using for static methods
            method = ((Class) receiver).getMethod(function, classes);
          } else {
            //used for instance methods
            method = receiver.getClass().getMethod(function, classes);
          }

          final T answer = (T) method.invoke(receiver, args);

          handler.post(new Runnable() {
            @Override
            public void run() {
              if (callback != null) callback.onSuccess(answer);
            }
          });

        } catch (final Exception ex) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              if (callback != null) callback.onException(ex);
            }
          });
        }
      }
    });
  }
}
