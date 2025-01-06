package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
public class StaffUserDetailsFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_CREATED_AT = "createdAt";

    private int userId;
    private String username;
    private String createdAt;

    public static StaffUserDetailsFragment newInstance(int userId, String username, String createdAt) {
        StaffUserDetailsFragment fragment = new StaffUserDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_CREATED_AT, createdAt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID);
            username = getArguments().getString(ARG_USERNAME);
            createdAt = getArguments().getString(ARG_CREATED_AT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_user_details, container, false);

        // Implement borrowing activity, reviews, and graph logic here

        return rootView;
    }
}
