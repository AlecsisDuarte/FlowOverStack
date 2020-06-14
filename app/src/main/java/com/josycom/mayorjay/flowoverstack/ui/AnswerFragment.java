package com.josycom.mayorjay.flowoverstack.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.josycom.mayorjay.flowoverstack.R;
import com.josycom.mayorjay.flowoverstack.adapters.AnswerAdapter;
import com.josycom.mayorjay.flowoverstack.util.AppUtils;
import com.josycom.mayorjay.flowoverstack.util.StringConstants;
import com.josycom.mayorjay.flowoverstack.viewmodel.AnswerViewModel;
import com.josycom.mayorjay.flowoverstack.viewmodel.CustomAnswerViewModelFactory;
import com.mukesh.MarkdownView;

import org.jsoup.Jsoup;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnswerFragment extends Fragment {

    private AnswerAdapter mAnswerAdapter;
    private String mOwnerQuestionLink;
    private TextView mTvNoAnswerQuestionDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_answer, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_answers);
        TextView tvQuestionDetail = view.findViewById(R.id.tv_question_detail);
        TextView tvDateQuestionDetail = view.findViewById(R.id.tv_date_question_detail);
        TextView tvNameQuestionDetail = view.findViewById(R.id.tv_name_question_detail);
        TextView tvVotesCountItem = view.findViewById(R.id.tv_votes_count_item);
        mTvNoAnswerQuestionDetail = view.findViewById(R.id.tv_no_answer_question_detail);
        MarkdownView markdownView = view.findViewById(R.id.mark_down_view);
        ImageView ivAvatarQuestionDetail = view.findViewById(R.id.iv_avatar_question_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAnswerAdapter = new AnswerAdapter();

        int questionId = 0;
        if (getArguments() != null){
            tvQuestionDetail.setText(Jsoup.parse(Objects.requireNonNull(requireArguments().getString(StringConstants.EXTRA_QUESTION_TITLE))).text());
            markdownView.setMarkDownText(getArguments().getString(StringConstants.EXTRA_QUESTION_FULL_TEXT));
            tvDateQuestionDetail.setText(getArguments().getString(StringConstants.EXTRA_QUESTION_DATE));
            tvNameQuestionDetail.setText(getArguments().getString(StringConstants.EXTRA_QUESTION_NAME));
            int voteCount = getArguments().getInt(StringConstants.EXTRA_QUESTION_VOTES_COUNT, 0);
            if (voteCount <= 0){
                tvVotesCountItem.setText(String.valueOf(voteCount));
            } else {
                tvVotesCountItem.setText(getString(R.string.plus_score).concat(String.valueOf(voteCount)));
            }
            questionId = getArguments().getInt(StringConstants.EXTRA_QUESTION_ID, 0);
            String avatarAddress = getArguments().getString(StringConstants.EXTRA_AVATAR_ADDRESS);
            mOwnerQuestionLink = getArguments().getString(StringConstants.EXTRA_QUESTION_OWNER_LINK);
            Glide.with(requireActivity())
                    .load(avatarAddress)
                    .placeholder(R.drawable.loading)
                    .into(ivAvatarQuestionDetail);
        }
        AnswerViewModel answerViewModel = new ViewModelProvider(requireActivity(),
                new CustomAnswerViewModelFactory(questionId,
                        StringConstants.ORDER_DESCENDING,
                        StringConstants.SORT_BY_ACTIVITY,
                        StringConstants.SITE,
                        StringConstants.ANSWER_FILTER,
                        StringConstants.API_KEY)).get(AnswerViewModel.class);
        answerViewModel.getAnswersLiveData().observe(getViewLifecycleOwner(), answers -> {
            if (answers.size() == 0){
                mTvNoAnswerQuestionDetail.setVisibility(View.VISIBLE);
            } else {
                mAnswerAdapter.setAnswers(answers);
            }
        });
        recyclerView.setAdapter(mAnswerAdapter);

        ivAvatarQuestionDetail.setOnClickListener(v -> openProfileOnWeb());

        tvNameQuestionDetail.setOnClickListener(v -> openProfileOnWeb());

        return view;
    }

    private void openProfileOnWeb() {
        AppUtils.directLinkToBrowser(getActivity(), mOwnerQuestionLink);
    }
}
