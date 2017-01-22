package com.barma.udk.core.interfaces;

/**
 * Created by vitalii on 12/24/14.
 */
public interface ILongActionCallback {
    void onActionBegin(final Object args);
    void onActionStep(final Object args);
    void onActionDone(final Object args);
}
