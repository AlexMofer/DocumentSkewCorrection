package io.github.alexmofer.documentskewcorrection.app.activities.main.opencv;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.activities.main.dc.MainDCFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.dc.MainDCViewModel;

/**
 * Core 示范
 * Created by Alex on 2025/5/26.
 */
public class MainOpenCVFragment extends MainDCFragment {

    public static void navigate(Fragment fragment) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        controller.navigate(R.id.main_action_root_to_core);
    }

    @NonNull
    @Override
    protected MainDCViewModel onCreateViewModel() {
        return new ViewModelProvider(this).get(MainOpenCVViewModel.class);
    }

    @Override
    protected CharSequence getTitle() {
        return "OpenCV 示范";
    }
}