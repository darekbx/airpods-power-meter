package com.darekbx.airpodspowermeter.bluetooth

class State(val disconnected: Boolean, val isCharging: Boolean, val powerPercent: Int, val inEar: Boolean)

class LeftPod(val state: State)

class RightPod(val state: State)

class Case(val state: State)

class StateResult(val leftPod: LeftPod, val rightPod: RightPod, val case: Case)
