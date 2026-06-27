package com.classisland.android.rust

import android.opengl.GLES20
import android.util.Log
import com.classisland.android.util.Constants

class GpuAccelerator {
    companion object {
        private var nativeLoaded = false
        init { try { System.loadLibrary("classisland_rust"); nativeLoaded = true } catch (_: UnsatisfiedLinkError) {} }
        fun isNative() = nativeLoaded
    }

    private var ok = false

    fun init(): Boolean = try {
        if (nativeLoaded) nativeInit()
        ok = true; Log.i(Constants.TAG, "GPU init OK"); true
    } catch (e: Exception) { Log.w(Constants.TAG, "GPU init fail: ${e.message}"); false }

    fun render(items: String, w: Int, h: Int, bg: Int, fg: Int): Boolean = try {
        if (nativeLoaded) nativeRender(items, w, h, bg, fg)
        else { GLES20.glClearColor(((bg shr 16)and0xFF)/255f, ((bg shr 8)and0xFF)/255f, (bg and 0xFF)/255f, ((bg shr 24)and0xFF)/255f); GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) }
        true
    } catch (e: Exception) { false }

    fun destroy() { try { if (nativeLoaded) nativeDestroy() } catch (_: Exception) {} }

    private external fun nativeInit(): Boolean
    private external fun nativeRender(items: String, w: Int, h: Int, bg: Int, fg: Int): Boolean
    private external fun nativeDestroy()
}