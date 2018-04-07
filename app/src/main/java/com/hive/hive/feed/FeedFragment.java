package com.hive.hive.feed;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hive.hive.R;
import com.hive.hive.model.association.Request;
import com.hive.hive.model.forum.ForumPost;

import java.util.ArrayList;

public class FeedFragment extends Fragment {

    private RecyclerView mRecyclerViewHome;
    private RecyclerViewFeedAdapter mRecyclerViewHomeAdapter;
    ArrayList<Object> DUMMYARRAY;


    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feed, container, false);
        DUMMYARRAY = new ArrayList<>();
        DUMMYARRAY.add(new ForumPost());
        DUMMYARRAY.add(new Request());
        DUMMYARRAY.add(new ForumPost());
        DUMMYARRAY.add(new Request());
        DUMMYARRAY.add(new ForumPost());
        DUMMYARRAY.add(new Request());
        DUMMYARRAY.add(new ForumPost());
        DUMMYARRAY.add(new Request());

        mRecyclerViewHome = v.findViewById(R.id.recyclerViewFeed);
        mRecyclerViewHomeAdapter = new RecyclerViewFeedAdapter(DUMMYARRAY);
        mRecyclerViewHome.setAdapter(mRecyclerViewHomeAdapter);
        mRecyclerViewHome.setLayoutManager(new LinearLayoutManager(v.getContext()));
        return v;
    }



}
