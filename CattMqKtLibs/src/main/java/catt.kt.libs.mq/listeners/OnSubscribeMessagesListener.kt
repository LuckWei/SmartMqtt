package catt.kt.libs.mq.listeners

interface OnSubscribeMessagesListener {
    fun onSubscribeMessage(topic: String, id: Int, payload: ByteArray, qos: Int, repeated: Boolean, retained: Boolean)
}