package catt.kt.libs.mq.listeners

interface OnPublishDeliveryListener {
    fun onDeliveryComplete(payload: ByteArray)
}