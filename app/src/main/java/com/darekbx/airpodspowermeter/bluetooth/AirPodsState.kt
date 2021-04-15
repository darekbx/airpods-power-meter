package com.darekbx.airpodspowermeter.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import java.util.*
import kotlin.math.abs

class AirPodsState(private val bluetoothAdapter: BluetoothAdapter) {

    companion object {
        private const val REPORT_DELAY = 2L
        private const val MIN_RSSI = 60
        private const val DATA_SIZE = 27
        private const val MANUFACTURER = 76
    }

    private lateinit var scanCallback: ScanCallback

    fun obtainState(callback: (StateResult) -> Unit) {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                parseScanResult(result)?.let { stateResult ->
                    callback(stateResult)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { onScanResult(-1, it) }
                super.onBatchScanResults(results)
            }
        }
        leScanner.startScan(getScanFilters(), getScanSettings(), scanCallback)
    }

    fun stopScan() {
        leScanner.stopScan(scanCallback)
    }

    private fun parseScanResult(result: ScanResult?): StateResult? {
        val data = result?.scanRecord?.getManufacturerSpecificData(MANUFACTURER)
        if (result != null && data != null && data.size == DATA_SIZE && abs(result.rssi) < MIN_RSSI) {
            val a: String = decodeHex(data)
            val flip = isFlipped(a)

            // (0-10 batt; 15=disconnected)
            val leftStatus = ("" + a[if (flip) 12 else 13]).toInt(16)
            val rightStatus = ("" + a[if (flip) 13 else 12]).toInt(16)
            val caseStatus = ("" + a[15]).toInt(16)

            // Charge status (bit 0=left; bit 1=right; bit 2=case)
            val charge = ("" + a[14]).toInt(16)

            val chargeL = (if (flip) (charge and 0b00000010) else (charge and 0b00000001)) != 0
            val chargeR = (if (flip) (charge and 0b00000001) else (charge and 0b00000010)) != 0
            val chargeCase = (charge and 0b00000100) != 0

            val leftPod = LeftPod(State(leftStatus == 15, chargeL, statusToPercent(leftStatus)))
            val rightPod = RightPod(State(rightStatus == 15, chargeR, statusToPercent(rightStatus)))
            val case = Case(State(caseStatus == 15, chargeCase, statusToPercent(caseStatus)))
            return StateResult(leftPod, rightPod, case)
        }
        return null
    }

    private fun statusToPercent(status: Int) =
        when {
            status == 10 -> 100
            status < 10 -> status * 10 + 5
            else -> 0
        }

    private fun isFlipped(str: String): Boolean {
        return ("" + str[10]).toInt(16) and 0x02 == 0
    }

    private fun decodeHex(bArr: ByteArray): String {
        val ret = StringBuilder()
        for (b in bArr) ret.append(String.format("%02X", b))
        return ret.toString()
    }

    private fun getScanSettings() =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(REPORT_DELAY)
            .build()

    private fun getScanFilters(): List<ScanFilter> {
        val manufacturerData = ByteArray(27)
        manufacturerData[0] = 7
        manufacturerData[1] = 25

        val manufacturerDataMask = ByteArray(27)
        manufacturerDataMask[0] = -1

        manufacturerDataMask[1] = -1
        val scanFilter = ScanFilter.Builder()
            .setManufacturerData(76, manufacturerData, manufacturerDataMask)
            .build()
        return Collections.singletonList(scanFilter)
    }

    private val leScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
}
