package com.topanimestream.custom;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CoordinatedHeader extends FrameLayout {

    /** The float array used to store each y-coordinate */
    private final float[] mCoordinates = new float[5];

    /** True if the header is currently animating, false otherwise */
    public boolean mAnimating;

    /**
     * Constructor for <code>CoordinatedHeader</code>
     *
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public CoordinatedHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Animates the header to the stored y-coordinate at the given index
     *
     * @param index The index used to retrieve the stored y-coordinate
     * @param duration Sets the duration for the underlying {@link Animator}
     */
    public void restoreCoordinate(int index, int duration) {
        // Find the stored value for the index
        final float y = mCoordinates[index];
        // Animate the header to the y-coordinate
        animate().y(y).setDuration(duration).setListener(mAnimatorListener).start();
    }

    /**
     * Saves the given y-coordinate at the specified index, the animates the
     * header to the requested value
     *
     * @param index The index used to store the given y-coordinate
     * @param y The y-coordinate to save
     */
    public void storeCoordinate(int index, float y) {
        if (mAnimating) {
            // Don't store any coordinates while the header is animating
            return;
        }
        // Save the current y-coordinate
        mCoordinates[index] = y;
        // Animate the header to the y-coordinate
        restoreCoordinate(index, 0);
    }

    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            mAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            mAnimating = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationStart(Animator animation) {
            mAnimating = true;
        }
    };

}