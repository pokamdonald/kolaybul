package com.finance.kolaybul;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    private String id;
    private String name;
    private String status;
    private String description;
    private String locationFound;
    private String locationToCollect;
    private String category;
    private String phone;
    private String datePosted;
    private String imageBase64; // Store Base64 image string

    public Item(String id, String name, String status, String description,
                String locationFound, String locationToCollect, String category,
                String phone, String datePosted, String imageBase64) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.locationFound = locationFound;
        this.locationToCollect = locationToCollect;
        this.category = category;
        this.phone = phone;
        this.datePosted = datePosted;
        this.imageBase64 = imageBase64;
    }

    protected Item(Parcel in) {
        id = in.readString();
        name = in.readString();
        status = in.readString();
        description = in.readString();
        locationFound = in.readString();
        locationToCollect = in.readString();
        category = in.readString();
        phone = in.readString();
        datePosted = in.readString();
        imageBase64 = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocationFound() { return locationFound; }
    public String getLocationToCollect() { return locationToCollect; }
    public String getCategory() { return category; }
    public String getPhone() { return phone; }
    public String getDatePosted() { return datePosted; }
    public String getImageBase64() { return imageBase64; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(description);
        dest.writeString(locationFound);
        dest.writeString(locationToCollect);
        dest.writeString(category);
        dest.writeString(phone);
        dest.writeString(datePosted);
        dest.writeString(imageBase64);
    }
}
