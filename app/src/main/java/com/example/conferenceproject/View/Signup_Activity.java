package com.example.conferenceproject.View;

import android.content.Intent;
import android.os.Bundle;
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

public class Signup_Activity extends AppCompatActivity {

    TextView login_text, welcomText, ORText;
    RelativeLayout login_button, google_button;
    EditText email_layout, password_layout, phone_layout, name_layout;
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
        setContentView(R.layout.activity_signup);

        initialisation();
        OnClicks();

    }

    public void initialisation() {
        // the status/navigation bar will be transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        name_layout = findViewById(R.id.name_layout);
        bottomlinear = findViewById(R.id.linear);
        ORText = findViewById(R.id.or_text);
        welcomText = findViewById(R.id.welcom);
        phone_layout = findViewById(R.id.phone_number_layout);
        login_text = findViewById(R.id.login_text);
        login_button = findViewById(R.id.login_button);
        google_button = findViewById(R.id.google_button);
        email_layout = findViewById(R.id.email_layout);
        password_layout = findViewById(R.id.password_layout);
        progressDialog = new MaterialAlertDialogBuilder(this);
    }

    public void OnClicks() {
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyTextField();
            }
        });
        login_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleInitialisation();
                Intent i = googleSignInClient.getSignInIntent();
                startActivityForResult(i, 1234);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credintal = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credintal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.setTitle("Wait a minute please !");
                            progressDialog.setMessage("Registering you...");
                            progressDialog.setCancelable(false);
                            progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
                            progressDialog.setIcon(R.drawable.ic__cloud_upload);
                            dialog = progressDialog.show();
                            dialog.show();
                            RejesterGoogleUser();
                        } else {
                            Toast.makeText(Signup_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void GoogleInitialisation() {
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(Signup_Activity.this, options);
    }

    public void VerifyTextField() {
        if (name_layout.getText().toString().trim().isEmpty()) {
            name_layout.setError("Enter your name");
        } else if (phone_layout.getText().toString().trim().isEmpty()) {
            phone_layout.setError("Enter your number");
        } else if (email_layout.getText().toString().trim().isEmpty()) {
            email_layout.setError("Enter your email");
        } else if (!email_layout.getText().toString().trim().toLowerCase().matches(emailPattern) && !email_layout.getText().toString().trim().toLowerCase().matches(emailPattern2)) {
            email_layout.setError("Invalid email form");
        } else if (password_layout.getText().toString().trim().isEmpty()) {
            password_layout.setError("Enter your password");
        } else if (password_layout.getText().toString().trim().length() < 7) {
            password_layout.setError("Short password");
        } else {
            progressDialog.setTitle("Wait a minute please !");
            progressDialog.setMessage("We are registering you !");
            progressDialog.setCancelable(false);
            progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
            progressDialog.setIcon(R.drawable.ic__cloud_upload);
            dialog = progressDialog.show();
            dialog.show();
            RejesterUser(name_layout.getText().toString().trim(), email_layout.getText().toString().trim().toLowerCase(), password_layout.getText().toString().trim().toLowerCase(), phone_layout.getText().toString().trim());
        }
    }

    public void RejesterUser(String name, String email, String password, String number) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    UserModel user = new UserModel(name, email, number, "", "");
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Signup_Activity.this, "Successfully Signed in !", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                startActivity(new Intent(Signup_Activity.this, Home_Activity.class));
                                finishAffinity();
                            } else {
                                Toast.makeText(Signup_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(Signup_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void RejesterGoogleUser() {
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot id : snapshot.getChildren()) {
                    // verifying if this user id is already logged in the database
                    if (id.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        userID = id.getKey();
                        break;
                    }
                }
                if (userID != null) {
                    // means that ths user has already logged in with this account
                    Toast.makeText(Signup_Activity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signup_Activity.this, Home_Activity.class));
                    finishAffinity();
                    dialog.dismiss();
                } else {
                    // this is his first time he logged in so we will create his child in the real time database
                    UserModel googleUser = new UserModel(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), "", "", "", FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(googleUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Signup_Activity.this, "Welcome !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Signup_Activity.this, Home_Activity.class));
                                finishAffinity();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(Signup_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Signup_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
}