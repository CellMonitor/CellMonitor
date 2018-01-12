package com.example.joe.cellmonitor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dell on 1/12/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    Context context;
    List<Users> list;

    public RecyclerViewAdapter(Context context, List<Users> TempList) {

        this.list = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Users users = list.get(position);

        holder.name.setText(users.getName());
        Picasso.with(context).load(users.getThumb_image()).placeholder(R.drawable.avatar).into(holder.thumb_image);
        holder.status.setText(users.getStatus());

    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView status;
        public CircleImageView thumb_image;

        public ViewHolder(View itemView) {

            super(itemView);
            name = (TextView) itemView.findViewById(R.id.user_single_name);
            status = (TextView) itemView.findViewById(R.id.user_single_status);
            thumb_image = (CircleImageView)itemView.findViewById(R.id.user_single_image);
        }

        public void setUserImage ( String thumb_image ){

        }
    }
}