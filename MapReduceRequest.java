import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MapReduceRequest implements Serializable {

    private double clientLatitude;
    private double clientLongitude;
    private ArrayList<String> foodCategories;
    private double minStars;
    private String priceCategory;
    private double radius;


    public MapReduceRequest(double clientLatitude, double clientLongitude, ArrayList<String> foodCategories, double minStars, String priceCategory, double radius) {
        this.clientLatitude = clientLatitude;
        this.clientLongitude = clientLongitude;
        this.foodCategories = foodCategories;
        this.minStars = minStars;
        this.priceCategory = priceCategory;
        this.radius = radius;
    }


    public double getClientLatitude() {
        return clientLatitude;
    }

    public void setClientLatitude(double clientLatitude) {
        this.clientLatitude = clientLatitude;
    }

    public double getClientLongitude() {
        return clientLongitude;
    }

    public void setClientLongitude(double clientLongitude) {
        this.clientLongitude = clientLongitude;
    }

    public List<String> getFoodCategories() {
        return foodCategories;
    }

    public void setFoodCategories(ArrayList<String> foodCategories) {
        this.foodCategories = foodCategories;
    }

    public double getMinStars() {
        return minStars;
    }

    public void setMinStars(double minStars) {
        this.minStars = minStars;
    }

    public String getPriceCategory() {
        return priceCategory;
    }

    public void setPriceCategory(String priceCategory) {
        this.priceCategory = priceCategory;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "MapReduceRequest{" +
                "clientLatitude=" + clientLatitude +
                ", clientLongitude=" + clientLongitude +
                ", foodCategories=" + foodCategories +
                ", minStars=" + minStars +
                ", priceCategory='" + priceCategory + '\'' +
                ", radius=" + radius +
                '}';
    }
}
