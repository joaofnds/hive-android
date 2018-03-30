package com.hive.hive.association.votes.tabs.current;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.foldablelayout.UnfoldableView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.hive.hive.R;
import com.hive.hive.association.votes.VotesHelper;
import com.hive.hive.model.association.Agenda;
import com.hive.hive.model.association.Question;
import com.hive.hive.model.user.User;
import com.hive.hive.utils.ProfilePhotoHelper;

import java.util.ArrayList;
import java.util.HashMap;


public class CurrentAdapter extends RecyclerView.Adapter<CurrentAdapter.RequestViewHolder> {
    private String TAG = CurrentAdapter.class.getSimpleName();
    //-- Data
    private HashMap<String, Agenda> mAgendas;
    private ArrayList<String> mAgendaIds;
    //-- Views
    private  UnfoldableView mUnfoldableView;
    private  FrameLayout mDetailsLayout;
    private View mView;
    //-- Current Agenda Questions
    private com.google.firebase.firestore.EventListener<QuerySnapshot> mQuestionsEL;
    private ListenerRegistration mQuestionsLR;
    private HashMap<String, Question> mQuestions; //FROM CURRENT AGENDA
    private ArrayList<String> mQuestionsIds; // FROM CURRENT AGENDA
    private Context mContext;

    public CurrentAdapter(Context context, HashMap<String, Agenda> agendas, ArrayList<String> agendasIds,
                          UnfoldableView unfoldableView, FrameLayout detailsLayout, View view){
        this.mContext = context;
        this.mAgendas = agendas;
        this.mAgendaIds = agendasIds;
        this.mUnfoldableView = unfoldableView;
        this.mDetailsLayout = detailsLayout;
        this.mView = view;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vote_cell, parent, false);

        mQuestionsIds = new ArrayList<>();

        mQuestions = new HashMap<>();

        mQuestionsEL = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    Log.e(TAG, e.getMessage());
                    return;
                }
                for(DocumentChange dc : documentSnapshots.getDocumentChanges()){
                    switch (dc.getType()){
                        case ADDED:
                            String questionId = dc.getDocument().getId();
                            Question question = dc.getDocument().toObject(Question.class);
                            mQuestions.put(questionId, question);
                            mQuestionsIds.add(questionId);
                            CurrentFragment.setItems(mContext, mQuestions, mQuestionsIds);
                            break;
                        case MODIFIED:
                            String modifiedId = dc.getDocument().getId();
                            mQuestions.remove(modifiedId);
                            mQuestions.put(modifiedId, dc.getDocument().toObject(Question.class));
                            CurrentFragment.setItems(mContext, mQuestions, mQuestionsIds);
                            break;
                        case REMOVED:
                            String removedId = dc.getDocument().getId();
                            mQuestions.remove(removedId);
                            mQuestionsIds.remove(removedId);
                            CurrentFragment.setItems(mContext, mQuestions, mQuestionsIds);
                            break;
                    }
                }
            }
        };
        return new RequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RequestViewHolder holder, int position) {
        final String agendaId = mAgendaIds.get(position);
        final Agenda agenda = mAgendas.get(agendaId);
        holder.mTitle.setText(agenda.getTitle());
        holder.mVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUnfoldableContent(agenda, agendaId);
                mUnfoldableView.unfold(view, mDetailsLayout);

            }
        });


    }
    @Override
    public int getItemCount() {
        return mAgendaIds.size();
    }

    private void changeUnfoldableContent(Agenda agenda, String agendaId){
        TextView titleTV = mView.findViewById(R.id.titleTV);
        TextView descriptionTV = mView.findViewById(R.id.contentTV);
        Log.d(TAG, "title "+agenda.getTitle());
        titleTV.setText(agenda.getTitle());
        descriptionTV.setText(agenda.getContent());
        fillUser(agenda.getSuggestedByRef());
        //TODO CHECK LAST ITEM CLICKED BEFORE RELOADING DATA
        //IF CLICK IS DIFF
        if(mQuestionsLR != null) //catches the first run
            mQuestionsLR.remove();
        //TODO REMOVE STATIC ASSOCIATION REFERENCE
        if(CurrentFragment.mCurrentSessionId != null)// should'nt happen, but just to be sure
            mQuestionsLR = VotesHelper.getQuestions(FirebaseFirestore.getInstance(),"gVw7dUkuw3SSZSYRXe8s",
                    CurrentFragment.mCurrentSessionId, agendaId).addSnapshotListener(mQuestionsEL);
    }
    private void fillUser(DocumentReference userRef){
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    TextView suggestedByTV = mView.findViewById(R.id.suggestedByTV);
                    ImageView suggestedByIV = mView.findViewById(R.id.suggestedByIV);
                    User user = documentSnapshot.toObject(User.class);
                    suggestedByTV.setText(user.getName());
                    ProfilePhotoHelper.loadImage(mView.getContext().getApplicationContext(), suggestedByIV, user.getPhotoUrl());
                    //Log.d(RequestAdapter.class.getSimpleName(), user.getPhotoUrl());
                }
            }
        });
    }
    /**
     * Class to serve as ViewHolder for a Request model in this adapter
     */
    class RequestViewHolder extends RecyclerView.ViewHolder{
        final CardView mVote;
        final TextView mTitle;


        RequestViewHolder(View view){
            super(view);
            mTitle = view.findViewById(R.id.budgetNameTV);
            mVote =  view.findViewById(R.id.cardVote);
        }

        @Override
        public String toString(){
            return "";
        }
    }

}
