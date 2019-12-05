package com.niklasm.iliasbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private int[] layouts;
    private Button btnSkip, btnNext;
    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.welcome_activity_button_right_got_it));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.welcome_activity_button_right_next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(final int arg0) {
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        if (!IliasBuddyPreferenceHandler.getFirstTimeLaunch(this, true)) {
            launchHomeScreen();
            finish();
        }

        // Making notification bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.slide_screen1,
                R.layout.slide_screen2,
                R.layout.slide_screen3};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        final MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(v -> launchHomeScreen());

        btnNext.setOnClickListener(v -> {
            // checking for last page
            // if last page home screen will be launched
            final int current = getItem(+1);
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
            } else {
                launchHomeScreen();
            }
        });
    }

    private void addBottomDots(final int currentPage) {
        final TextView[] dots = new TextView[layouts.length];

        final int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        final int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("•");
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }

    private int getItem(final int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        IliasBuddyPreferenceHandler.setFirstTimeLaunch(this, false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View view = Objects.requireNonNull(layoutInflater).inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull final View view, @NonNull final Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(@NonNull final ViewGroup container, final int position, @NonNull final Object object) {
            final View view = (View) object;
            container.removeView(view);
        }
    }

}
