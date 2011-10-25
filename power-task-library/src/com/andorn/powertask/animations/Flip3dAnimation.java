package com.andorn.powertask.animations;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

public class Flip3dAnimation extends Animation {

	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterX;
	private final float mCenterY;
	private Camera mCamera;

	public Flip3dAnimation(View currentView, View nextView, float fromDegrees, float toDegrees, float centerX,
			float centerY) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
		
		setDuration(500);
		setFillAfter(true);
		setInterpolator(new AccelerateInterpolator());
		setAnimationListener(new DisplayNextView(currentView, nextView, fromDegrees, toDegrees));
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees
				+ ((mToDegrees - fromDegrees) * interpolatedTime);

		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;

		final Matrix matrix = t.getMatrix();

		camera.save();

		camera.rotateY(degrees);

		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}

	public static void applyRotation(View currentView, View nextView, float start,
			float end) {
		// Find the center of image
		final float centerX = currentView.getWidth() / 2.0f;
		final float centerY = currentView.getHeight() / 2.0f;

		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final Flip3dAnimation rotation = new Flip3dAnimation(currentView, nextView, start, end,
				centerX, centerY);

		currentView.startAnimation(rotation);
	}

	public final class DisplayNextView implements Animation.AnimationListener {
		View mCurrentView;
		View mNextView;
		float mStart;
		float mEnd;

		public DisplayNextView(View currentView, View nextView, float start,
				float end) {
			mCurrentView = currentView;
			mNextView = nextView;
			mStart = start;
			mEnd = end;
		}

		public void onAnimationStart(Animation animation) { }

		public void onAnimationEnd(Animation animation) {
			mCurrentView.post(new SwapViews(mCurrentView, mNextView, mStart, mEnd));
		}

		public void onAnimationRepeat(Animation animation) { }
	}

	public final class SwapViews implements Runnable {
		View mCurrentView;
		View mNextView;
		float mStart;
		float mEnd;

		public SwapViews(View currentView, View nextView, float start,
				float end) {
			mCurrentView = currentView;
			mNextView = nextView;
			mStart = start;
			mEnd = end;
		}

		public void run() {
			final float centerX = mCurrentView.getWidth() / 2.0f;
			final float centerY = mCurrentView.getHeight() / 2.0f;

			mCurrentView.setVisibility(View.GONE);
			mNextView.setVisibility(View.VISIBLE);
			mNextView.requestFocus();

			Flip3dAnimation rotation = new Flip3dAnimation(mCurrentView, mNextView, mStart, mEnd, centerX, centerY);

			rotation.setDuration(500);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new DecelerateInterpolator());

			mNextView.startAnimation(rotation);
		}
	}
}