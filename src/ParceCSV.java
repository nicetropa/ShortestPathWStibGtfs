import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ParceCSV {
    private String[] societes = {"DELIJN","SNCB","STIB","TEC"};
   //private static ArrayList<StopTime> stopTimes = new ArrayList<>();
    private HashMap<String, List<StopTime>> stopTimesMap = new HashMap<>();
    private HashMap<String,List<Stop>> stopsMap = new HashMap<>();
    private HashMap<String, Stop> stopIdToStopMap;
    private int stopsCounter = 0;
    private int departureTime;

    public ParceCSV(String departureTimeString) {
        departureTime = GraphBuilder.timeStringToSeconds(departureTimeString);
    }

    private void insertOrdered(StopTime stopTime, List<StopTime> stopTimes) {
        if (stopTimes.isEmpty()) {
            stopTimes.add(stopTime);
            return;
        }
    
        int index = binarySearchInsertIndex(stopTime, stopTimes, 0, stopTimes.size() - 1);
        stopTimes.add(index, stopTime);
    }
    
    private int binarySearchInsertIndex(StopTime stopTime, List<StopTime> stopTimes, int start, int end) {
        if (start >= end) {
            return (stopTime.compareTo(stopTimes.get(start)) > 0) ? start + 1 : start;
        }
    
        int mid = (start + end) / 2;
        if (stopTime.compareTo(stopTimes.get(mid)) > 0) {
            return binarySearchInsertIndex(stopTime, stopTimes, mid + 1, end);
        } else {
            return binarySearchInsertIndex(stopTime, stopTimes, start, mid);
        }
    }

    public HashMap<String, List<StopTime>> parceStopTimes() throws CsvValidationException, IOException {

        for (short i=0; i < societes.length;i++) {
            FileReader fileReader = new FileReader("../data/GTFS/"+societes[i]+"/stop_times.csv");
            CSVReader csvReader = new CSVReader(fileReader);
            
            String[] nextRecord = csvReader.readNext();
            nextRecord = csvReader.readNext();
            while (nextRecord != null) {
                if (GraphBuilder.timeStringToSeconds(nextRecord[1]) >= departureTime && GraphBuilder.timeStringToSeconds(nextRecord[1]) < departureTime+3600*9) {
                    StopTime newStopTime = new StopTime(nextRecord[0], nextRecord[1], nextRecord[2], nextRecord[3]);
                    bindStopNameToStopTime(newStopTime);
                    if (!stopTimesMap.containsKey(nextRecord[0])) {
                        List<StopTime> stopTimes = new ArrayList<>();
                        stopTimes.add(newStopTime);
                        stopTimesMap.put(nextRecord[0], stopTimes);
                    } else {
                        insertOrdered(newStopTime, stopTimesMap.get(nextRecord[0]));
                    }
                }
                nextRecord = csvReader.readNext();
            }
            csvReader.close();
        }
        return stopTimesMap;
    }

    public HashMap<String,List<Stop>> parceStops() throws CsvValidationException, IOException {
        for (short i=0; i < societes.length;i++) {
            FileReader fileReader = new FileReader("../data/GTFS/"+societes[i]+"/stops.csv");
            CSVReader csvReader = new CSVReader(fileReader);

            String[] nextRecord = csvReader.readNext();
            nextRecord = csvReader.readNext();
            while (nextRecord != null) {
                nextRecord[1] = nextRecord[1].toLowerCase(Locale.FRENCH);
                if (!stopsMap.containsKey(nextRecord[1])){
                    List<Stop> stops = new ArrayList<>();
                    stops.add(new Stop(nextRecord[0],nextRecord[1],nextRecord[2],nextRecord[3]));
                    stopsMap.put(nextRecord[1], stops);
                    
                } else {
                    stopsMap.get(nextRecord[1]).add(new Stop(nextRecord[0],nextRecord[1],nextRecord[2],nextRecord[3]));
                }
                nextRecord = csvReader.readNext();
            }
            csvReader.close();
        }
        initializeStopIdToStopMap();
        return stopsMap;
    }

    public static void bindRoutesToTrips(StopTime stopTime) throws CsvValidationException, IOException {
        String societe = stopTime.getTripId().split("-")[0];
        CSVReader routesCsvReader = new CSVReader(new FileReader("../data/GTFS/"+societe+"/routes.csv"));
        CSVReader tripsCsvReader = new CSVReader(new FileReader("../data/GTFS/"+societe+"/trips.csv"));
        
        String[] nextTripsRecord;
        String[] nextRoutesRecord;

        do {
            nextTripsRecord = tripsCsvReader.readNext();
        }
        while (nextTripsRecord != null && !nextTripsRecord[0].equals(stopTime.getTripId()));
        
        
        if (nextTripsRecord != null){
            do {
                nextRoutesRecord = routesCsvReader.readNext();
            }
            while (nextRoutesRecord != null && !nextRoutesRecord[0].equals(nextTripsRecord[1]));

            if (nextRoutesRecord != null) {
                stopTime.setRouteName(nextRoutesRecord[1]);
                stopTime.setRouteType(nextRoutesRecord[3]);
            }
        }
        tripsCsvReader.close();
        routesCsvReader.close();
    }

    private void initializeStopIdToStopMap() {
        stopIdToStopMap = new HashMap<>();
        for (List<Stop> stops : stopsMap.values()) {
            for (Stop stop : stops) {
                stopIdToStopMap.put(stop.getStopId(), stop);
            }
        }
    }

    private void bindStopNameToStopTime(StopTime stopTime) {
        Stop stop = stopIdToStopMap.get(stopTime.getStopId());
        if (stop != null) {
            stopTime.setStopName(stop.getStopName());
        }
    }

    public int getCountStops() {
        return stopsCounter;
    }
}