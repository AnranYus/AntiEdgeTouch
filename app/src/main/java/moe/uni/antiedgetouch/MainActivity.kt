package moe.uni.antiedgetouch

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.uni.antiedgetouch.ui.theme.AntiEdgeTouchTheme

class MainActivity : ComponentActivity() {
    private var binder: FloatingWindowService.LocalBinder? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as FloatingWindowService.LocalBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContent {
            AntiEdgeTouchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ControlPanel(this) {
                        binder?.getService()?.changeWindowWidth(it)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binder?.getService()?.showBackground()
    }

    override fun onPause() {
        super.onPause()
        binder?.getService()?.hideBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
    }

    private fun initWindow() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1)
        } else {
            val intent = Intent(this, FloatingWindowService::class.java)
            startService(intent)
            bindService(intent, mConnection, Activity.BIND_AUTO_CREATE)
        }

    }
}

@Composable
fun ControlPanel(context: Context, onWidthChange: (Int) -> Unit) {
    Row {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            var value by remember {
                val width = context.getSharedPreferences(Definition.SP_NAME, Context.MODE_PRIVATE)
                    .getInt(Definition.WINDOW_WIDTH, 0) * 0.01f
                mutableFloatStateOf(width)
            }
            Slider(value = value, onValueChange = {
                val progress = it * 100
                value = it
                onWidthChange.invoke(progress.toInt())
            }, onValueChangeFinished = {
                val progress = value * 100
                context.getSharedPreferences(Definition.SP_NAME, Context.MODE_PRIVATE).edit()
                    .putInt(Definition.WINDOW_WIDTH, progress.toInt()).apply()
            }, modifier = Modifier
                .width(300.dp)
            )
        }

    }
}
