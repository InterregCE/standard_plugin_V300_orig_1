package io.cloudflight.jems.plugin.standard.pre_condition_check.helpers

import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData

object LifecycleDataContainer {
    private var instance: ProjectLifecycleData? = null

    fun get(): ProjectLifecycleData? {
        if (instance == null) throw Exception("ProjectLifecycleData was not initialized!")
        return instance
    }

    fun set(lifecycleData: ProjectLifecycleData) {
        synchronized (this)
        {
            instance = lifecycleData
        }
    }
}