package com.falldetector.safestep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder> {
    
    private List<EmergencyContact> contactsList;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnTestClickListener onTestClickListener;
    
    public interface OnEditClickListener {
        void onEditClick(EmergencyContact contact);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(EmergencyContact contact);
    }
    
    public interface OnTestClickListener {
        void onTestClick(EmergencyContact contact);
    }
    
    public EmergencyContactsAdapter(List<EmergencyContact> contactsList,
                                   OnEditClickListener onEditClickListener,
                                   OnDeleteClickListener onDeleteClickListener,
                                   OnTestClickListener onTestClickListener) {
        this.contactsList = contactsList;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onTestClickListener = onTestClickListener;
    }
    
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactsList.get(position);
        
        holder.tvContactName.setText(contact.getName());
        holder.tvContactPhone.setText(contact.getPhone());
        
        if (contact.getRelationship() != null && !contact.getRelationship().isEmpty()) {
            holder.tvContactRelationship.setText(contact.getRelationship());
            holder.tvContactRelationship.setVisibility(View.VISIBLE);
        } else {
            holder.tvContactRelationship.setVisibility(View.GONE);
        }
        
        // Set avatar initials
        String initials = "";
        if (contact.getName() != null && !contact.getName().isEmpty()) {
            String[] nameParts = contact.getName().split(" ");
            for (String part : nameParts) {
                if (!part.isEmpty()) {
                    initials += part.charAt(0);
                }
            }
        }
        holder.tvContactAvatar.setText(initials.toUpperCase());
        
        holder.btnEditContact.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(contact);
            }
        });
        
        holder.btnTestAlert.setOnClickListener(v -> {
            if (onTestClickListener != null) {
                onTestClickListener.onTestClick(contact);
            }
        });
        
        holder.btnDeleteContact.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(contact);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return contactsList.size();
    }
    
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName;
        TextView tvContactPhone;
        TextView tvContactRelationship;
        TextView tvContactAvatar;
        Button btnEditContact;
        Button btnTestAlert;
        Button btnDeleteContact;
        
        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);
            tvContactPhone = itemView.findViewById(R.id.tv_contact_phone);
            tvContactRelationship = itemView.findViewById(R.id.tv_contact_relationship);
            tvContactAvatar = itemView.findViewById(R.id.tv_contact_avatar);
            btnEditContact = itemView.findViewById(R.id.btn_edit_contact);
            btnTestAlert = itemView.findViewById(R.id.btn_test_alert);
            btnDeleteContact = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
