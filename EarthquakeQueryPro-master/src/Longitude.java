
public class Longitude {
    private float lon;

    /**
     * Construct a Longitude object with a float
     * @param _lon is the degree of the longitude in float
     */
    public Longitude(float _lon) {
        lon = _lon;
    }

    /**
     * @return float is the value of the longitude
     */
    public float value() {
        return lon;
    }

    /**
     * Modify the longitude with a float
     * @param _lon is the degree of the longitude in float
     */
    public void set(float _lon) {
        lon = _lon;
    }

    /**
     * @return String is the output of the longitude
     * Convert sign symbol to W and E
     */
    public String toString() {
        if (lon == 180 || lon == -180)
            return "180.00°";
        if (lon > 0)
            return String.format("%.2f° E", lon);
        if (lon < 0)
            return String.format("%.2f° W", -lon);
        return String.format("%.2f°", lon);
    }
}
