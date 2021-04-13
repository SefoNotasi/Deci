package ru.notasi.deci;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class BoardFragment extends Fragment {
    private MainActivity mActivity;
    private Repository mRepo;
    private FloatingActionButton mActionButton;
    private RadioGroup mRadioGroupSkins;
    private TextView mViewLevel,
            mViewExperience,
            mViewLastResult,
            mViewCountFlips,
            mViewCountHeads,
            mViewCountTails,
            mViewCountEdges;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mRepo = new Repository(mActivity);

        mViewCountFlips = view.findViewById(R.id.view_count_flips);
        mViewCountHeads = view.findViewById(R.id.view_count_heads);
        mViewCountTails = view.findViewById(R.id.view_count_tails);
        mViewCountEdges = view.findViewById(R.id.view_count_edges);
        mViewLastResult = view.findViewById(R.id.view_count_last);
        mViewLevel = view.findViewById(R.id.view_count_level);
        mViewExperience = view.findViewById(R.id.view_count_experience);

        ((FloatingActionButton) mActivity.findViewById(R.id.button_share)).hide();
        ((ExtendedFloatingActionButton) mActivity.findViewById(R.id.button_auto)).hide();
        mActionButton = mActivity.findViewById(R.id.button_action);
        mActionButton.setImageResource(R.drawable.ic_baseline_share_24);
        setActionButton();
        mActionButton.setOnClickListener(view12 -> {
            Intent intentShare = ShareCompat.IntentBuilder.from(mActivity)
                    .setType(Constants.INTENT_TYPE_TEXT)
                    .setText(getString(R.string.text_share_stats,
                            mRepo.getCountFlips(),
                            mRepo.getCountHeads(),
                            mRepo.getCountTails(),
                            mRepo.getCountEdges(),
                            mRepo.getLastFlipStats().toLowerCase(),
                            mRepo.getLevel(),
                            mRepo.getExperience(),
                            mRepo.getExpRequired(),
                            getString(R.string.text_share_ad,
                                    getString(R.string.link_store))))
                    .getIntent();
            startActivity(intentShare);
        });

        Button reset = view.findViewById(R.id.button_reset);
        reset.setOnClickListener(view1 -> Snackbar.make(getView(),
                getString(R.string.snack_stats),
                Snackbar.LENGTH_LONG)
                .setAnchorView(mActionButton)
                .setAction(getString(R.string.snack_yes), view11 -> resetStats())
                .show()
        );

        mRadioGroupSkins = view.findViewById(R.id.radio_skins);
        mRadioGroupSkins.setOnCheckedChangeListener((radioGroup1, i) -> {
            int select = radioGroup1.indexOfChild(view.findViewById(radioGroup1.getCheckedRadioButtonId()));
            mRepo.setSkin(select);
        });

        mActivity.removeBadges(R.id.nav_board); // TODO: If badges exist.
        setTextViews();
        setSkins();
    }

    private void setActionButton() {
        if (mRepo.getCountFlips() == 0) {
            mActionButton.hide();
        } else {
            mActionButton.show();
        }
    }

    private void setTextViews() {
        mViewCountFlips.setText(String.valueOf(mRepo.getCountFlips()));
        mViewCountHeads.setText(String.valueOf(mRepo.getCountHeads()));
        mViewCountTails.setText(String.valueOf(mRepo.getCountTails()));
        mViewCountEdges.setText(String.valueOf(mRepo.getCountEdges()));
        mViewLastResult.setText(mRepo.getLastFlipStats());
        mViewLevel.setText(String.valueOf(mRepo.getLevel()));
        mViewExperience.setText(getString(R.string.count_experience, mRepo.getExperience(), mRepo.getExpRequired()));
    }

    private void setSkins() {
        int level = mRepo.getLevel();
        int skin = mRepo.getSkin();
        int skins = mRadioGroupSkins.getChildCount();

        for (int i = 1; i < skins; i++) {
            mRadioGroupSkins.getChildAt(i).setEnabled(level > i - 1);
        }

        for (int i = 0; i < skins; i++) {
            ((RadioButton) mRadioGroupSkins.getChildAt(i)).setChecked(skin == i);
        }
    }

    private void resetStats() {
        mRepo.resetStats();
        setActionButton();
        setTextViews();
        setSkins();
        if (mRepo.getTips())
            mActivity.showToast(getString(R.string.toast_reset));
    }
}