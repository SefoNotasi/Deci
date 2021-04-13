package ru.notasi.deci;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class Shake implements SensorEventListener {
    // Delay between shakes.
    private static final int DELAY_DURATION = Constants.ANIM_DURATION_FULL;
    // Minimum number of movements to register a shake.
    private static final int MIN_MOVEMENTS = 2;
    // Maximum time (in milliseconds) for the whole shake to occur.
    private static final int MAX_SHAKE_DURATION = 500;
    // Indexes for X, Y and Z values.
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    // Arrays to store gravity and linear acceleration values.
    private final float[] mGravity = {0.0f, 0.0f, 0.0f};
    private final float[] mLinearAcceleration = {0.0f, 0.0f, 0.0f};
    // OnShakeListener that will be notified when the shake is detected.
    private final OnShakeListener mShakeListener;
    // Minimum acceleration needed to count as a shake movement.
    private int mShakeSense; // private static final int MIN_SHAKE_ACCELERATION = 5;
    private long mLastTimeShake = 0;
    // Start time for the shake detection.
    private long mStartTime = 0;

    // Counter for shake movements.
    private int mMoveCount = 0;

    // Constructor that sets the shake listener.
    public Shake(OnShakeListener shakeListener) {
        mShakeListener = shakeListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This method will be called when the accelerometer detects a change.

        // Check if last time of shaking is less than delay.
        if (System.currentTimeMillis() - mLastTimeShake < DELAY_DURATION) return;

        // Call a helper method that wraps code from the Android developer site.
        setCurrentAcceleration(event);

        // Get the max linear acceleration in any direction.
        float maxLinearAcceleration = getMaxCurrentLinearAcceleration();

        // Check if the acceleration is greater than our minimum threshold.
        if (maxLinearAcceleration > mShakeSense) {
            long now = System.currentTimeMillis();

            // Set the mStartTime if it was reset to zero.
            if (mStartTime == 0) {
                mStartTime = now;
            }

            long elapsedTime = now - mStartTime;

            // Check if we're still in the shake window we defined.
            if (elapsedTime > MAX_SHAKE_DURATION) {
                // Too much time has passed. Start over!
                resetShakeDetection();
            } else {
                // Keep track of all the movements.
                mMoveCount++;

                // Check if enough movements have been made to qualify as a shake.
                if (mMoveCount > MIN_MOVEMENTS) {
                    // Get current time for delay between shakes.
                    mLastTimeShake = System.currentTimeMillis();

                    // It's a shake! Notify the listener.
                    mShakeListener.onShake();

                    // Reset for the next one!
                    resetShakeDetection();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Intentionally blank.
    }

    private void setCurrentAcceleration(SensorEvent event) {
        /*
         *  BEGIN SECTION from Android developer site.
         *  This code accounts for gravity using a high-pass filter.
         */

        // Alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate.

        final float alpha = 0.8f;

        // Gravity components of X, Y and Z acceleration.
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * event.values[X];
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * event.values[Y];
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * event.values[Z];

        // Linear acceleration along the X, Y and Z axes (gravity effects removed).
        mLinearAcceleration[X] = event.values[X] - mGravity[X];
        mLinearAcceleration[Y] = event.values[Y] - mGravity[Y];
        mLinearAcceleration[Z] = event.values[Z] - mGravity[Z];

        /*
         *  END SECTION from Android developer site.
         */
    }

    private float getMaxCurrentLinearAcceleration() {
        // Start by setting the value to the X value.
        float maxLinearAcceleration = mLinearAcceleration[X];

        // Check if the Y value is greater.
        if (mLinearAcceleration[Y] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Y];
        }

        // Check if the Z value is greater.
        if (mLinearAcceleration[Z] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Z];
        }

        // Return the greatest value.
        return maxLinearAcceleration;
    }

    private void resetShakeDetection() {
        mStartTime = 0;
        mMoveCount = 0;
    }

    public int getSense() {
        return mShakeSense;
    }

    public void setSense(int sense) {
        mShakeSense = sense;
    }

    // (I'd normally put this definition in it's own .java file).
    public interface OnShakeListener {
        void onShake();
    } // TODO: implement interface
}