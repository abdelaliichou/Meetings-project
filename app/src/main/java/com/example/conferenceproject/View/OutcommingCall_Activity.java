package com.example.conferenceproject.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.conferenceproject.Model.Api.ApiService;
import com.example.conferenceproject.Model.Api.Api_Client;
import com.example.conferenceproject.Model.Constants;
import com.example.conferenceproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutcommingCall_Activity extends AppCompatActivity {

    TextView username, calltype;
    CardView end_call;
    ImageView vedioImage;
    ImageView audioImage;
    String InviterToken = null;
    String meetingRoom = null;
    String meetingType = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcomming_call);

        initialisation();
        ChargingInitialData();
        OnClicks();
        SettingInviterToken();

    }

    public void InitialisationMeeting(String meetingType, String receiverToken) {
        // this function will create a json object that will be sent to the FirebaseMessagingListener ,
        // and when that listener will receive it will start the incomingCall_Activity for the receiver user
        // because of his token , and it will charge all the data of the caller in the receiver phone .
        try {
            // this is the json array of the person who is starting the call
            // for sending all his info to the receiver user

            // we need to see the architecture picture in the drawables to better understand this Architecture
            JSONArray tokens = new JSONArray();
            // the receiverToken variable is for the receiver user
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            // data of the caller
            data.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            data.put(Constants.REMOTE_INVITER_TOKEN, InviterToken);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + UUID.randomUUID().toString().substring(0, 5));

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            // send this json to the FirebaseReceiver
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void sendRemoteMessage(String jsonMessageBody, String type) {
        Api_Client.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), jsonMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        // witch mean that we called it from the InitialisationMeeting function
                        Toast.makeText(OutcommingCall_Activity.this, "invitation sent successfully !", Toast.LENGTH_SHORT).show();
                    } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        Toast.makeText(OutcommingCall_Activity.this, "invitation Cancelled !", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OutcommingCall_Activity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(OutcommingCall_Activity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void CancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            // the receiverToken variable is for the receiver not the caller
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            // send this json to the FirebaseReceiver
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void SettingInviterToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // the caller token
                InviterToken = task.getResult();
                if (getIntent().getStringExtra("type") != null && getIntent().getStringExtra("token") != null) {
                    // we are passing the receiver token to the function
                    InitialisationMeeting(getIntent().getStringExtra("type"), getIntent().getStringExtra("token"));
                } else {
                    Toast.makeText(this, "Can't find the receiver user !", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(OutcommingCall_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void initialisation() {
        // the status/navigation bar will be transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        meetingType = getIntent().getStringExtra("type");
        vedioImage = findViewById(R.id.video);
        audioImage = findViewById(R.id.audio);
        username = findViewById(R.id.user_name);
        calltype = findViewById(R.id.call_type);
        end_call = findViewById(R.id.respond_false);
    }

    public void OnClicks() {
        end_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // token of the receiver not the caller
                CancelInvitation(getIntent().getStringExtra("token"));
                // onBackPressed();
            }
        });
    }

    public void ChargingInitialData() {
        username.setText(getIntent().getStringExtra("email"));
        switch (getIntent().getStringExtra("type")) {
            case "audio":
                calltype.setText("Audio call with " + getIntent().getStringExtra("name"));
                audioImage.setVisibility(View.VISIBLE);
                vedioImage.setVisibility(View.GONE);
                break;
            case "video":
                calltype.setText("video call with " + getIntent().getStringExtra("name"));
                audioImage.setVisibility(View.GONE);
                vedioImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    public BroadcastReceiver invitation_response_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    // means that the receiver accepted the call
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutcommingCall_Activity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(OutcommingCall_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation rejected !", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitation_response_receiver, new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitation_response_receiver
        );
    }
}