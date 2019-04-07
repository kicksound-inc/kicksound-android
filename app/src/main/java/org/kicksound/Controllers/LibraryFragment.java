package org.kicksound.Controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kicksound.R;

import androidx.fragment.app.Fragment;

public class LibraryFragment extends Fragment {
    public LibraryFragment() {
        // Required empty public constructor
    }

    public static LibraryFragment newInstance() {
        LibraryFragment fragment = new LibraryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_fragment, container, false);
    }
}