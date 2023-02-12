package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalUser;

public class SingUpActivity extends AppCompatActivity {

    EditText password_create;
    EditText username_create;
    EditText email_create;
    Button createBTN;

    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Firebase Auth require Google Account on the device to run successfully


        createBTN = findViewById(R.id.acc_sing_up_button);
        password_create = findViewById(R.id.password_create);
        email_create = findViewById(R.id.email_create);
        username_create = findViewById(R.id.userName_create_ET);


        // Authentication
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null){
                    //already
                }else{
                    //not
                }
            }
        };

        createBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(email_create.getText().toString())
                    && !TextUtils.isEmpty(password_create.getText().toString())){
                    String email = email_create.getText().toString().trim();
                    String password = password_create.getText().toString().trim();
                    String username = username_create.getText().toString().trim();

                    CreateUserEmailAccount(email, password, username);
                }else{
                    Toast.makeText(SingUpActivity.this,
                            "Empty Fields are detected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CreateUserEmailAccount(String email, String password, final String username) {
        if (!TextUtils.isEmpty(email_create.getText().toString())
                && !TextUtils.isEmpty(password_create.getText().toString())){

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //take user to Next Activity:(AddJournalActivity)

                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserID = currentUser.getUid();

                                // Create a userMap so we can create a user in the User Collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userID", currentUserID);
                                userObj.put("username", username);

                                //Adding users to Firestore
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()){
                                                                    String name = task.getResult().getString("sername");

                                                                    // If the user registered successfully,
                                                                    // then move to the AddJournalActivity

                                                                    // Getting use of Global Journal USER
                                                                    JournalUser journalUser = JournalUser.getInstance();
                                                                    journalUser.setUserId(currentUserID);
                                                                    journalUser.setUsername(name);


                                                                    Intent i = new Intent(SingUpActivity.this, AddJournalActivity.class);

                                                                    i.putExtra("username", name);
                                                                    i.putExtra("userId", currentUserID);
                                                                    startActivity(i);
                                                                }else{

                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //Display Failed Message
                                                                Toast.makeText(SingUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    });
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}