package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StaffAddBookFragment extends Fragment {

    public StaffAddBookFragment() {
        // Required empty public constructor
    }

    public static StaffAddBookFragment newInstance() {

        return new StaffAddBookFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_add_book, container, false);
    }
}