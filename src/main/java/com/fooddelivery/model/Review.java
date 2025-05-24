package com.fooddelivery.model;

public class Review {
    private String userId;
    private int rating; // 1-5
    private String comment; // Optional

    public Review(String userId, int rating, String comment) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters
    public String getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return "Review{" +
               "userId='" + userId + '\'' +
               ", rating=" + rating +
               (comment != null && !comment.isEmpty() ? ", comment='" + comment + '\'' : "") +
               '}';
    }
}
