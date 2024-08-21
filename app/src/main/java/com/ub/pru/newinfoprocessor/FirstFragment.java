package com.ub.pru.newinfoprocessor;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pru.newinfoprocessor.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private BluetoothAdapter mBtAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        binding.buttonStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent enableBtIntentIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntentIntent);
            }
        });

        binding.buttonEndBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtAdapter.disable();
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}