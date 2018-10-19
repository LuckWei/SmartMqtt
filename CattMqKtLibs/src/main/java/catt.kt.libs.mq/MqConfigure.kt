package catt.kt.libs.mq

import android.annotation.SuppressLint

@SuppressLint("StaticFieldLeak")
object MqConfigure {
    /**
     * 需要初始化
     */
    @JvmStatic
    var deviceNo: String = ""

    /**
     * Mqtt client id
     * 连接前需要初始化
     */
    @JvmStatic
    var clientId: String = ""

    /**
     * Mqtt subscribe topics
     * 连接前需要初始化
     */
    @JvmStatic
    var topics: Array<String> = arrayOf()

    @JvmStatic
    var userName: String = ""

    @JvmStatic
    var password: String = ""

    @JvmStatic
    var serverUrl: String = ""

    /**
     * 0 服务质量普通
     * 1 服务质量高
     * 2 服务质量最高
     */
    @JvmStatic
    var qos: Int = 1

    /**
     * 是否开启MQ日志
     */
    @JvmStatic
    var isTrace: Boolean = true

    /**
     * 超时时间,单位是秒(example: 30s)
     */
    @JvmStatic
    var connectionTimeout: Int = 10
    /**
     * 保持连接时间周期,单位是秒(example: 60s)
     */
    @JvmStatic
    var keepAliveInterval: Int = 60

    /**
     * 后台是否保持连接常态化
     * true 不保持， false 保持
     */
    @JvmStatic
    var isCleanSession: Boolean = false

    /**
     * 最大容量
     * PS:高流量传输中需要增大此值
     */
    @JvmStatic
    var maxInflight: Int = 1000

    @JvmStatic
    var bufferSize: Int = Integer.MAX_VALUE
}