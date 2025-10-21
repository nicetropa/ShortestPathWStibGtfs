import org.apache.commons.lang3.tuple.Pair;

public class Stop {
    private String stopId;
    private String stopName;
    private String stopLat;
    private String stopLong;

    public Stop(String stopId, String stopName, String stopLat, String stopLong) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLong = stopLong;
    }

    public String getStopId() { return stopId; }
    public String getStopName() { return stopName; }
    public Pair<String, String> getCoordinates() { return Pair.of(stopLat, stopLong); }

    public void setStopId(String stopId) { this.stopId = stopId; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public void setStopLat(String stopLat) { this.stopLat = stopLat; }
    public void setStopLong(String stopLong) { this.stopLong = stopLong; }
}
