package io.cloudflight.jems.plugin.standard.pre_condition_check.helpers

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData

object CallDataContainer {
    private var instance: CallDetailData? = null

    fun get(): CallDetailData {
        if (instance == null) throw Exception("CallData was not initialized!")
        return instance!!
    }

    fun set(callData: CallDetailData) {
        synchronized (this)
        {
            instance = callData
        }
    }
}
