package com.example.conferenceproject.Firebase;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.conferenceproject.Model.Constants;
import com.example.conferenceproject.View.IncommingCall_Activity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingServices extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // this is the token for the message out from my phone
        Log.d("YOUR FCM TOKEN IS : => ", token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        // here is where i can receive the json that the sender send to the device with token of the receiver

        String type = message.getData().get(Constants.REMOTE_MSG_TYPE);

        // we are checking if this type is the invitation type
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                // means that the caller is sending his information request , and we need to open the IncommingCall_Activity
                Intent intent = new Intent(getApplicationContext(), IncommingCall_Activity.class);
                intent.putExtra(Constants.REMOTE_MSG_MEETING_TYPE, message.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)); // audio or video
                // the email of the caller who send this json to us
                intent.putExtra("email", message.getData().get("email"));
                // passing the token of the caller who send this json to us to the accept/reject call activity
                intent.putExtra(Constants.REMOTE_INVITER_TOKEN, message.getData().get(Constants.REMOTE_INVITER_TOKEN)); // token of the caller
                intent.putExtra(Constants.REMOTE_MSG_MEETING_ROOM,message.getData().get(Constants.REMOTE_MSG_MEETING_ROOM));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE,message.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
