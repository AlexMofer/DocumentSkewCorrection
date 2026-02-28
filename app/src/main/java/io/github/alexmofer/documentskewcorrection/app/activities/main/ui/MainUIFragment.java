package io.github.alexmofer.documentskewcorrection.app.activities.main.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.window.AvoidAreaCalculator;
import io.github.alexmofer.android.support.window.AvoidAreaCalculatorViewModel;
import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.activities.main.common.MainCommonFragment;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainUiBinding;

/**
 * UI 示范
 * Created by Alex on 2025/5/28.
 */
public class MainUIFragment extends MainCommonFragment<MainUIViewModel> {

    public static void navigate(Fragment fragment) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        controller.navigate(R.id.main_action_root_to_ui);
    }

    @NonNull
    @Override
    protected MainUIViewModel onCreateViewModel() {
        return new ViewModelProvider(this).get(MainUIViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainUiBinding binding =
                FragmentMainUiBinding.inflate(getLayoutInflater(), container, false);
        final AvoidAreaCalculator calculator =
                AvoidAreaCalculatorViewModel.getInstance(requireActivity()).getCalculator();
        calculator.calculate(getViewLifecycleOwner(), WindowInsetsCompat.Type.systemBars(),
                AvoidAreaCalculator.ALL, (start, top, end, bottom) -> {
                    binding.fmuVToolbar.setPadding(start, top, end, 0);
                    binding.fmuVContent.setPadding(start, 0, end, bottom);
                });
        binding.fmuVToolbar.setNavigationOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.fmuVToolbar.setOnMenuItemClickListener(newOnMenuItemClickListener());

        final LifecycleOwner owner = getViewLifecycleOwner();
        final RequestManager manager = Glide.with(this);
        mViewModel.getOriginal().observe(owner,
                value -> manager.load(value).into(binding.fmuVCorrection));
        mViewModel.getPoints().observe(owner, binding.fmuVCorrection::setPoints);
        mViewModel.getCorrected().observe(owner,
                value -> {
                    if (value != null) {
                        MainCorrectedFragment.navigate(this, value);
                        mViewModel.clearCorrected();
                    }
                });
        mViewModel.getFailure().observe(owner, value -> {
            if (value != null) {
                StringResource.showToast(requireContext(), value);
                mViewModel.clearFailure();
            }
        });
        binding.fmuVApply.setOnClickListener(
                v -> mViewModel.correct(requireContext(), binding.fmuVCorrection.getPoints()));
        return binding.getRoot();
    }
}