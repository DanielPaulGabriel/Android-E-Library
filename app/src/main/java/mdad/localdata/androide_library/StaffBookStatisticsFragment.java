package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StaffBookStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StaffBookStatisticsFragment extends Fragment {


    private static final String ARG_BOOK_ID = "bookId";
    private int bookId;

    public StaffBookStatisticsFragment() {
        // Required empty public constructor
    }

    public static StaffBookStatisticsFragment newInstance(int bookId) {
        StaffBookStatisticsFragment fragment = new StaffBookStatisticsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_book_statistics, container, false);

        return rootView;
    }
}