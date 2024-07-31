package Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cleansweep.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.lang.Math;

import Models.PostObject;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder>{

    private Context mContext;
    private List<PostObject> mData;
    private String currentUserId;
    public String geoHashval;
    public String date;

    public PostAdapter(Context mContext, List<PostObject> mData, String currentUserId, String geoHash, String currentDate){
        this.mContext = mContext;
        this.mData = mData;
        this.currentUserId = currentUserId;
        this.geoHashval = geoHash;
        this.date = currentDate;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new MyViewHolder(row);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PostObject post = mData.get(position);
        holder.postTitle.setText(post.getTitleVal());
        holder.userName.setText(post.getUserNameVal());
        Glide.with(mContext).load(post.getUserPhotoVal()).into(holder.userPhoto);
        holder.startTime.setText(post.getStartDateVal());
        holder.endTime.setText(post.getEndDateVal());
        holder.partcipantCount.setText("Partcicpants: " + post.getParticipantsCount());
        holder.postLocation.setText(post.getLocationNameVal());
        holder.latlon.setText(getlatlon(Double.parseDouble(post.getLatitudeVal()), Double.parseDouble(post.getLongitudeVal())));
        Glide.with(mContext).load(post.getImageVal()).into(holder.postImage);

        String postGeoHash = post.getGeoHashVal();
        boolean inRange;
        if (postGeoHash.equals(geoHashval)) {
            inRange = true;
        } else {
            inRange = false;
        }

        boolean isToday = post.getStartDateVal().compareTo(date) <= 0 && post.getEndDateVal().compareTo(date) >= 0;

        if (post.getUserIdVal().equals(currentUserId)) {
            holder.joinButton.setText("Delete");
            holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.fadedbrown));
            holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.brown));
        } else {
            if (post.getParticipants().contains(currentUserId)) {
                holder.joinButton.setText("Leave");
                holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.fadedolive));
                holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.olive));
            } else if (!isToday) {
                holder.joinButton.setText("Upcoming");
                holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.brown));
                holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.fadedbrown));
            } else if (!inRange) {
                holder.joinButton.setText("Not near Location");
                holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.brown));
                holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.fadedbrown));
            } else {
                holder.joinButton.setText("Join");
                holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.darkestolive));
                holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightolive));
            }
        }

        holder.joinButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference postRef = db.collection("posts").document(post.getDocId());

            if (post.getUserIdVal().equals(currentUserId)){
                postRef.delete();
                // delete the post
                mData.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mData.size());
            } else {
                if (post.getParticipants().contains(currentUserId)) {
                    // Remove user from participants
                    post.removeParticipant(currentUserId);
                    postRef.update("participants", post.getParticipants());
                    holder.joinButton.setText("Join");
                    holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.darkestolive));
                    holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightolive));
                } else if (!isToday) {
                    // do nothing for now
                } else if (!inRange) {
                    // do nothing for now
                } else {
                    // Add user to participants
                    post.addParticipant(currentUserId);
                    postRef.update("participants", post.getParticipants());
                    holder.joinButton.setText("Leave");
                    holder.joinButton.setTextColor(ContextCompat.getColor(mContext, R.color.fadedolive));
                    holder.joinButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.olive));
                }
                holder.partcipantCount.setText("Partcicpants: " + post.getParticipantsCount());
            }


        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String getlatlon(double latitude, double longitude){
        double remainder = 0;
        String latlon = "";
//        latitude
        remainder = Math.abs(latitude);
        latlon += String.valueOf((int)Math.floor(remainder)) + "°";
        remainder = (remainder - Math.floor(remainder)) * 60;
        latlon += String.valueOf((int)Math.floor(remainder)) + "'";
        remainder = (remainder - Math.floor(remainder)) * 60;
        latlon += String.valueOf(Math.floor(remainder)) + '"';
        if (latitude < 0){
            latlon += "S";
        } else {
            latlon += "N";
        }
        latlon += " ";
        //        longitude
        remainder = Math.abs(longitude);
        latlon += String.valueOf((int)Math.floor(remainder)) + "°";
        remainder = (remainder - Math.floor(remainder)) * 60;
        latlon += String.valueOf((int)Math.floor(remainder)) + "'";
        remainder = (remainder - Math.floor(remainder)) * 60;
        latlon += String.valueOf(Math.floor(remainder)) + '"';
        if (longitude < 0){
            latlon += "W";
        } else {
            latlon += "E";
        }

        return latlon;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView userName;
        ImageView userPhoto;
        TextView startTime;
        TextView endTime;
        TextView partcipantCount;
        TextView postLocation;
        TextView latlon;
        ImageView postImage;
        Button joinButton;

        public MyViewHolder(View itemView){
            super(itemView);
            postTitle = itemView.findViewById(R.id.postitem_title);
            userName = itemView.findViewById(R.id.postitem_username);
            userPhoto = itemView.findViewById(R.id.postitem_userimage);
            startTime = itemView.findViewById(R.id.postitem_starttime);
            endTime = itemView.findViewById(R.id.postitem_endtime);
            partcipantCount = itemView.findViewById(R.id.postitem_participantcount);
            postLocation = itemView.findViewById(R.id.postitem_location);
            latlon = itemView.findViewById(R.id.postitem_latlon);
            postImage = itemView.findViewById(R.id.postitem_image);
            joinButton = itemView.findViewById(R.id.postitem_join);
        }
    }
}
