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

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersHorizontalAdapter extends RecyclerView.Adapter<AllUsersHorizontalAdapter.ViewHolder> {

    public static ArrayList<String> list = new ArrayList<String>();
    Context context ;
    User_Listener user_listener ;

    public AllUsersHorizontalAdapter(@NonNull ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public AllUsersHorizontalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
        return new AllUsersHorizontalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllUsersHorizontalAdapter.ViewHolder holder, int position) {
        if (!list.get(position).isEmpty()){
            Picasso.get().load(list.get(position)).into(holder.user_image);
        } else {
            holder.user_image.setImageResource(R.drawable.user);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView user_image  ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_image = itemView.findViewById(R.id.profile_image);
        }
    }
}
