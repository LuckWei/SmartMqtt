package catt.kt.libs.mq.wrapper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log.*


internal abstract class ServiceConnectionImpl(
    private val _connectionType: String,
    private val _implicitAction: String,
    private val _ownCategory: String
) : ServiceConnection {
    private val _TAG: String = ServiceConnectionImpl::class.java.simpleName
    private val _handler: ConnectionHandler by lazy { ConnectionHandler(this@ServiceConnectionImpl) }

    private val _intent: Intent by lazy {
        Intent().apply {
            action = _implicitAction
            addCategory(_ownCategory)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }


    fun inject(context: Context): ServiceConnectionImpl {
        this.context = context
        return this@ServiceConnectionImpl
    }

    protected var context: Context? = null

    protected var binder: IBinder? = null

    fun bindServiceOfMessage() {
        _handler.sendEmptyMessage(MSG_CODE_RECONNECT)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        i(_TAG, "onServiceConnected,  componentName[${name.packageName}/${name.className}]")
        binder = service
    }

    override fun onServiceDisconnected(name: ComponentName) {
        w(_TAG, "onServiceDisconnected, componentName[${name.packageName}/${name.className}]")
        binder = null
    }

    @Throws(IllegalArgumentException::class)
    fun unbindService() {
        binder ?: return
        context?.apply {
            unbindService(this@ServiceConnectionImpl)
        }
        binder = null
    }

    @Throws(Exception::class)
    fun bindService() {
        if (binder == null) {
            i(_TAG, "attempted to bind [$_connectionType] service.")
            if (TextUtils.isEmpty(_implicitAction)) throw IllegalArgumentException("$_connectionType -> method[get_implicitAction()] cannot null.")
            context?.apply {
                bindService(
                    createExplicitFromImplicitIntent(this@apply, _intent)
                        ?: throw NullPointerException("Intent cannot null."),
                    this@ServiceConnectionImpl,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }

    private companion object {
        private const val MSG_CODE_RECONNECT = 10000

        private class ConnectionHandler constructor(private val _root: ServiceConnectionImpl) :
            Handler(Looper.getMainLooper()) {
            private val _TAG: String = ConnectionHandler::class.java.simpleName
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_CODE_RECONNECT -> {
                        try {
                            _root.bindService()
                        } catch (ex: Exception) {
                            e(_TAG, "Unable to bind [${_root._connectionType}] service.", ex)
                        }
                    }
                }
            }
        }


        /***
         * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit _intent,
         * "java.lang.IllegalArgumentException: Service Intent must be explicit"
         *
         * If you are using an implicit _intent, and know only 1 target would answer this _intent,
         * This method will help you turn the implicit _intent into the explicit form.
         *
         * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
         * @param context
         * @param implicitIntent - The original implicit _intent
         * @return Explicit Intent created from the implicit original _intent
         */
        private fun createExplicitFromImplicitIntent(context: Context, implicitIntent: Intent): Intent? {
            context.packageManager.queryIntentServices(implicitIntent, 0)?.apply {
                forEach {
                    if (it.serviceInfo.packageName == context.packageName) return Intent(implicitIntent).apply {
                        component = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
                    }
                }
            }
            return null
        }
    }
}
