package com.hive.hive.association.votes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.hive.hive.R;
import com.hive.hive.model.association.Question;
import com.hive.hive.model.association.QuestionOptions;
import com.hive.hive.model.association.Vote;

import java.util.ArrayList;

/**
 * Created by vplentz on 22/03/18.
 */

public class VotesHelper {
    private static String TAG = VotesHelper.class.getSimpleName();
    public static String ASSOCIATION_COLLECTION = "associations";

    public static String QUESTIONS_COLLECTION = "questions";

    public static String SESSIONS_COLLECTION = "sessions";

    public static String AGENDAS_COLLECTION = "agendas";

    public static String VOTES_COLLECTION = "votes";

    //--- Sessions

    //TODO ADD WHERE CLAUSE

    public static Query getCurrentSession(FirebaseFirestore db, String associationID) {
        return db
                .collection(ASSOCIATION_COLLECTION)
                .document(associationID)
                .collection(SESSIONS_COLLECTION)
                .whereEqualTo("status", "Current");
    }

    //--- Agendas
    public static CollectionReference getAgendas(FirebaseFirestore db, String associationID, String sessionID){
        return db
                .collection(ASSOCIATION_COLLECTION)
                .document(associationID)
                .collection(SESSIONS_COLLECTION)
                .document(sessionID)
                .collection(AGENDAS_COLLECTION);
    }

    //--- Questions
    public static CollectionReference getQuestions(FirebaseFirestore db, String associationID, String sessionID, String agendaID){
        return db
                .collection(ASSOCIATION_COLLECTION)
                .document(associationID)
                .collection(SESSIONS_COLLECTION)
                .document(sessionID)
                .collection(AGENDAS_COLLECTION)
                .document(agendaID)
                .collection((QUESTIONS_COLLECTION));
    }

    //--- Vote
    public static void setVote(FirebaseFirestore db, String associationID, String sessionID, String agendaID, ArrayList<String> questionsIDs,
                               final ArrayList<Integer> answersPositions, final ArrayList<Vote> votes, Context context){

        //reference to questionOption
        final ArrayList<DocumentReference> documentReferences = new ArrayList<>();
        for(String questionID :questionsIDs){
            documentReferences.add(
                    db
                            .collection(ASSOCIATION_COLLECTION)
                            .document(associationID)
                            .collection(SESSIONS_COLLECTION)
                            .document(sessionID)
                            .collection(AGENDAS_COLLECTION)
                            .document(agendaID)
                            .collection((QUESTIONS_COLLECTION))
                            .document(questionID)

            );
        }


        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                int questionToAnswer = 0;
                String userID = FirebaseAuth.getInstance().getUid();

                if(userID == null) return null; //SOME USER PROBLEM OCCURED

                ArrayList<Question> questions = new ArrayList<>();
                for(DocumentReference documentReference : documentReferences ){
                    Question question = transaction.get(documentReference).toObject(Question.class);
                    ArrayList<QuestionOptions> questionOptions =  question.getOptions();

                    //decrement last voting option score, if needed
                    if(transaction.get(documentReference.collection(VOTES_COLLECTION).document(userID)).exists()){
                        Vote lastVote = transaction.get(documentReference.collection(VOTES_COLLECTION).document(userID)).toObject(Vote.class);
                        if(lastVote.getVotingOption() != answersPositions.get(questionToAnswer)){
                            questionOptions.get(lastVote.getVotingOption())
                                    .setScore(questionOptions.get(lastVote.getVotingOption()).getScore() - 1);
                            questionOptions.get(answersPositions.get(questionToAnswer))
                                    .setScore(questionOptions.get(answersPositions.get(questionToAnswer)).getScore() + 1);

                        }
                    }else
                        questionOptions.get(answersPositions.get(questionToAnswer))
                                .setScore(questionOptions.get(answersPositions.get(questionToAnswer)).getScore() + 1);
                    questionToAnswer++;
                    questions.add(question);
                }
                for(int i = 0; i < documentReferences.size(); i++){
                    transaction.set(documentReferences.get(i), questions.get(i));
                    transaction.set(
                            documentReferences.get(i).collection(VOTES_COLLECTION).document(FirebaseAuth.getInstance().getUid())
                            , votes.get(i));
                }


                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, context.getString(R.string.vote_ok), Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, context.getString(R.string.vote_not_ok), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static CollectionReference getVoters(FirebaseFirestore db, String votersPath){
        return db.collection(votersPath);
    }
}
