package com.example.conferenceproject.Controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conferenceproject.Model.Listener.User_Listener;
import com.example.conferenceproject.Model.UserModel;
import com.example.conferenceproject.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {

    public static ArrayList<UserModel> list = new ArrayList<UserModel>();
    Context context ;
    User_Listener user_listener ;

    public AllUsersAdapter(@NonNull ArrayList<UserModel> list, Context context , User_Listener user_listener) {
        this.list = list;
        this.context = context;
        this.user_listener = user_listener ;
    }

    @NonNull
    @Override
    public AllUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout2, parent, false);
        return new AllUsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllUsersAdapter.ViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        holder.parent_card.setAnimation(AnimationUtils.loadAnimation(context, R.anim.card_pop_up));
        if (list.get(position).getImage() != null){
            if (!list.get(position).getImage().isEmpty()) {
                Picasso.get().load(list.get(position).getImage()).into(holder.user_image);
            } else {
                holder.user_image.setImageResource(R.drawable.user);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView  name ;
        ImageView user_image , audio , vedio ;
        MaterialCardView parent_card ;
        CardView audio_card , video_card ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            audio_card = itemView.findViewById(R.id.audio_card);
            video_card = itemView.findViewById(R.id.video_card);
            audio = itemView.findViewById(R.id.audio);
            vedio = itemView.findViewById(R.id.video);
            name = itemView.findViewById(R.id.user_name);
            user_image = itemView.findViewById(R.id.profile_image);
            parent_card = itemView.findViewById(R.id.parent_card);

            audio_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // calling the function implemented in the Home_Activity from the interface
                    user_listener.startAudioMeeting(list.get(getAdapterPosition()));
                }
            });

            video_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // calling the function implemented in the Home_Activity from the interface
                    user_listener.startVideoMeeting(list.get(getAdapterPosition()));
                }
            });
        }
    }
}
