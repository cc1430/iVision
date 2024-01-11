package com.cc.ivision.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.cc.ivision.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;

    int[] ivTabs = {
            R.drawable.selector_home,
            R.drawable.selector_protect_eye,
            R.drawable.selector_mall,
            R.drawable.selector_mine
    };

    String[] tvTabs = {
            "首页", "护眼", "商城", "我的"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new ProtectEyeFragment());
        fragmentList.add(new MallFragment());
        fragmentList.add(new MineFragment());
        viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager(), fragmentList));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);


        for (int i = 0; i < fragmentList.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_home_bottom, null, false);
            ImageView ivTab = view.findViewById(R.id.ivTab);
            TextView tvTab = view.findViewById(R.id.tvTab);
            ivTab.setImageResource(ivTabs[i]);
            tvTab.setText(tvTabs[i]);
            if (i == 0) {
                view.setSelected(true);
            }
            tabLayout.getTabAt(i).setCustomView(view);
        }
    }
}