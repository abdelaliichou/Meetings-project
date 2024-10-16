package com.example.conferenceproject.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Profile_Activity extends AppCompatActivity {

    TextInputLayout email_layout, number_layout, name_layout;
    RelativeLayout update_button, logout_button, about_us_button;
    ProgressBar progressBar;
    MaterialAlertDialogBuilder progressDialog;
    AlertDialog dialog;
    String emailPattern = "[a-zA-Z0-9._-]+@esi-sba.dz";
    String emailPattern2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ImageView profile_image;
    Uri image_uri = null;
    DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initialisation();
        initialisationFields();
        OnClicks();

    }

    public void initialisation() {
        progressDialog = new MaterialAlertDialogBuilder(this);
        root = FirebaseDatabase.getInstance().getReference();
        profile_image = findViewById(R.id.user_image);
        progressBar = findViewById(R.id.progress);
        email_layout = findViewById(R.id.email_layout);
        number_layout = findViewById(R.id.number_layout);
        name_layout = findViewById(R.id.name_layout);
        update_button = findViewById(R.id.update_button);
        about_us_button = findViewById(R.id.help_button);
        logout_button = findViewById(R.id.logout_button);
    }

    public void initialisationFields() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    UserModel user = snapshot.getValue(UserModel.class);
                    email_layout.getEditText().setText(user.getEmail());
                    name_layout.getEditText().setText(user.getName());
                    number_layout.getEditText().setText(user.getPhonenumber());
                    if (!user.getImage().isEmpty()){
                        Picasso.get().load(user.getImage()).into(profile_image);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void OnClicks() {
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "pick an image !"), 10);
            }
        });
        about_us_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://abdelali-ichou.vercel.app/")));
            }
        });
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyFields();
            }
        });
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new MaterialAlertDialogBuilder(Profile_Activity.this);
                progressDialog.setTitle("Déconnecter ?");
                progressDialog.setMessage("Vous êtes sûr que vous voulez déconnecter ?");
                progressDialog.setCancelable(true);
                progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
                progressDialog.setIcon(R.drawable.ic_logout);
                progressDialog.setPositiveButton("oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DeleteFCMTokenFromFirebase();
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // dismiss the orders
                                dialog.dismiss();
                            }
                        });
                progressDialog.setCancelable(false);
                dialog = progressDialog.show();
                dialog.show();
            }
        });
    }

    // gets the image that we select from our phone and puts it in the imageuri variable
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == -1 && data != null && data.getData() != null) {
            // this is the uri of the image that w have selected from the Phone Gallery
            image_uri = data.getData();
            profile_image.setImageURI(image_uri);
            Toast.makeText(Profile_Activity.this, "Image selected successfully !", Toast.LENGTH_SHORT).show();
        }
    }

    public void DeleteFCMTokenFromFirebase() {
        progressDialog = new MaterialAlertDialogBuilder(Profile_Activity.this);
        progressDialog.setTitle("Wait a second please");
        progressDialog.setMessage("We are signing you out ...");
        progressDialog.setCancelable(false);
        progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
        progressDialog.setIcon(R.drawable.ic_logout);
        dialog = progressDialog.show();
        dialog.show();
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FCM_Token").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // the token has been deleted from this user account attribute
                    dialog.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    SignoutGoogle();
                    startActivity(new Intent(Profile_Activity.this, Login_Activity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(Profile_Activity.this, "Token Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void SignoutGoogle(){
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleClient = GoogleSignIn.getClient(Profile_Activity.this, options);
        googleClient.signOut();
    }

    public void VerifyFields() {
        if (name_layout.getEditText().getText().toString().trim().isEmpty()) {
            name_layout.setError("Enter your name");
        } else if (email_layout.getEditText().getText().toString().trim().isEmpty()) {
            email_layout.setError("Enter your email");
        } else if (!email_layout.getEditText().getText().toString().trim().toLowerCase().matches(emailPattern) && !email_layout.getEditText().getText().toString().trim().toLowerCase().matches(emailPattern2)) {
            email_layout.setError("Invalid email form");
        } else if (number_layout.getEditText().getText().toString().trim().isEmpty()) {
            number_layout.setError("Enter your number !");
        } else {
            progressDialog.setTitle("Update user ...");
            progressDialog.setMessage("Wait  minute please !");
            progressDialog.setCancelable(false);
            progressDialog.setBackground(getResources().getDrawable(R.drawable.alert_dialog_back));
            progressDialog.setIcon(R.drawable.update);
            dialog = progressDialog.show();
            dialog.show();
            update(name_layout.getEditText().getText().toString().trim(),
                    email_layout.getEditText().getText().toString().trim(),
                    number_layout.getEditText().getText().toString().trim(),
                    image_uri);
        }
    }

    public void update(String name, String email, String number, Uri picture) {
        root.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    root.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phonenumber").setValue(number).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // updating the email
                                FirebaseAuth.getInstance().getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            // updating it in the real time data base
                                            root.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        // updating the image
                                                        if (picture != null) { // the user changed his profile image  , so we will charge his new profile picture
                                                            StorageReference storage = FirebaseStorage.getInstance().getReference().child("Users pictures").child((FirebaseAuth.getInstance().getCurrentUser().getUid()));
                                                            storage.putFile(picture).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                // we charge the new image in the storage firebase after that we charge it in the real time data base
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                    // which means that we have uploaded the image to the firebase
                                                                    // and now we are going to get our image uri as a string
                                                                    if (task.isSuccessful()) {
                                                                        storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                            @Override
                                                                            public void onSuccess(Uri uri) {
                                                                                // this uri makes us able to charge the image in the imageview every time ( its like url )
                                                                                root.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            Toast.makeText(Profile_Activity.this, "Profile updated successfully !", Toast.LENGTH_SHORT).show();
                                                                                            dialog.dismiss();
                                                                                            finish();
                                                                                        } else {
                                                                                            Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    } else {
                                                                        Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(Profile_Activity.this, "Profile updated successfully !", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                            dialog.dismiss();
                                                        }
                                                    } else {
                                                        Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            } else { // the user did not change his profile image
                                Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(Profile_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
}