package com.ibm.health.common.navigation.android

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract

public abstract class IntentNavContract<I : IntentNav, R : Parcelable> :
    ActivityResultContract<I, IntentNavContractResult<R?>>() {

    override fun createIntent(context: Context, input: I): Intent =
        input.toIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?): IntentNavContractResult<R?> =
        IntentNavContractResult(resultCode, intent.getOptionalArgs())
}

public data class IntentNavContractResult<T>(
    val resultCode: Int,
    val value: T,
)
