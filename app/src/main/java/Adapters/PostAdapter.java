package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cleansweep.R;

import java.util.List;

import Models.PostObject;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder>{

    Context mContext;
    List<PostObject> mData;

    public PostAdapter(Context mContext, List<PostObject> mData){
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.postTitle.setText(mData.get(position).getTitleVal());
        String userid = mData.get(position).getUserIdVal();
        String Username = mData.get(position).getUserNameVal();
        holder.userName.setText(Username);
        Glide.with(mContext).load(mData.get(position).getUserPhotoVal()).into(holder.userPhoto);
        holder.startTime.setText(mData.get(position).getStartDateVal());
        holder.endTime.setText(mData.get(position).getEndDateVal());
        holder.postLocation.setText(mData.get(position).getLocationNameVal());
        Glide.with(mContext).load(mData.get(position).getImageVal()).into(holder.postImage);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView userName;
        ImageView userPhoto;
        TextView startTime;
        TextView endTime;
        TextView postLocation;
        ImageView postImage;
        Button joinButton;


        public MyViewHolder(View itemView){
            super(itemView);
            postTitle = itemView.findViewById(R.id.postitem_title);
            userName = itemView.findViewById(R.id.postitem_username);
            userPhoto = itemView.findViewById(R.id.postitem_userimage);
            startTime = itemView.findViewById(R.id.postitem_starttime);
            endTime = itemView.findViewById(R.id.postitem_endtime);
            postLocation = itemView.findViewById(R.id.postitem_location);
            postImage = itemView.findViewById(R.id.postitem_image);
            joinButton = itemView.findViewById(R.id.postitem_join);
        }
    }
}
