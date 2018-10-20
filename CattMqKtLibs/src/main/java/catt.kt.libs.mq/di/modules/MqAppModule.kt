package catt.kt.libs.mq.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MqAppModule(private val _context: Context) {

    @Singleton
    @Provides
    fun provideContext() = _context
}