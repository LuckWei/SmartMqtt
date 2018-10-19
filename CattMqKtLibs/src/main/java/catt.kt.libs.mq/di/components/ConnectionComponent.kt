package catt.kt.libs.mq.di.components

import catt.kt.libs.mq.core.MqConnectionService
import catt.kt.libs.mq.di.modules.ConnectionModule
import catt.kt.libs.mq.di.scopes.ConnectionScope
import dagger.Component

@ConnectionScope
@Component(dependencies = [MqAppComponent::class], modules = [ConnectionModule::class])
interface ConnectionComponent {

    fun inject(service: MqConnectionService)
}