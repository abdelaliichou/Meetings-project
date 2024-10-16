package com.example.conferenceproject.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.conferenceproject.Controller.AllUsersAdapter;
import com.example.conferenceproject.Controller.AllUsersHorizontalAdapter;
import com.example.conferenceproject.Model.Listener.User_Listener;
import com.example.conferenceproject.Model.UserModel;
import com.example.conferenceproject.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Home_Activity extends AppCompatActivity implements User_Listener {


    RecyclerView recyclerVertical, recyclerHorizontal;
    AllUsersAdapter adapterVertical;
    AllUsersHorizontalAdapter adapterHorizontal;
    ShimmerFrameLayout shimmerFrameLayout;
    ShimmerFrameLayout shimmerFrameLayoutHorizontal;
    SwipeRefreshLayout refresh;
    EditText search_layout;
    ImageView clearSearchImage, user_image;
    MaterialAlertDialogBuilder progressDialog;
    AlertDialog dialog;
    MaterialCardView profile_card , off_card;
    private static final int TIME = 2000 ;
    private long backPressed ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initialisation();
        OnClicks();
        SettingRecycler();
        InitialisationToken();
        refreshOrders();
        Search_User();
        settingUserImage();

    }

    public void refreshOrders() {
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                shimmerFrameLayout.startShimmer();
                AllUsersAdapter.list = GETAllUsers();
                adapterVertical.notifyDataSetChanged();
                //
                shimmerFrameLayoutHorizontal.setVisibility(View.VISIBLE);
                shimmerFrameLayoutHorizontal.startShimmer();
                AllUsersHorizontalAdapter.list = GETAllUsersImages();
                adapterHorizontal.notifyDataSetChanged();
                refresh.setRefreshing(false);
            }
        });
    }

    public void initialisation() {
        progressDialog = new MaterialAlertDialogBuilder(this);
        off_card = findViewById(R.id.off_card);
        user_image = findViewById(R.id.user_image);
        profile_card = findViewById(R.id.profile_card);
        clearSearchImage = findViewById(R.id.clear);
        search_layout = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        shimmerFrameLayout = findViewById(R.id.shimmer);
        shimmerFrameLayoutHorizontal = findViewById(R.id.shimmer2);
        recyclerVertical = findViewById(R.id.recycler);
        recyclerHorizontal = findViewById(R.id.images_recycler);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayoutHorizontal.setVisibility(View.VISIBLE);
        shimmerFrameLayoutHorizontal.startShimmer();
    }

    public void Search_User() {
        search_layout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // recyclerViewAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //  recyclerViewAdapter.Search_note(editable.toString());
                if (editable.toString().trim().length() > 0) {
                    clearSearchImage.setVisibility(View.VISIBLE);
                } else {
                    clearSearchImage.setVisibility(View.GONE);
                }
            }
        });
    }

    public void settingUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(String.class) != null) {
                        if (!snapshot.getValue(String.class).isEmpty()) {
                            Picasso.get().load(snapshot.getValue(String.class)).into(user_image);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home_Activity.this, "Can't charge user image : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SettingRecycler() {
        adapterVertical = new AllUsersAdapter(GETAllUsers(), Home_Activity.this, this);
        recyclerVertical.setAdapter(adapterVertical);
        recyclerVertical.setLayoutManager(new LinearLayoutManager(Home_Activity.this, LinearLayoutManager.VERTICAL, false));

        // horizontal
        adapterHorizontal = new AllUsersHorizontalAdapter(GETAllUsersImages(), Home_Activity.this);
        recyclerHorizontal.setAdapter(adapterHorizontal);
        recyclerHorizontal.setLayoutManager(new LinearLayoutManager(Home_Activity.this, LinearLayoutManager.HORIZONTAL, false));

    }

    public ArrayList<String> GETAllUsersImages() {
        ArrayList<String> liste = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                liste.clear();
                for (DataSnapshot ActiveUser : snapshot.getChildren()) {
                    UserModel EnLignUsers = ActiveUser.getValue(UserModel.class);
                    // getting the current app user token
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FCM_Token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String CurrentUserToken = snapshot.getValue(String.class);
                                if (!CurrentUserToken.equals(EnLignUsers.getToken())) {
                                    // so that the user don't call him self obviously !
                                    liste.add(EnLignUsers.getImage());
                                    liste.add("https://firebasestorage.googleapis.com/v0/b/conference-app-8cd70.appspot.com/o/Users%20pictures%2FbNyzdoI6vRQXBgYZBFx6atGugA22?alt=media&token=7a843587-bee3-4c62-a3b4-479ac3fa9d22");
                                    liste.add("https://firebasestorage.googleapis.com/v0/b/conference-app-8cd70.appspot.com/o/Users%20pictures%2FbNyzdoI6vRQXBgYZBFx6atGugA22?alt=media&token=7a843587-bee3-4c62-a3b4-479ac3fa9d22");
                                    liste.add("https://firebasestorage.googleapis.com/v0/b/conference-app-8cd70.appspot.com/o/Users%20pictures%2FbNyzdoI6vRQXBgYZBFx6atGugA22?alt=media&token=7a843587-bee3-4c62-a3b4-479ac3fa9d22");
                                    shimmerFrameLayoutHorizontal.setVisibility(View.GONE);
                                    shimmerFrameLayoutHorizontal.stopShimmer();
                                    adapterHorizontal.notifyDataSetChanged();
                                }
                            }
                            adapterHorizontal.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            shimmerFrameLayoutHorizontal.setVisibility(View.VISIBLE);
                            shimmerFrameLayoutHorizontal.startShimmer();
                            Toast.makeText(Home_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                shimmerFrameLayoutHorizontal.setVisibility(View.VISIBLE);
                shimmerFrameLayoutHorizontal.startShimmer();
                Toast.makeText(Home_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return liste;
    }

    public ArrayList<UserModel> GETAllUsers() {
        ArrayList<UserModel> liste = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                liste.clear();
                for (DataSnapshot ActiveUser : snapshot.getChildren()) {
                    UserModel EnLignUsers = ActiveUser.getValue(UserModel.class);
                    // getting the current app user token
                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FCM_Token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String CurrentUserToken = snapshot.getValue(String.class);
                                if (!CurrentUserToken.equals(EnLignUsers.getToken())) {
                                    // so that the user don't call him self obviously !
                                    liste.add(EnLignUsers);
                                    shimmerFrameLayout.setVisibility(View.GONE);
                                    shimmerFrameLayout.stopShimmer();
                                    adapterVertical.notifyDataSetChanged();
                                }
                            }
                            adapterVertical.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            shimmerFrameLayout.setVisibility(View.VISIBLE);
                            shimmerFrameLayout.startShimmer();
                            Toast.makeText(Home_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                shimmerFrameLayout.startShimmer();
                Toast.makeText(Home_Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return liste;
    }

    public void OnClicks() {

        off_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new MaterialAlertDialogBuilder(Home_Activity.this);
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

        profile_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home_Activity.this, Profile_Activity.class));
            }
        });

        clearSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_layout.setText("");
            }
        });

    }

    public void DeleteFCMTokenFromFirebase() {
        progressDialog = new MaterialAlertDialogBuilder(Home_Activity.this);
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
                    startActivity(new Intent(Home_Activity.this, Login_Activity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(Home_Activity.this, "Token Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void SignoutGoogle(){
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleClient = GoogleSignIn.getClient(Home_Activity.this, options);
        googleClient.signOut();
    }

    public void InitialisationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // sending the token to the ext function
                SendFCMTokenToFirebase(task.getResult());
            } else {
                Toast.makeText(Home_Activity.this, "Token Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SendFCMTokenToFirebase(String token) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FCM_Token").setValue(token).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Toast.makeText(Home_Activity.this, "Token saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Home_Activity.this, "Token Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressed + TIME > System.currentTimeMillis()){
            finishAffinity();
            return ;
        } else {
            Toast.makeText(this, "Double press to exit !", Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }

    // this is the function implemented from the interface
    @Override
    public void startVideoMeeting(UserModel user) {
        if (user.getToken() == null || user.getToken().trim().isEmpty()) {
            Toast.makeText(this, user.getName() + " is not on ligne !", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Home_Activity.this, OutcommingCall_Activity.class);
            intent.putExtra("email", user.getEmail());
            intent.putExtra("name", user.getName());
            intent.putExtra("token", user.getToken());
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    // this is the function implemented from the interface
    @Override
    public void startAudioMeeting(UserModel user) {
        if (user.getToken() == null || user.getToken().trim().isEmpty()) {
            Toast.makeText(this, user.getName() + " is not on ligne !", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Home_Activity.this, OutcommingCall_Activity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("token", user.getToken());
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }
}
