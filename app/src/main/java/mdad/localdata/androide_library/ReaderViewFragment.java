package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ReaderViewFragment extends Fragment {
    private View rootView;
    private static final String ARG_TITLE = "bookTitle";
    private static final String ARG_CONTENT = "content_path";

    private String bookTitle;
    private String bookContent;

    public ReaderViewFragment() {
        // Required empty public constructor
    }

    public static ReaderViewFragment newInstance(String bookTitle, String bookContent) {
        ReaderViewFragment fragment = new ReaderViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, bookTitle);
        args.putString(ARG_CONTENT, bookContent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookTitle = getArguments().getString(ARG_TITLE);
            bookContent = getArguments().getString(ARG_CONTENT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_books, container, false);
        return rootView;
    }
}