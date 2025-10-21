import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.opencsv.exceptions.CsvValidationException;

import edu.princeton.cs.algs4.DijkstraSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;

public class AppRunner {
    
    private GraphBuilder graphBuilder;
    private ParceCSV parcer;
    HashMap<String, List<Stop>> stopMap;
    HashMap<String, List<StopTime>> stopTimesMap;


    public AppRunner(){
        graphBuilder = new GraphBuilder();
    }

    private String[] handleUserRequest(String[] listStrings) {
        for (int i = 0 ; i < listStrings.length ; i++) {
            listStrings[i] = listStrings[i].toLowerCase(Locale.FRENCH);
        }
        return listStrings;
    }

    private String secToTime(double timeInSec) {
        String time = "";
        time += (int)timeInSec/3600+"h"+(int)timeInSec%3600/60+"m"+(int)timeInSec%3600%60+"s";
        return time;
    }

    private void parce(String departureTime) throws CsvValidationException, IOException {

        parcer = new ParceCSV(departureTime);

        stopMap = parcer.parceStops();
        stopTimesMap = parcer.parceStopTimes();

    }

    private void handleDijkstraPath(DijkstraSP dijkstraSP, String departureTime) throws CsvValidationException, IOException {
        HashMap<Integer, HashMap<String,StopTime>> vertexes = graphBuilder.getVertexesMap();
        String lastTripId = "";
        int lastArrivalTime = 0;
        String retTripId = "";
 
        for (DirectedEdge directedEdge : dijkstraSP.pathTo(graphBuilder.getVertexArrival())) {
            int from = directedEdge.from();
            int to = directedEdge.to();
            boolean notFound = true;

            for (String tripId : vertexes.get(from).keySet()) {

                if (lastTripId.equals("")) {
                    int minWaitingTime = Integer.MAX_VALUE;
                    for (String tripIdBis : vertexes.get(to).keySet()) {
                        if (vertexes.get(from).containsKey(tripIdBis) && vertexes.get(to).get(tripIdBis).getStopSequence()-vertexes.get(from).get(tripIdBis).getStopSequence() == 1) {
                            int waitingTime = Math.max(0,vertexes.get(from).get(tripIdBis).getDepartureTimeInSeconds() - GraphBuilder.timeStringToSeconds(departureTime));
                            if (waitingTime<minWaitingTime) {
                                minWaitingTime = waitingTime;
                                lastTripId = tripIdBis;
                            }
                        }
                    }
                }
                if (tripId.equals(lastTripId) && vertexes.get(to).containsKey(tripId) && vertexes.get(to).get(tripId).getStopSequence()-vertexes.get(from).get(tripId).getStopSequence() == 1) {
                    ParceCSV.bindRoutesToTrips(vertexes.get(from).get(tripId));
                    ParceCSV.bindRoutesToTrips(vertexes.get(to).get(tripId));
                    retTripId = tripId;
                    notFound = false;
                    break;
                } 
            }
            
            if (notFound) {
                int minWaitingTime = Integer.MAX_VALUE;
                for (String tripId : vertexes.get(from).keySet()){
                    
                    if (!vertexes.get(to).containsKey(tripId)) continue;
                 
                    int waitingTime = vertexes.get(from).get(tripId).getDepartureTimeInSeconds()-lastArrivalTime;
                    
                    if (waitingTime<minWaitingTime && waitingTime>=0 && vertexes.get(to).get(tripId).getStopSequence() - vertexes.get(from).get(tripId).getStopSequence()==1) {
                        minWaitingTime = waitingTime;
                        retTripId = tripId;
                    }
                }
                lastTripId = retTripId;
                if (vertexes.get(from).get(retTripId) != null && vertexes.get(to).get(retTripId) != null) {
                    ParceCSV.bindRoutesToTrips(vertexes.get(from).get(retTripId));
                    ParceCSV.bindRoutesToTrips(vertexes.get(to).get(retTripId));
                }
            }
            
            if (vertexes.get(from).get(retTripId) != null && vertexes.get(to).get(retTripId) != null) {
                lastArrivalTime = vertexes.get(to).get(retTripId).getDepartureTimeInSeconds();
                
                System.out.println("Take " + vertexes.get(from).get(retTripId).getRouteType() + " " 
                + vertexes.get(from).get(retTripId).getRouteName() + " from " 
                + vertexes.get(from).get(retTripId).getStopName() + " (" + vertexes.get(from).get(retTripId).getDepartureTime() 
                + ")" + " to " + vertexes.get(to).get(retTripId).getStopName() + " (" 
                + vertexes.get(to).get(retTripId).getDepartureTime() + ")");
            } else {
                lastArrivalTime += directedEdge.weight();
                System.out.println("Walk for " + secToTime(directedEdge.weight()) + " from " + vertexes.get(from).get(vertexes.get(from).keySet().toArray()[0]).getStopName() + " to " + vertexes.get(to).get(vertexes.get(to).keySet().toArray()[0]).getStopName());
            }
        }
    }

    public void run(String[] args) throws CsvValidationException, IOException {
        args = handleUserRequest(args);

        long startTime = System.nanoTime();

        parce(args[2]);

        EdgeWeightedDigraph graph = graphBuilder.buildGraph(stopTimesMap, stopMap, args[0], args[1], GraphBuilder.timeStringToSeconds(args[2]));

        DijkstraSP dijkstraSP = new DijkstraSP(graph, graphBuilder.getVertexStart());
        
        handleDijkstraPath(dijkstraSP, args[2]);

        long stopTime = System.nanoTime();
        System.out.println("#######################################");
        System.out.println("Execution time: " + (stopTime - startTime)/Math.pow(10, 9) + " sec");
    }
}
