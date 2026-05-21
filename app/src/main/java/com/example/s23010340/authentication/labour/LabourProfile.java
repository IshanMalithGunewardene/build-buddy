package com.example.s23010340.authentication.labour;

public class LabourProfile {
    private final int id;
    private final String name;
    private final String category;
    private final String phone;
    private final String location;
    private final String district;
    private final String city;
    private final String photoUri;
    private final boolean available;
    private final boolean hired;

    public LabourProfile(
        int id,
        String name,
        String category,
        String phone,
        String location,
        String district,
        String city,
        String photoUri,
        boolean available,
        boolean hired
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.phone = phone;
        this.location = location;
        this.district = district;
        this.city = city;
        this.photoUri = photoUri;
        this.available = available;
        this.hired = hired;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isHired() {
        return hired;
    }
}
