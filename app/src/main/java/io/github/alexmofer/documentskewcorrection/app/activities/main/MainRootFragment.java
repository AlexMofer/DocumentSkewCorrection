package io.github.alexmofer.documentskewcorrection.app.activities.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import io.github.alexmofer.android.support.window.AvoidAreaCalculator;
import io.github.alexmofer.android.support.window.AvoidAreaCalculatorViewModel;
import io.github.alexmofer.documentskewcorrection.app.activities.main.hms.MainHMSFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.opencv.MainOpenCVFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.tensorflow.MainTensorflowFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.ui.MainUIFragment;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainRootBinding;

/**
 * 根页面
 * Created by Alex on 2025/5/26.
 */
public class MainRootFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainRootBinding binding =
                FragmentMainRootBinding.inflate(getLayoutInflater(), container, false);
        final AvoidAreaCalculator calculator =
                AvoidAreaCalculatorViewModel.getInstance(requireActivity()).getCalculator();
        calculator.calculate(getViewLifecycleOwner(), WindowInsetsCompat.Type.systemBars(),
                AvoidAreaCalculator.ALL, binding.fmrVContent::setPadding);
        binding.fmrVOpencv.setOnClickListener(v -> MainOpenCVFragment.navigate(this));
        binding.fmrVTensorflow.setOnClickListener(v -> MainTensorflowFragment.navigate(this));
        binding.fmrVHms.setOnClickListener(v -> MainHMSFragment.navigate(this));
        binding.fmrVUi.setOnClickListener(v -> MainUIFragment.navigate(this));
        return binding.getRoot();
    }
}
