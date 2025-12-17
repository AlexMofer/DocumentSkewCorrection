package io.github.alexmofer.documentskewcorrection.app.activities.main.tensorflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.activities.main.dc.MainDCFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.dc.MainDCViewModel;

/**
 * Tensorflow 代理位图处理 示范
 * Created by Alex on 2025/5/27.
 */
public class MainTensorflowFragment extends MainDCFragment {

    public static void navigate(Fragment fragment) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        controller.navigate(R.id.main_action_root_to_tensorflow);
    }


    @NonNull
    @Override
    protected MainDCViewModel onCreateViewModel() {
        return new ViewModelProvider(this).get(MainTensorflowViewModel.class);
    }

    @Override
    protected CharSequence getTitle() {
        return "Tensorflow 代理位图处理 示范";
    }
}