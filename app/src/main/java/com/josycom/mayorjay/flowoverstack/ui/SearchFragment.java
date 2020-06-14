package com.josycom.mayorjay.flowoverstack.ui;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.josycom.mayorjay.flowoverstack.R;
import com.josycom.mayorjay.flowoverstack.adapters.SearchAdapter;
import com.josycom.mayorjay.flowoverstack.model.Owner;
import com.josycom.mayorjay.flowoverstack.model.Question;
import com.josycom.mayorjay.flowoverstack.util.DateUtil;
import com.josycom.mayorjay.flowoverstack.util.StringConstants;
import com.josycom.mayorjay.flowoverstack.viewmodel.SearchViewModel;

import java.util.List;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_AVATAR_ADDRESS;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_DATE;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_FULL_TEXT;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_ID;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_NAME;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_OWNER_LINK;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_TITLE;
import static com.josycom.mayorjay.flowoverstack.util.StringConstants.EXTRA_QUESTION_VOTES_COUNT;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private String mSearchInput;
    private List<Question> mQuestions;
    private SearchViewModel mSearchViewModel;
    private RecyclerView mRvSearchResults;
    private FloatingActionButton mSearchScrollUpFab;
    private NestedScrollView mSearchNestedScrollview;
    private TextInputEditText mSearchTextInputEditText;
    private ProgressBar mSearchPbFetchData;
    private TextView mSearchTvError;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRvSearchResults = view.findViewById(R.id.rv_search_results);
        mSearchScrollUpFab = view.findViewById(R.id.search_scroll_up_fab);
        mSearchNestedScrollview = view.findViewById(R.id.search_nested_scrollview);
        MaterialButton searchButton = view.findViewById(R.id.search_button);
        mSearchTextInputEditText = view.findViewById(R.id.search_text_input_editText);
        mSearchPbFetchData = view.findViewById(R.id.search_pb_fetch_data);
        mSearchTvError = view.findViewById(R.id.search_tv_error);

        mRvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvSearchResults.setHasFixedSize(true);
        mRvSearchResults.setItemAnimator(new DefaultItemAnimator());

        mSearchScrollUpFab.setVisibility(View.INVISIBLE);
        mSearchNestedScrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 0) {
                mSearchScrollUpFab.setVisibility(View.VISIBLE);
            } else {
                mSearchScrollUpFab.setVisibility(View.INVISIBLE);
            }
        });
        mSearchScrollUpFab.setOnClickListener(v -> mSearchNestedScrollview.scrollTo(0, 0));

        View.OnClickListener onClickListener = v -> {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
            int position = viewHolder.getAdapterPosition();
            Question currentQuestion = mQuestions.get(position);
            assert currentQuestion != null;
            Owner questionOwner = currentQuestion.getOwner();

            Bundle arg = new Bundle();
            arg.putString(EXTRA_QUESTION_TITLE, currentQuestion.getTitle());
            arg.putString(EXTRA_QUESTION_NAME, questionOwner.getDisplayName());
            arg.putString(EXTRA_QUESTION_DATE,
                    DateUtil.toNormalDate(currentQuestion.getCreationDate()));
            arg.putString(EXTRA_QUESTION_FULL_TEXT, currentQuestion.getBody());
            arg.putString(EXTRA_AVATAR_ADDRESS, questionOwner.getProfileImage());
            arg.putInt(EXTRA_QUESTION_ID, currentQuestion.getQuestionId());
            arg.putInt(EXTRA_QUESTION_VOTES_COUNT, currentQuestion.getScore());
            arg.putString(EXTRA_QUESTION_OWNER_LINK, questionOwner.getLink());
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.answer_dest, arg);
            requireActivity().overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
        };

        searchButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(mSearchTextInputEditText.getText()).toString())){
                mSearchTextInputEditText.setError(getString(R.string.type_a_search_query));
            } else {
                mSearchInput = mSearchTextInputEditText.getText().toString();
                InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus()).getWindowToken(), 0);
                }
                makeSearch();
            }
        });
        final SearchAdapter searchAdapter = new SearchAdapter();
        mSearchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        mSearchViewModel.getResponseLiveData().observe(getViewLifecycleOwner(), searchResponse -> {
            switch (searchResponse.networkState) {
                case StringConstants.LOADING:
                    onLoading();
                    break;
                case StringConstants.LOADED:
                    onLoaded();
                    mQuestions = searchResponse.questions;
                    searchAdapter.setQuestions(searchResponse.questions);
                    break;
                case StringConstants.NO_MATCHING_RESULT:
                    onNoMatchingResult();
                    break;
                case StringConstants.FAILED:
                    onError();
                    break;
            }
        });
        mRvSearchResults.setAdapter(searchAdapter);
        searchAdapter.setOnClickListener(onClickListener);
        return view;
    }

    private void makeSearch() {
        mSearchViewModel.setQuery(mSearchInput);
    }

    private void onLoading() {
        mSearchPbFetchData.setVisibility(View.VISIBLE);
        mRvSearchResults.setVisibility(View.INVISIBLE);
        mSearchTvError.setVisibility(View.INVISIBLE);
    }

    private void onLoaded() {
        mSearchPbFetchData.setVisibility(View.INVISIBLE);
        mRvSearchResults.setVisibility(View.VISIBLE);
        mSearchTvError.setVisibility(View.INVISIBLE);
    }

    private void onNoMatchingResult() {
        mSearchPbFetchData.setVisibility(View.INVISIBLE);
        mRvSearchResults.setVisibility(View.INVISIBLE);
        mSearchTvError.setVisibility(View.VISIBLE);
        mSearchTvError.setText(R.string.no_matching_result);
    }

    private void onError() {
        mSearchPbFetchData.setVisibility(View.INVISIBLE);
        mRvSearchResults.setVisibility(View.INVISIBLE);
        mSearchTvError.setVisibility(View.VISIBLE);
        mSearchTvError.setText(R.string.search_error_message);
    }
}
