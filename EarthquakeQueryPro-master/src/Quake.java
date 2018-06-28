public class Quake {
    private int id;
    private String date;
    private Latitude latitude;
    private Longitude longitude;
    private int depth;
    private float magnitude;
    private String region;
    public static final String WORLDWIDE = "--- World Wide ---";

    public Quake(int id, String date, float latitude, float longitude, int depth, float magnitude, String region) {
        this.id = id;
        this.date = date;
        this.latitude = new Latitude(latitude);
        this.longitude = new Longitude(longitude);
        this.depth = depth;
        this.magnitude = magnitude;
        this.region = region;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Latitude getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude.set(latitude);
    }

    public Longitude getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude.set(longitude);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getMagnitude() {
        return String.format("%.1f", magnitude);
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
