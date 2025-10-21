import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;

public class GraphBuilder {
    private EdgeWeightedDigraph graph;
    private int lastVertex = 0;
    private HashMap<String, Integer> vertexes = new HashMap<>();
    private HashMap<Integer, HashMap<String,StopTime>> inverseVertexes = new HashMap<>();
    private int vertexArrival = 0;
    private int vertexStart = 0;
    
    public static int timeStringToSeconds(String time) {
        String[] parts = time.split(":"); 
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static double haversine(double[] coord1, double[] coord2) {
        final double R = 6371000;
        double lat1 = Math.toRadians(coord1[0]);
        double lon1 = Math.toRadians(coord1[1]);
        double lat2 = Math.toRadians(coord2[0]);
        double lon2 = Math.toRadians(coord2[1]);
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        return R * 2 * Math.asin(Math.sqrt(a));
    }

    public void addWalkingEdges(HashMap<String, List<Stop>> stopMap, double maxDistM, double walkSpeedMS) {
        List<String> stopIds = new ArrayList<>(stopMap.keySet());
        List<List<double[]>> coordsList = new ArrayList<>();
        
        for (String stopId : stopIds) {
            List<Stop> stops = stopMap.get(stopId);
            List<double[]> coords = new ArrayList<>();
            for (Stop stop : stops) {
                coords.add(new double[]{Double.parseDouble(stop.getCoordinates().getLeft()), Double.parseDouble(stop.getCoordinates().getRight())});
            }
            coordsList.add(coords);
        }

        for (int i = 0; i < stopIds.size(); i++) {
            String sid = stopIds.get(i);
            List<double[]> coords = coordsList.get(i);
            KDTree kdTree = new KDTree(coords);

            for (int j = 0; j < coords.size(); j++) {
                double[] coord = coords.get(j);
                List<Integer> idxs = kdTree.queryBallPoint(coord, maxDistM);
                for (int k : idxs) {
                    if (k == j) continue;
                    String sid2 = stopIds.get(i);
                    List<Stop> stops2 = stopMap.get(sid2);
                    Stop stop2 = stops2.get(k);

                    double dist = haversine(coord, new double[]{Double.parseDouble(stop2.getCoordinates().getLeft()), Double.parseDouble(stop2.getCoordinates().getRight())});
                    double travelTime = dist / walkSpeedMS;

                    int vertex1 = vertexes.computeIfAbsent(sid, k1 -> lastVertex++);
                    int vertex2 = vertexes.computeIfAbsent(sid2, k1 -> lastVertex++);
                    graph.addEdge(new DirectedEdge(vertex1, vertex2, travelTime));
                    graph.addEdge(new DirectedEdge(vertex2, vertex1, travelTime));
                }
            }
        }
    }

    public EdgeWeightedDigraph buildGraph(HashMap<String, List<StopTime>> stopTimesMap, HashMap<String, List<Stop>> stopMap, String start, String arrival, int departureTime) {
        graph = new EdgeWeightedDigraph(stopMap.size());
    
        if (!stopMap.containsKey(start) || stopMap.get(start).isEmpty()) {
            throw new IllegalArgumentException("Le point de départ '" + start + "' est introuvable dans stopMap.");
        }
        if (!stopMap.containsKey(arrival) || stopMap.get(arrival).isEmpty()) {
            throw new IllegalArgumentException("Le point d'arrivée '" + arrival + "' est introuvable dans stopMap.");
        }
    
        for (List<StopTime> stopTimes : stopTimesMap.values()) {
            for (int i = 0; i < stopTimes.size() - 1; i++) { 
                StopTime stopTime = stopTimes.get(i);
                StopTime stopTimeBis = stopTimes.get(i + 1);
                
                int vertex = vertexes.computeIfAbsent(stopTime.getStopName(), k -> lastVertex++);
                int vertexBis = vertexes.computeIfAbsent(stopTimeBis.getStopName(), k -> lastVertex++);
    
                inverseVertexes.putIfAbsent(vertex, new HashMap<>());
                inverseVertexes.get(vertex).put(stopTime.getTripId(), stopTime);
    
                inverseVertexes.putIfAbsent(vertexBis, new HashMap<>());
                inverseVertexes.get(vertexBis).put(stopTimeBis.getTripId(), stopTimeBis);
   
                double diffTime = stopTimeBis.getDepartureTimeInSeconds() - stopTime.getDepartureTimeInSeconds();
                
                if (stopTime.getStopName().equals(start)) {
                    int waitingTime = Math.max(0, stopTime.getDepartureTimeInSeconds() - departureTime);
                    diffTime += waitingTime;
                }
                
                graph.addEdge(new DirectedEdge(vertex, vertexBis, diffTime));
    
                if (stopTime.getStopName().equals(start)) {
                    for (Stop stop : stopMap.get(start)) {
                        if (stopTime.getStopId().equals(stop.getStopId())) vertexStart = vertex;
                    }
                }
                if (stopTimeBis.getStopName().equals(arrival)) {
                    for (Stop stop : stopMap.get(arrival)) {
                        if (stopTimeBis.getStopId().equals(stop.getStopId())) vertexArrival = vertexBis;
                    }
                }
            }
        }
        
        addWalkingEdges(stopMap, 500, 1.4);

        return graph;
    }

    public HashMap<Integer, HashMap<String,StopTime>> getVertexesMap() {
        return inverseVertexes;
    }

    public int getVertexStart() {
        return vertexStart;
    }

    public int getVertexArrival() {
        return vertexArrival;
    }

    class KDTree {
        private List<double[]> points;

        public KDTree(List<double[]> points) {
            this.points = points;
        }

        public List<Integer> queryBallPoint(double[] query, double radius) {
            List<Integer> neighbors = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                if (haversine(query, points.get(i)) <= radius) {
                    neighbors.add(i);
                }
            }
            return neighbors;
        }
    }
}
