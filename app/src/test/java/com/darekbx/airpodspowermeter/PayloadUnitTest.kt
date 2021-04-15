package com.darekbx.airpodspowermeter

import android.util.Log
import org.junit.Test

import org.junit.Assert.*

class PayloadUnitTest {

    @Test
    fun payload_check() {
        val payload = "121900207C982959FA8272A7BF5610906652403AB59D05367D0300"

        val flip = isFlipped(payload)
        val leftStatus = ("" + payload[if (flip) 12 else 13]).toInt(16) // Left airpod (0-10 batt; 15=disconnected)
        val rightStatus = ("" + payload[if (flip) 13 else 12]).toInt(16) // Right airpod (0-10 batt; 15=disconnected)
        val caseStatus = ("" + payload[15]).toInt(16) // Case (0-10 batt; 15=disconnected)
        val chargeStatus = ("" + payload[14]).toInt(16) // Charge status (bit 0=left; bit 1=right; bit 2=case)

        Log.v("--------- LEFT", if (leftStatus === 10) "100%" else if (leftStatus < 10) (leftStatus * 10 + 5).toString() + "%" else "")
        Log.v("--------- RIGHT", if (rightStatus === 10) "100%" else if (rightStatus < 10) (rightStatus * 10 + 5).toString() + "%" else "")
        Log.v("--------- CASE", if (caseStatus === 10) "100%" else if (caseStatus < 10) (caseStatus * 10 + 5).toString() + "%" else "")

    }

    private fun isFlipped(str: String): Boolean {
        return ("" + str[10]).toInt(16) and 0x02 == 0
    }

}
