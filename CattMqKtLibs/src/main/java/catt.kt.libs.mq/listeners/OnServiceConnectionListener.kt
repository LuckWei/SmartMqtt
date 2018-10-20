package catt.kt.libs.mq.listeners

import android.content.ComponentName

interface OnServiceConnectionListener {
    fun onServiceConnected(name: ComponentName)
    fun onServiceDisconnected(name: ComponentName)
}