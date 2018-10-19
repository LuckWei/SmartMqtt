package catt.kt.libs.mq.core

interface IConnectionPresenter {
    fun isConnected(): Boolean
    fun connect()
    fun disconnect(quiesceTimeout: Long = 0L)
    fun publishMessage(topic: String, message: String)
    fun subscribe(topics: Array<String>)
    fun destroyOwn()
}