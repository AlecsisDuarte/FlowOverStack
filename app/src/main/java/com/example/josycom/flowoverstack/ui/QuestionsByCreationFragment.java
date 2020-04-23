package com.example.josycom.flowoverstack.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.josycom.flowoverstack.R;
import com.example.josycom.flowoverstack.adapters.QuestionAdapter;
import com.example.josycom.flowoverstack.model.Owner;
import com.example.josycom.flowoverstack.model.Question;
import com.example.josycom.flowoverstack.util.DateUtil;
import com.example.josycom.flowoverstack.util.StringConstants;
import com.example.josycom.flowoverstack.viewmodel.CustomQuestionViewModelFactory;
import com.example.josycom.flowoverstack.viewmodel.QuestionViewModel;

import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_AVATAR_ADDRESS;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_ANSWERS_COUNT;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_DATE;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_FULL_TEXT;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_ID;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_NAME;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_OWNER_LINK;
import static com.example.josycom.flowoverstack.util.StringConstants.EXTRA_QUESTION_TITLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionsByCreationFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private PagedList<Question> mQuestions;
    private View.OnClickListener mOnClickListener;
    private SwipeRefreshLayout mSwipeContainer;

    public QuestionsByCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions_by_creation, container, false);
        mRecyclerView = view.findViewById(R.id.creation_recycler_view);
        mSwipeContainer = view.findViewById(R.id.creationSwipeContainer);
        mSwipeContainer.setColorSchemeResources(R.color.colorPrimaryLight);

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
                int position = viewHolder.getAdapterPosition();
                Intent answerActivityIntent = new Intent(getContext(), AnswerActivity.class);
                Question currentQuestion = mQuestions.get(position);
                Owner questionOwner = currentQuestion.getOwner();

                answerActivityIntent.putExtra(EXTRA_QUESTION_TITLE, currentQuestion.getTitle());
                answerActivityIntent.putExtra(EXTRA_QUESTION_NAME, questionOwner.getDisplayName());
                answerActivityIntent.putExtra(EXTRA_QUESTION_DATE,
                        DateUtil.toNormalDate(currentQuestion.getCreationDate()));
                answerActivityIntent.putExtra(EXTRA_QUESTION_FULL_TEXT, currentQuestion.getBody());
                answerActivityIntent.putExtra(EXTRA_AVATAR_ADDRESS, questionOwner.getProfileImage());
                answerActivityIntent.putExtra(EXTRA_QUESTION_ANSWERS_COUNT, currentQuestion.getAnswerCount());
                answerActivityIntent.putExtra(EXTRA_QUESTION_ID, currentQuestion.getQuestionId());
                answerActivityIntent.putExtra(EXTRA_QUESTION_OWNER_LINK, questionOwner.getLink());

                startActivity(answerActivityIntent);
            }
        };
        handleRecyclerView();
        return view;
    }

    private void handleRecyclerView(){
        final QuestionAdapter questionAdapter = new QuestionAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        QuestionViewModel questionViewModel = new ViewModelProvider(this, new CustomQuestionViewModelFactory(StringConstants.FIRST_PAGE,
                StringConstants.PAGE_SIZE,
                StringConstants.ORDER_DESCENDING,
                StringConstants.SORT_BY_CREATION,
                StringConstants.SITE,
                StringConstants.QUESTION_FILTER)).get(QuestionViewModel.class);
        questionViewModel.getQuestionPagedList().observe(getViewLifecycleOwner(), new Observer<PagedList<Question>>() {
            @Override
            public void onChanged(PagedList<Question> questions) {
                mQuestions = questions;
                questionAdapter.submitList(questions);
            }
        });
        mRecyclerView.setAdapter(questionAdapter);
        questionAdapter.setOnClickListener(mOnClickListener);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                questionViewModel.refresh();
                mSwipeContainer.setRefreshing(false);
            }
        });
    }
}
