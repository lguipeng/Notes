/*
 * Copyright 2015 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.client.android.helper;

import android.util.Log;

import java.util.Locale;

/**
 * A logging utility, which helps printing formatted {@link String}s.
 *
 * @author rwondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class Cat {

    private final String mTag;

    public Cat(String tag) {
        mTag = tag == null ? "" : tag;
    }

    public void d(String message) {
        println(Log.DEBUG, message, null);
    }

    public void d(String message, Object... args) {
        println(Log.DEBUG, format(message, args), null);
    }

    public void d(Throwable t, String message, Object... args) {
        println(Log.DEBUG, format(message, args), t);
    }

    public void i(String message) {
        println(Log.INFO, message, null);
    }

    public void i(String message, Object... args) {
        println(Log.INFO, format(message, args), null);
    }

    public void i(Throwable t, String message, Object... args) {
        println(Log.INFO, format(message, args), t);
    }

    public void w(String message) {
        println(Log.WARN, message, null);
    }

    public void w(String message, Object... args) {
        println(Log.WARN, format(message, args), null);
    }

    public void w(Throwable t, String message, Object... args) {
        println(Log.WARN, format(message, args), t);
    }

    public void w(Throwable t) {
        if (t == null) {
            t = new Exception("null exception logged");
        }
        println(Log.WARN, t.getMessage(), t);
    }

    public void e(Throwable t) {
        if (t == null) {
            t = new Exception("null exception logged");
        }
        println(Log.ERROR, t.getMessage(), t);
    }

    public void e(String message) {
        println(Log.ERROR, message, null);
    }

    public void e(String message, Object... args) {
        println(Log.ERROR, format(message, args), null);
    }

    public void e(Throwable t, String message, Object... args) {
        println(Log.ERROR, format(message, args), t);
    }

    public void v(String message) {
        println(Log.VERBOSE, message, null);
    }

    public void v(String message, Object... args) {
        println(Log.VERBOSE, format(message, args), null);
    }

    public void v(Throwable t, String message, Object... args) {
        println(Log.VERBOSE, format(message, args), t);
    }

    private void println(int priority, String message, Throwable t) {
        if (message == null) {
            message = "";
        }

        if (t != null) {
            message += '\n' + Log.getStackTraceString(t);
        }
        Log.println(priority, mTag, message);
    }

    private static String format(String message, Object[] args) {
        if (message == null) {
            return "null";
        } else {
            return String.format(Locale.US, message, args);
        }
    }
}

