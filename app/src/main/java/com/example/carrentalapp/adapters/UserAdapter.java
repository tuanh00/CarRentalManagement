// UserAdapter.java
package com.example.carrentalapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private FirebaseFirestore db;
    private boolean isAdmin;

    /**
     * Constructor for UserAdapter.
     * @param context The context in which the adapter is used.
     * @param userList The list of users to display.
     * @param isAdmin Flag indicating if the current user is an admin.
     */
    public UserAdapter(Context context, List<User> userList, boolean isAdmin) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds data to each user item view.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textViewUserName.setText(user.getFirstName() + " " + user.getLastName());
        holder.textViewUserEmail.setText(user.getEmail());

        // Load user profile image if available
        String profileImageUrl = user.getImgUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_user_avatar_placeholder) // Ensure this drawable exists
                    .error(R.drawable.ic_user_avatar_placeholder)       // Fallback in case of error
                    .into(holder.imageViewAvatar);
        } else {
            holder.imageViewAvatar.setImageResource(R.drawable.ic_user_avatar_placeholder);
        }

        // Admin-specific: Show block/unblock button
        if (isAdmin) {
            holder.buttonBlockUnblock.setVisibility(View.VISIBLE);
            if (user.isBlocked()) {
                holder.buttonBlockUnblock.setText("Unblock");
                holder.buttonBlockUnblock.setBackgroundTintList(context.getResources().getColorStateList(R.color.colorAccent));
            } else {
                holder.buttonBlockUnblock.setText("Block");
                holder.buttonBlockUnblock.setBackgroundTintList(context.getResources().getColorStateList(R.color.colorMoneyGreen));
            }

            holder.buttonBlockUnblock.setOnClickListener(v -> {
                toggleBlockStatus(user, position);
            });
        } else {
            holder.buttonBlockUnblock.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Toggles the blocked status of a user.
     * @param user The user whose status is to be toggled.
     * @param position The position of the user in the list.
     */
    private void toggleBlockStatus(User user, int position) {
        boolean newStatus = !user.isBlocked();
        db.collection("Users").document(user.getUid())
                .update("blocked", newStatus)
                .addOnSuccessListener(aVoid -> {
                    user.setBlocked(newStatus);
                    notifyItemChanged(position);
                    String message = newStatus ? "User blocked successfully" : "User unblocked successfully";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ViewHolder class for user items.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUserName, textViewUserEmail;
        Button buttonBlockUnblock;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            buttonBlockUnblock = itemView.findViewById(R.id.buttonBlockUnblock);
        }
    }
}
