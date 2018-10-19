package catt.kt.libs.mq.listeners

interface OnConnectionListener {
    fun onConnectComplete(reconnect: Boolean, serverURI: String)
    fun onConnectionLost(cause: Throwable)
    fun onConnecting(serverURI: String)
}