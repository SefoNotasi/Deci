package ru.notasi.deci;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.Random;

public class CoinFragment extends Fragment {
    private final Random mRandom = new Random();
    private MainActivity mActivity;
    private FloatingActionButton mShareButton;
    private ExtendedFloatingActionButton mAutoButton;
    private ImageView mImageCoin;
    private Shake mShake;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SoundPool mSoundPool;
    private LinearProgressIndicator mProgress;
    private boolean mIsAutoOn = false;
    private Handler mHandler;
    private Runnable mRunnable;
    private Repository mRepo;
    private int mCountFlips,
            mCountHeads,
            mCountTails,
            mCountEdges,
            mIdImageHeads,
            mIdImageTails,
            mIdImageEdge,
            mLevel,
            mDifficulty,
            mExperience,
            mExpRequired,
            mSkin,
            mSoundHeads,
            mSoundTails,
            mSoundEdge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mRepo = new Repository(mActivity);
        mLevel = mRepo.getLevel();
        mDifficulty = mRepo.getDifficulty();
        mExperience = mRepo.getExperience();
        mExpRequired = mRepo.getExpRequired();
        mSkin = mRepo.getSkin();
        mCountFlips = mRepo.getCountFlips();
        mCountHeads = mRepo.getCountHeads();
        mCountTails = mRepo.getCountTails();
        mCountEdges = mRepo.getCountEdges();

        mIdImageHeads = getResources().getIdentifier(Constants.RES_IMAGE_COIN_HEADS, Constants.RES_TYPE_DRAWABLE, mActivity.getPackageName());
        mIdImageTails = getResources().getIdentifier(Constants.RES_IMAGE_COIN_TAILS, Constants.RES_TYPE_DRAWABLE, mActivity.getPackageName());
        mIdImageEdge = getResources().getIdentifier(Constants.RES_IMAGE_COIN_EDGE, Constants.RES_TYPE_DRAWABLE, mActivity.getPackageName());
        mImageCoin = view.findViewById(R.id.image_coin);
        mImageCoin.setImageResource(mRepo.getIdImageCoin());
        mImageCoin.setOnClickListener(view1 -> {
            if (!mIsAutoOn) flip();
        });

        mProgress = view.findViewById(R.id.progress_bar);
        mProgress.setMax(mExpRequired);
        setProgress(mExperience);
        setSkin();

        ImageButton soundButton = view.findViewById(R.id.button_sound);
        soundButton.setSelected(mRepo.getSound());
        soundButton.setOnClickListener(view14 -> {
            if (mRepo.getSound()) {
                mRepo.setSound(false);
                soundButton.setSelected(false);
                if (mRepo.getTips())
                    mActivity.showToast(getString(R.string.toast_sound_off));
            } else {
                mRepo.setSound(true);
                soundButton.setSelected(true);
                if (mRepo.getTips()) mActivity.showToast(getString(R.string.toast_sound_on));
            }
        });

        ImageButton shakeButton = view.findViewById(R.id.button_shake);
        shakeButton.setSelected(mRepo.getShake());
        shakeButton.setOnClickListener(view15 -> {
            if (mRepo.getShake()) {
                mRepo.setShake(false);
                shakeButton.setSelected(false);
                if (mRepo.getTips()) mActivity.showToast(getString(R.string.toast_shake_off));
            } else {
                mRepo.setShake(true);
                shakeButton.setSelected(true);
                if (mRepo.getTips()) mActivity.showToast(getString(R.string.toast_shake_on));
            }
        });

        ((FloatingActionButton) mActivity.findViewById(R.id.button_action)).hide();
        mShareButton = mActivity.findViewById(R.id.button_share);
        setShareButton();
        mShareButton.setOnClickListener(view12 -> {
            Intent intentShare = ShareCompat.IntentBuilder.from(mActivity)
                    .setType(Constants.INTENT_TYPE_TEXT)
                    .setText(getString(R.string.text_share_flip,
                            mRepo.getLastFlipShare(),
                            getString(R.string.text_share_ad,
                                    getString(R.string.link_store))))
                    .getIntent();
            startActivity(intentShare);
        });

        mAutoButton = mActivity.findViewById(R.id.button_auto);
        mAutoButton.setIconResource(R.drawable.ic_baseline_play_arrow_24);
        mAutoButton.shrink();
        mAutoButton.show();
        mAutoButton.setOnClickListener(view13 -> {
            if (mIsAutoOn) {
                stopAutoFlip();
                if (mRepo.getTips()) {
                    Snackbar.make(getView(), getString(R.string.snack_auto_off),
                            Snackbar.LENGTH_LONG).setAnchorView(mShareButton).show();
                }
            } else {
                startAutoFlip();
                if (mRepo.getTips()) {
                    Snackbar.make(getView(), getString(R.string.snack_auto_on),
                            Snackbar.LENGTH_LONG).setAnchorView(mShareButton).show();
                }
            }
        });

        createSoundPool();
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShake = new Shake(() -> {
            if (mRepo.getShake() & !mIsAutoOn) flip();
        });
        mShake.setSense(mRepo.getShakeSense());

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                flip();
                mHandler.postDelayed(this, Constants.DELAY_AUTO_FLIP);
            }
        };

        if (mRepo.getCountFlipsForRate() >= 30) {
            mRepo.setCountFlipsForRate(0);
            rateApp();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createSoundPool();
        mSensorManager.registerListener(mShake, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSoundPool.release();
        mSoundPool = null;
        mSensorManager.unregisterListener(mShake);
        if (mIsAutoOn) stopAutoFlip();
    }

    private void startAutoFlip() {
        mIsAutoOn = true;
        mAutoButton.setIconResource(R.drawable.ic_outline_play_arrow_24);
        mAutoButton.extend();
        mHandler.postDelayed(mRunnable, Constants.DELAY_AUTO_FLIP);
    }

    private void stopAutoFlip() {
        mIsAutoOn = false;
        mAutoButton.setIconResource(R.drawable.ic_baseline_play_arrow_24);
        mAutoButton.shrink();
        mHandler.removeCallbacks(mRunnable);
    }

    private void setShareButton() {
        if (mCountFlips == 0) {
            mShareButton.hide();
        } else {
            mShareButton.show();
        }
    }

    private void getLevelUp() {
        mLevel++;
        mRepo.setLevel(mLevel);

        mDifficulty++;
        mRepo.setDifficulty(mDifficulty);

        mExperience = 0;
        mRepo.setExperience(mExperience);

        mExpRequired *= mDifficulty;
        mRepo.setExpRequired(mExpRequired);

        if (mIsAutoOn) stopAutoFlip();
        if (mLevel < Constants.COUNT_SKINS) {
            mSkin = mLevel;
            mRepo.setSkin(mSkin);
            int badges = mActivity.getBadges(R.id.nav_board);
            mActivity.setBadges(R.id.nav_board, ++badges);
        }

        mProgress.setMax(mExpRequired);
        setProgress(mExperience);
        setSkin();

        if (mRepo.getTips()) {
            Snackbar.make(getView(),
                    mLevel < Constants.COUNT_SKINS ? getString(R.string.snack_skin, mLevel) : getString(R.string.snack_level, mLevel),
                    Snackbar.LENGTH_LONG).setAnchorView(mShareButton).show();
        }
    }

    private void setSkin() {
        switch (mSkin) {
            case 0: // Silver
                mImageCoin.clearColorFilter();
                break;
            case 1: // Gold #FFC800
                mImageCoin.setColorFilter(Color.argb(255, 255, 200, 0), PorterDuff.Mode.MULTIPLY);
                break;
            case 2: // Cobalt #00FFFF
                mImageCoin.setColorFilter(Color.argb(255, 0, 255, 255), PorterDuff.Mode.MULTIPLY);
                break;
            case 3: // Mithril #00FF00
                mImageCoin.setColorFilter(Color.argb(255, 0, 255, 0), PorterDuff.Mode.MULTIPLY);
                break;
            case 4: // Orichalc #FF00FF
                mImageCoin.setColorFilter(Color.argb(255, 255, 0, 255), PorterDuff.Mode.MULTIPLY);
                break;
            case 5: // Adamant #FF3200
                mImageCoin.setColorFilter(Color.argb(255, 255, 50, 0), PorterDuff.Mode.MULTIPLY);
                break;
            default: // Luminite
                setSkinMax();
                break;
        }
    }

    private void setSkinMax() {
        float[] hsv = {0, 1, 1}; // Transition color.
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(animation -> {
            hsv[0] = 360 * animation.getAnimatedFraction();
            mImageCoin.setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.MULTIPLY);
        });
        valueAnimator.start();
        valueAnimator.setDuration(10_000);
        valueAnimator.setRepeatCount(Animation.INFINITE);
    }

    private void setProgress(int experience) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgress.setProgress(experience, true);
        } else {
            mProgress.setProgress(experience);
        }
    }

    private void rateApp() {
        ReviewManager manager = ReviewManagerFactory.create(mActivity);
        // Request a ReviewInfo object.
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object.
                ReviewInfo reviewInfo = task.getResult();

                // Launch the in-app review flow.
                Task<Void> flow = manager.launchReviewFlow(mActivity, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, log or handle the error code.
                // @ReviewErrorCode int reviewErrorCode = ((TaskException) task.getException()).getErrorCode();
            }
        });
    }

    public void flip() {
        if (mCountFlips == 0) mShareButton.show();
        mCountFlips++;
        mRepo.setCountFlips(mCountFlips);

        int side = mRandom.nextInt(101); // 0-100 (101).
        if (side == 100 & mRepo.getIdImageCoin() != mIdImageEdge) { // 1 of 101, remain: 0-99 (100).
            edge();
        } else if (side >= 50) { // 50-99 (50%), remain: 0-49 (50).
            heads();
        } else { // 0-49 (50%). Unwrapped condition: if (side < 50).
            tails();
        }

        setProgress(mExperience);
        mRepo.setCountFlipsForRate(mRepo.getCountFlipsForRate() + 1);
    }

    private void heads() {
        mExperience += 2;
        mRepo.setExperience(mExperience);
        mCountHeads++;
        mRepo.setCountHeads(mCountHeads);
        mRepo.setLastFlipShare(getString(R.string.text_last_heads));
        mRepo.setLastFlipStats(getString(R.string.text_heads));

        animate(mIdImageHeads);
        if (mRepo.getTips())
            mActivity.showToast(getString(R.string.toast_heads));
        if (mRepo.getSound()) playSound(1);
        if (mRepo.getVibro()) vibrate(1);
    }

    private void tails() {
        mExperience++;
        mRepo.setExperience(mExperience);
        mCountTails++;
        mRepo.setCountTails(mCountTails);
        mRepo.setLastFlipShare(getString(R.string.text_last_tails));
        mRepo.setLastFlipStats(getString(R.string.text_tails));

        animate(mIdImageTails);
        if (mRepo.getTips())
            mActivity.showToast(getString(R.string.toast_tails));
        if (mRepo.getSound()) playSound(2);
        if (mRepo.getVibro()) vibrate(2);
    }

    private void edge() {
        mExperience += 100;
        mRepo.setExperience(mExperience);
        mCountEdges++;
        mRepo.setCountEdges(mCountEdges);
        mRepo.setLastFlipShare(getString(R.string.text_last_edge));
        mRepo.setLastFlipStats(getString(R.string.text_edges));

        animate(mIdImageEdge);
        if (mRepo.getTips())
            mActivity.showToast(getString(R.string.toast_edge));
        if (mRepo.getSound()) playSound(3);
        if (mRepo.getVibro()) vibrate(3);
    }

    private void animate(int side) {
        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mImageCoin, Constants.ANIM_SCALE_X, 1f, 0f);
        final ObjectAnimator animator2 = ObjectAnimator.ofFloat(mImageCoin, Constants.ANIM_SCALE_X, 0f, 1f);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mImageCoin.setImageResource(side);
                mRepo.setIdImageCoin(side);
                if (mExperience >= mExpRequired) getLevelUp();
                animator2.start();
            }
        });
        animator1.start();
        animator1.setDuration(Constants.ANIM_DURATION_HALF);
        animator2.setDuration(Constants.ANIM_DURATION_HALF);
    }

    private void vibrate(int side) {
        Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            int mVibroForce = mRepo.getVibroForce();
            switch (side) {
                case 1:
                    long headsVibroPattern = 300;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(headsVibroPattern, mVibroForce));
                    } else {
                        vibrator.vibrate(headsVibroPattern);
                    }
                    break;
                case 2:
                    long[] tailsVibroPattern = {100, 50, 100};
                    int[] tailsVibroAmplitudes = {mVibroForce, 0, mVibroForce};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(tailsVibroPattern, tailsVibroAmplitudes, -1));
                    } else {
                        vibrator.vibrate(tailsVibroPattern, -1);
                    }
                    break;
                case 3:
                    long[] edgeVibroPattern = {100, 50, 100, 50, 100};
                    int[] edgeVibroAmplitudes = {mVibroForce, 0, mVibroForce, 0, mVibroForce};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(edgeVibroPattern, edgeVibroAmplitudes, -1));
                    } else {
                        vibrator.vibrate(edgeVibroPattern, -1);
                    }
                    break;
            }
        }
    }

    private void playSound(int side) {
        if (mSoundPool != null) {
            mSoundPool.autoPause();
            float mSoundVolume = mRepo.getVolume();
            switch (side) {
                case 1:
                    mSoundPool.play(mSoundHeads, mSoundVolume, mSoundVolume, 0, 0, 1);
                    break;
                case 2:
                    mSoundPool.play(mSoundTails, mSoundVolume, mSoundVolume, 0, 0, 1);
                    break;
                case 3:
                    mSoundPool.play(mSoundEdge, mSoundVolume, mSoundVolume, 0, 0, 1);
                    break;
            }
        } else {
            // TODO: Log or createSoundPool();
        }
    }

    private void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA) // USAGE_ASSISTANCE_SONIFICATION
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); // STREAM_NOTIFICATION
        }

        mSoundHeads = mSoundPool.load(mActivity, R.raw.sound_heads, 1);
        mSoundTails = mSoundPool.load(mActivity, R.raw.sound_tails, 1);
        mSoundEdge = mSoundPool.load(mActivity, R.raw.sound_edge, 1);
        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC); // STREAM_NOTIFICATION
    }
}