package com.fooddelivery.model;

public class Coordinates {
    private double latitude;
    private double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Coordinates{" +
               "latitude=" + latitude +
               ", longitude=" + longitude +
               '}';
    }
    
    /**
     * Calculate distance in kilometers between this set of coordinates and another.
     * Uses Haversine formula.
     * @param other The other Coordinates object.
     * @return Distance in kilometers.
     */
    public double distanceTo(Coordinates other) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // convert to kilometers
    }
}
