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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cleansweep.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import Models.PostObject;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder>{

    private Context mContext;
    private List<PostObject> mData;
    private String currentUserId;
    public String geoHashval;

    public PostAdapter(Context mContext, List<PostObject> mData, String currentUserId, String geoHash){
        this.mContext = mContext;
        this.mData = mData;
        this.currentUserId = currentUserId;
        this.geoHashval = geoHash;
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
        Glide.with(mContext).load(post.getImageVal()).into(holder.postImage);

        String postGeoHash = post.getGeoHashVal();
        boolean inRange;
        if (postGeoHash.equals(geoHashval)) {
            inRange = true;
        } else {
            inRange = false;
        }

        if (post.getUserIdVal().equals(currentUserId)) {
            holder.joinButton.setText("Delete");
        } else {
            if (post.getParticipants().contains(currentUserId)) {
                holder.joinButton.setText("Leave");
            } else if (!inRange) {
                holder.joinButton.setText("Not in Range");
            } else {
                holder.joinButton.setText("Join");
            }
        }

        holder.joinButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference postRef = db.collection("posts").document(post.getDocId());

            if (post.getUserIdVal().equals(currentUserId)){
                postRef.delete();
            } else {
                if (post.getParticipants().contains(currentUserId)) {
                    // Remove user from participants
                    post.removeParticipant(currentUserId);
                    postRef.update("participants", post.getParticipants());
                    holder.joinButton.setText("Join");
                } else if (!inRange) {
                    // do nothing for now
                } else {
                    // Add user to participants
                    post.addParticipant(currentUserId);
                    postRef.update("participants", post.getParticipants());
                    holder.joinButton.setText("Leave");
                }
                holder.partcipantCount.setText("Partcicpants: " + post.getParticipantsCount());
            }


        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView userName;
        ImageView userPhoto;
        TextView startTime;
        TextView endTime;
        TextView partcipantCount;
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
            partcipantCount = itemView.findViewById(R.id.postitem_participantcount);
            postLocation = itemView.findViewById(R.id.postitem_location);
            postImage = itemView.findViewById(R.id.postitem_image);
            joinButton = itemView.findViewById(R.id.postitem_join);
        }
    }
}
