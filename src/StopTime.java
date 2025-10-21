import com.opencsv.bean.CsvBindByName;

public class StopTime implements Comparable<StopTime>{

    private String routeName;

    private String routeType;

    private String stopName;

    private int departureTimeInSec;

    @CsvBindByName(column = "trip_id")
    private String tripId;

    @CsvBindByName(column = "departure_time")
    private String departureTime;

    @CsvBindByName(column = "stop_id")
    private String stopId;

    @CsvBindByName(column = "stop_sequence")
    private int stopSequence;


    public StopTime() {
    }

    public StopTime(String tripId, String departureTime, String stopId, String stopSequence) {
        this.tripId = tripId;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = Integer.parseInt(stopSequence);
        departureTimeInSec = computeDepartureTimeInSeconds();
    }

    public int computeDepartureTimeInSeconds() {
        String[] parts = departureTime.split(":"); 
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    @Override
    public int compareTo(StopTime other) {
        int comparaison = this.tripId.compareTo(other.tripId);
        if (comparaison != 0) {
            return comparaison;
        }
        else {
            return Integer.compare(Integer.valueOf(this.stopSequence), Integer.valueOf(other.stopSequence));
        }
    }

    public String getStopName() {
        return stopName;
    }

    public String getTripId() {
        return tripId;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteType() {
        return routeType;
    }

    public int getDepartureTimeInSeconds() {
        return departureTimeInSec;
    }

    public void setStopName(String newStopName) {
        stopName = newStopName;
    }

    public void setTripId(String newTripId) {
        tripId = newTripId;
    }

    public void setDepartureTime(String newDepartureTime) {
        departureTime = newDepartureTime;
    }

    public void setStopId(String newStopId) {
        stopId = newStopId;
    }

    public void setStopSequence(int newStopSequence) {
        stopSequence = newStopSequence;
    }

    public void setRouteName(String newRouteName) {
        routeName = newRouteName;
    }

    public void setRouteType(String newRouteType) {
        routeType = newRouteType;
    }
}
