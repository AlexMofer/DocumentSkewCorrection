package io.github.alexmofer.documentskewcorrection.app.activities.main.dc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.widget.AvoidArea;
import io.github.alexmofer.documentskewcorrection.app.activities.main.common.MainCommonFragment;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainDcBinding;

/**
 * 基础检测与识别
 * Created by Alex on 2025/5/26.
 */
public abstract class MainDCFragment extends MainCommonFragment<MainDCViewModel> {

    /**
     * 获取标题
     *
     * @return 标题
     */
    protected abstract CharSequence getTitle();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainDcBinding binding =
                FragmentMainDcBinding.inflate(getLayoutInflater(), container, false);
        AvoidArea.paddingIgnoreBottom(binding.fmaVToolbar);
        AvoidArea.paddingIgnoreTop(binding.fmaVContent);
        binding.fmaVToolbar.setTitle(getTitle());
        binding.fmaVToolbar.setNavigationOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.fmaVToolbar.setOnMenuItemClickListener(newOnMenuItemClickListener());

        final LifecycleOwner owner = getViewLifecycleOwner();
        final RequestManager manager = Glide.with(this);
        mViewModel.getInfo().observe(owner,
                value -> StringResource.setText(binding.fmaVInfo, value));
        mViewModel.getOriginal().observe(owner,
                value -> manager.load(value).into(binding.fmaVOriginal));
        mViewModel.getCorrected().observe(owner,
                value -> manager.load(value).into(binding.fmaVCorrected));
        mViewModel.getFailure().observe(owner, value -> {
            if (value != null) {
                StringResource.showToast(requireContext(), value);
                mViewModel.clearFailure();
            }
        });
        return binding.getRoot();
    }
}
