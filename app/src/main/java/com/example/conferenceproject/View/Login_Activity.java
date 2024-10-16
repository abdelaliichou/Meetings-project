package com.example.conferenceproject.View;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.conferenceproject.Model.UserModel;
import com.example.conferenceproject.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login_Activity extends AppCompatActivity {

    TextView signup_text, welcomText, ORText;
    RelativeLayout login_button, google_button;
    EditText email_layout, password_layout;
    LinearLayout bottomlinear;
    String emailPattern = "[a-zA-Z0-9._-]+@esi-sba.dz";
    String emailPattern2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    MaterialAlertDialogBuilder progressDialog;
    AlertDialog dialog;
    GoogleSignInClient googleSignInClient;
    GoogleSignInOptions options;
    String userID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialisation();
        OnClicks();
        CheckAlreadyLOGIN();

    }

    public void CheckAlreadyLOGIN() {
       if (FirebaseAuth.getInstance().getCurrentUser() != null ){
           startActivity(new Intent(Login_Activity.this, Home_Activity.class));
           finishAffinity();
       }
    }

    public void initialisation() {
        // the status/navigation bar will be transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ORText = findViewById(R.id.or_text);
        welcomText = findViewById(R.id.welcom);
        signup_text = findViewById(R.id.signup_text);
        login_button = findViewById(R.id.login_button);
        google_button = findViewById(R.id.google_button);
        email_layout = findViewById(R.id.email_layout);
        password_layout = findViewById(R.id.password_layout);
        bottomlinear = findViewById(R.id.linear);
        progressDialog = new MaterialAlertDialogBuilder(this);
    }

    public void OnClicks() {
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyTextField();
            }
        });
        signup_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentSharedAnimation();
            }
        });
        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleInitialisation();
                Intent i = googleSignInClient.getSignInIntent();
                startActivityForResult(i,1234);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credintal = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credintal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // here we will save the google user in the realtime data base and after we will go to the home page
                            progressDialog.setTitle("Wait a minute please !");
                            progressDialog.setMessage("Registering you...");
                            progressDialog.setCancelable(false);
                            progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
                            progressDialog.setIcon(R.drawable.ic__cloud_upload);
                            dialog = progressDialog.show();
                            dialog.show();
                            RejesterGoogleUser();
                        } else {
                            Toast.makeText(Login_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch(ApiException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void RejesterGoogleUser(){
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot id : snapshot.getChildren()){
                    // verifying if this google user id is already logged in the database
                    if (id.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        userID = id.getKey() ;
                        break;
                    }
                }
                if (userID != null){
                    // means that ths user has already logged in with this account
                    Toast.makeText(Login_Activity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                    finishAffinity();
                    dialog.dismiss();
                } else {
                    // this is his first time he logged in so we will create his child in the real time database
                    UserModel googleUser = new UserModel(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),"","","",FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(googleUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Login_Activity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                                finishAffinity();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(Login_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void GoogleInitialisation(){
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(Login_Activity.this, options);
    }

    public void IntentSharedAnimation() {
        Intent intent = new Intent(Login_Activity.this, Signup_Activity.class);
        // shared animation between signup/login activities
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View, String>(welcomText, "text");
        pairs[1] = new Pair<View, String>(email_layout, "email");
        pairs[2] = new Pair<View, String>(password_layout, "password");
        pairs[3] = new Pair<View, String>(login_button, "done");
        pairs[4] = new Pair<View, String>(google_button, "google");
        pairs[5] = new Pair<View, String>(bottomlinear, "bottom_text");
        pairs[6] = new Pair<View, String>(ORText, "or_text");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login_Activity.this, pairs);
            startActivity(intent, options.toBundle());
        }
    }

    public void VerifyTextField() {
        if (email_layout.getText().toString().trim().isEmpty()) {
            email_layout.setError("Enter your email");
        } else if (!email_layout.getText().toString().trim().toLowerCase().matches(emailPattern) && !email_layout.getText().toString().trim().toLowerCase().matches(emailPattern2)) {
            email_layout.setError("Invalid email form");
        } else if (password_layout.getText().toString().trim().isEmpty()) {
            password_layout.setError("Enter your password");
        } else if (password_layout.getText().toString().trim().length() < 7) {
            password_layout.setError("Short password");
        } else {
            progressDialog.setTitle("Wait a minute please !");
            progressDialog.setMessage("Searching for this user...");
            progressDialog.setCancelable(false);
            progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
            progressDialog.setIcon(R.drawable.ic__cloud_upload);
            dialog = progressDialog.show();
            dialog.show();
            VerifyUser(email_layout.getText().toString().trim().toLowerCase(), password_layout.getText().toString().trim().toLowerCase());
        }
    }

    public void VerifyUser(String email, String password) {
        FirebaseAuth.getInstance().signOut();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Welcome !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}