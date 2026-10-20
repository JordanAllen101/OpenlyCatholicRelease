package com.openlycatholic.app;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.openlycatholic.app.examination.ExaminationFragment;
import com.openlycatholic.app.home.HomeFragment;
import com.openlycatholic.app.missal.MissalFragment;
import com.openlycatholic.app.rosary.RosaryFragment;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_TAB = 0;

    private final int[] tabContainers = {
            R.id.navHome,
            R.id.navRosary,
            R.id.navMissal,
            R.id.navExamination
    };
    private final int[] tabIconBackgrounds = {
            R.id.navHomeIconBackground,
            R.id.navRosaryIconBackground,
            R.id.navMissalIconBackground,
            R.id.navExaminationIconBackground
    };
    private final int[] tabIcons = {
            R.id.navHomeIcon,
            R.id.navRosaryIcon,
            R.id.navMissalIcon,
            R.id.navExaminationIcon
    };
    private final int[] tabLabels = {
            R.id.navHomeLabel,
            R.id.navRosaryLabel,
            R.id.navMissalLabel,
            R.id.navExaminationLabel
    };
    private int selectedTab = HOME_TAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.navHome).setOnClickListener(v ->
                selectTab(HOME_TAB, new HomeFragment()));
        findViewById(R.id.navRosary).setOnClickListener(v ->
                selectTab(1, new RosaryFragment()));
        findViewById(R.id.navMissal).setOnClickListener(v ->
                selectTab(2, new MissalFragment()));
        findViewById(R.id.navExamination).setOnClickListener(v ->
                selectTab(3, new ExaminationFragment()));

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt("selectedTab", HOME_TAB);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContainer, new HomeFragment())
                    .commit();
        }
        updateSelectedTab(selectedTab);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selectedTab", selectedTab);
        super.onSaveInstanceState(outState);
    }

    private void selectTab(int tab, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
        fragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit();
        selectedTab = tab;
        updateSelectedTab(tab);
    }

    private void updateSelectedTab(int selected) {
        int selectedColor = ContextCompat.getColor(this, R.color.primary);
        int unselectedColor = ContextCompat.getColor(this, R.color.text_secondary);

        for (int index = 0; index < tabContainers.length; index++) {
            boolean isSelected = index == selected;
            View iconBackground = findViewById(tabIconBackgrounds[index]);
            ImageView icon = findViewById(tabIcons[index]);
            TextView label = findViewById(tabLabels[index]);

            iconBackground.setSelected(isSelected);
            icon.setImageTintList(ColorStateList.valueOf(
                    isSelected ? selectedColor : unselectedColor
            ));
            label.setTextColor(isSelected ? selectedColor : unselectedColor);
            findViewById(tabContainers[index]).setSelected(isSelected);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
            return;
        }

        Fragment currentFragment = fragmentManager.findFragmentById(R.id.mainContainer);
        if (!(currentFragment instanceof HomeFragment)) {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainContainer, new HomeFragment())
                    .commit();
            selectedTab = HOME_TAB;
            updateSelectedTab(selectedTab);
            return;
        }

        super.onBackPressed();
    }
}
