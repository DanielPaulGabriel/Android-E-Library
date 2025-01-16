package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StaffBooksCataloguePopulateFragment extends Fragment {

    public StaffBooksCataloguePopulateFragment() {
        // Required empty public constructor
    }

    public static StaffBooksCataloguePopulateFragment newInstance() {

        return new StaffBooksCataloguePopulateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_books_catalogue_populate, container, false);

        return rootView;
    }


}