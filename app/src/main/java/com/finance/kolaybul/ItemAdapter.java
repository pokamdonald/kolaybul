package com.finance.kolaybul;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private final Context context;
    private List<Item> itemList;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        // Convert Base64 string to Bitmap & load into ImageView
        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.itemImage.setImageBitmap(decodedBitmap);

                //  Show full image when clicked
                holder.itemImage.setOnClickListener(v -> showFullImagePopup(v.getContext(), decodedBitmap));
            } catch (Exception e) {
                holder.itemImage.setImageResource(R.drawable.noimage); // Fallback image
            }
        } else {
            holder.itemImage.setImageResource(R.drawable.noimage); // Default image
        }

        //  Bind TextViews
        holder.txtName.setText(item.getName());
        holder.txtStatus.setText("Status: " + item.getStatus());
        holder.txtDescription.setText("Description: " + item.getDescription());
        holder.txtLocation.setText("Location Found: " + item.getLocationFound());
        holder.txtLocationToCollect.setText("Location to Collect: " + item.getLocationToCollect());
        holder.txtDatePosted.setText("Date: " + item.getDatePosted());
        holder.txtCategory.setText("Category: " + item.getCategory());
        holder.txtPhone.setText("Phone: " + item.getPhone());

        // Claim button action (Call or WhatsApp)
        holder.btnClaim.setOnClickListener(v -> showClaimOptions(item.getPhone()));
    }

    /**
     * Function to Show Full-Size Image in a Popup
     */
    private void showFullImagePopup(Context context, Bitmap image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.popup_image, null);

        ImageView imgFullScreen = view.findViewById(R.id.imgFullScreen);
        imgFullScreen.setImageBitmap(image);

        AlertDialog dialog = builder.setView(view).create();

        //  Close when clicking outside
        view.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    /**
     *  Show Options to Call or Message via WhatsApp
     */
    private void showClaimOptions(String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contact Finder")
                .setMessage("Do you want to call or send a WhatsApp message?")
                .setPositiveButton("Call", (dialog, which) -> openPhoneDialer(phoneNumber))
                .setNegativeButton("WhatsApp", (dialog, which) -> openWhatsApp(phoneNumber))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     *  Open Phone Dialer
     */
    private void openPhoneDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    /**
     *  Open WhatsApp Chat
     */
    private void openWhatsApp(String phoneNumber) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void updateItemList(List<Item> newItemList) {
        itemList.clear();
        itemList.addAll(newItemList);
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        ImageView btnClaim;
        TextView txtName, txtStatus, txtDescription, txtLocation, txtLocationToCollect, txtDatePosted, txtCategory, txtPhone;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            txtName = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtLocationToCollect = itemView.findViewById(R.id.txtLocationToCollect);
            txtDatePosted = itemView.findViewById(R.id.txtDate);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }
    }
}
