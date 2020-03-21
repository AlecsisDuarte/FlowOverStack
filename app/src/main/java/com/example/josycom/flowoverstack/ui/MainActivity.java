package com.example.josycom.flowoverstack.ui;

import android.os.Bundle;

import com.example.josycom.flowoverstack.R;
import com.example.josycom.flowoverstack.adapters.QuestionAdapter;
import com.example.josycom.flowoverstack.model.Question;
import com.example.josycom.flowoverstack.viewmodel.QuestionViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_questions);
        final QuestionAdapter questionAdapter = new QuestionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        QuestionViewModel questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        questionViewModel.getQuestionPagedList().observe(this, new Observer<PagedList<Question>>() {
            @Override
            public void onChanged(PagedList<Question> questions) {
                questionAdapter.submitList(questions);
            }
        });
        recyclerView.setAdapter(questionAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_filter){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
