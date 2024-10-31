package moe.uni.antiedgetouch

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager


class FloatingWindowService : Service() {
    private lateinit var leftFloatingView : View
    private lateinit var rightFloatingView: View
    private val params by lazy {
        val width = getSharedPreferences(Definition.SP_NAME, MODE_PRIVATE).getInt(Definition.WINDOW_WIDTH, 20)

        WindowManager.LayoutParams(
            width,  // 宽度 100dp
            WindowManager.LayoutParams.MATCH_PARENT,  // 高度填充屏幕
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
    }
    private lateinit var windowManager: WindowManager

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    inner class LocalBinder: Binder(){
        fun getService(): FloatingWindowService = this@FloatingWindowService
    }


    fun showBackground(){
        leftFloatingView.setBackgroundColor(Color.RED)
        rightFloatingView.setBackgroundColor(Color.RED)
    }

    fun hideBackground(){
        leftFloatingView.setBackgroundColor(Color.TRANSPARENT)
        rightFloatingView.setBackgroundColor(Color.TRANSPARENT)
    }

    fun changeWindowWidth(value:Int){
        params.width = value
        params.gravity = Gravity.START or Gravity.TOP
        windowManager.updateViewLayout(leftFloatingView, params)

        params.gravity = Gravity.END or Gravity.TOP
        windowManager.updateViewLayout(rightFloatingView, params)
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        leftFloatingView = LayoutInflater.from(this).inflate(R.layout.side_floating_window, null)
        rightFloatingView =  LayoutInflater.from(this).inflate(R.layout.side_floating_window, null)

        params.gravity = Gravity.START or Gravity.TOP
        params.x = 0
        params.y = 0
        windowManager.addView(leftFloatingView, params)

        params.gravity = Gravity.END or Gravity.TOP
        windowManager.addView(rightFloatingView, params)
        showBackground()
    }
}