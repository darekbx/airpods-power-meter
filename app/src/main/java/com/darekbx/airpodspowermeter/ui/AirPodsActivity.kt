package com.darekbx.airpodspowermeter.ui

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darekbx.airpodspowermeter.R
import com.darekbx.airpodspowermeter.bluetooth.*
import com.darekbx.airpodspowermeter.databinding.ActivityAirpodsBinding
import com.darekbx.airpodspowermeter.utils.PermissionRequester

class AirPodsActivity: AppCompatActivity() {

    private var _binding: ActivityAirpodsBinding? = null
    private val binding get() = _binding!!

    private val bluetoothAdapter by lazy {
        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter
    }

    private val airPodsState by lazy { AirPodsState(bluetoothAdapter) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAirpodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionRequester.runWithPermissions {
            obtainStatus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        airPodsState.stopScan()
    }

    private fun obtainStatus() {
        binding.progressContainer.visibility = View.VISIBLE

        airPodsState.obtainState { stateResult ->
            binding.progressContainer.visibility = View.GONE
            updateLeftPod(stateResult.leftPod)
            updateRightPod(stateResult.rightPod)
            updateCase(stateResult.case)
        }
    }

    private fun updateLeftPod(leftPod: LeftPod) {
        with (leftPod.state) {
            if (!disconnected) {
                binding.leftPodIndicator.power = powerPercent
                binding.leftPodPercent.text = "${powerPercent}%"
                binding.leftPodChargeIndicator.isVisible(isCharging)
                binding.leftPodEarIndicator.isVisible(inEar)
            } else {
                binding.leftPodIndicator.visibility = View.GONE
                binding.leftPodPercent.visibility = View.GONE
                binding.leftPodChargeIndicator.visibility = View.GONE
                binding.leftPodEarIndicator.visibility = View.GONE
            }
        }
    }
    private fun updateRightPod(rightPod: RightPod) {
        with (rightPod.state) {
            if (!disconnected) {
                binding.rightPodIndicator.power = powerPercent
                binding.rightPodPercent.text = "${powerPercent}%"
                binding.rightPodChargeIndicator.isVisible(isCharging)
                binding.rightPodEarIndicator.isVisible(inEar)
            } else {
                binding.rightPodIndicator.visibility = View.GONE
                binding.rightPodPercent.visibility = View.GONE
                binding.rightPodChargeIndicator.visibility = View.GONE
                binding.rightPodEarIndicator.visibility = View.GONE
            }
        }
    }
    
    private fun updateCase(case: Case) {
        with (case.state) {
            if (!disconnected) {
                binding.caseIndicator.power = powerPercent
                binding.casePercent.text = "${powerPercent}%"
                binding.caseChargeIndicator.isVisible(isCharging)
            } else {
                binding.caseIndicator.visibility = View.GONE
                binding.casePercent.visibility = View.GONE
                binding.caseChargeIndicator.visibility = View.GONE
            }
        }
    }

    private val permissionRequester by lazy {
        PermissionRequester(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            onDenied = { showRationale() },
            onShowRationale = { showDeniedPermissionInformation() }
        )
    }

    private fun showRationale() {
        Toast.makeText(this, R.string.location_permission_rationale, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showDeniedPermissionInformation() {
        Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT)
            .show()
    }
    
    private fun View.isVisible(value: Boolean) {
        this.visibility = if (value) View.VISIBLE else View.GONE
    }
}
