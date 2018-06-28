
public class Latitude {
    private float lat;

    /**
     * Construct a Latitude object with a float
     * @param _lat is the degree of the latitude in float
     */
    public Latitude(float _lat) {
        lat = _lat;
    }

    /**
     * @return float the value of the latitude
     */
    public float value() {
        return lat;
    }

    /**
     * Modify the latitude with a float
     * @param _lat is the degree of the latitude in float
     */
    public void set(float _lat) {
        lat = _lat;
    }

    /**
     * @return String is the output of the latitude
     * Convert sign symbol to N and S
     */
    public String toString() {
        if (lat > 0)
            return String.format("%.2f° N", lat);
        if (lat < 0)
            return String.format("%.2f° S", -lat);
        return String.format("%.2f°", lat);
    }
}
