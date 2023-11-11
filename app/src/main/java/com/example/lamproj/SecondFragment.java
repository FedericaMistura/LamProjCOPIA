package com.example.lamproj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lamproj.databinding.FragmentSecondBinding;

/*
Quando l'utente clicca su "Get help" dal menu, si apre questo fragment
con le indicazioni su come funziona l'app
 */
public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private TextView tvSecond;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        //tvSecond = getActivity().findViewById(R.id.textview_second);
        //tvSecond.setText(txt);
        return binding.getRoot();

    }
    /*
    Visualizzazione della stringa presente nelle risorse
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String txt = getResources().getString(R.string.help);
        tvSecond = view.findViewById(R.id.textview_second);
        tvSecond.setText(txt);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}