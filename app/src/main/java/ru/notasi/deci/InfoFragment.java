package ru.notasi.deci;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        ((FloatingActionButton) activity.findViewById(R.id.button_share)).hide();
        ((ExtendedFloatingActionButton) activity.findViewById(R.id.button_auto)).hide();
        ((FloatingActionButton) activity.findViewById(R.id.button_action)).hide();
//        activity.removeBadges(R.id.nav_info); TODO: If badges exist.

        WebView webView = view.findViewById(R.id.info_web);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setBackgroundColor(activity.getColor(R.color.white));
        } else {
            webView.setBackgroundColor(getResources().getColor(R.color.white));
        }
        webView.setInitialScale(1);
        webView.loadUrl(getString(R.string.link_app));
        webView.clearCache(true);
        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener((view1, scrollX, scrollY, scrollOldX, scrollOldY) -> {
                if (scrollY <= scrollOldY) {
                    activity.slideNavUp();
                } else {
                    activity.slideNavDown();
                }
            });
        } else {
            webView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                // TODO: Scroll for API 16.
                // activity.showToast("Scrolling");
            });
        }
    }
}