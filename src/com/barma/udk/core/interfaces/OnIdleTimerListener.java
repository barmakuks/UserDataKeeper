package com.barma.udk.core.interfaces;

/**
 * Created by Vitalii Misiura on 12/31/14.
 */
public interface OnIdleTimerListener {
    void onTimerStart(int secondsLeft);
    void onTimer(int secondsLeft);
    void onTimeOver();
}
