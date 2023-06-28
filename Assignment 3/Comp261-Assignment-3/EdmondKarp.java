
import java.lang.reflect.Array;
import java.util.*;

import javafx.util.Pair;

/** Edmond karp algorithm to find augmentation paths and network flow.
 * 
 * This would include building the supporting data structures:
 * 
 * a) Building the residual graph(that includes original and backward (reverse) edges.)
 *     - maintain a map of Edges where for every edge in the original graph we add a reverse edge in the residual graph.
 *     - The map of edges are set to include original edges at even indices and reverse edges at odd indices (this helps accessing the corresponding backward edge easily)
 *     
 *     
 * b) Using this residual graph, for each city maintain a list of edges out of the city (this helps accessing the neighbours of a node (both original and reverse))

 * The class finds : augmentation paths, their corresponing flows and the total flow
 * 
 * 
 */

public class EdmondKarp {
    // class members

    //data structure to maintain a list of forward and reverse edges - forward edges stored at even indices and reverse edges stored at odd indices
    private static Map<String, Edge> edges;

    // Augmentation path and the corresponding flow
    private static ArrayList<Pair<ArrayList<String>, Integer>> augmentationPaths = null;


    /**
     * Building the residual graph(that includes original and backward (reverse) edges.)
     * - maintain a map of Edges where for every edge in the original graph we add a reverse edge in the residual graph.
     * - The map of edges are set to include original edges at even indices and reverse edges at odd indices (this helps accessing the corresponding backward edge easily)
     */
    public static void computeResidualGraph(Graph graph) {
        //get the original edges and hold them at even indices and reverse edges at odd indices
        int index = 0;
        edges = new HashMap<String, Edge>();
        for (Edge current : graph.getOriginalEdges()) {         //for each edge in the original graph
            edges.put(String.valueOf(index), current);      //add the edge to our map
            current.fromCity().addEdgeId(String.valueOf(index));    //add the edge id to the from city
            index++;                                    //increment the index
            //add the reverse edge;
            Edge reverse = new Edge(current.toCity(), current.fromCity(), current.transpType(), 0, 0);  //create a reverse edge
            edges.put(String.valueOf(index), reverse);      //add the reverse edge to our map
            reverse.fromCity().addEdgeId(String.valueOf(index));
            index++;
        }
        //printResidualGraphData(graph);  //may help in debugging
    }

    /**
     * Update the residual graph
     *
     * @param augmentationPath
     */
    public static void updateResidualGraph(Pair<ArrayList<String>, Integer> augmentationPath) {
        //for each edge in the augmentation path update the flow and capacity
        for (String eId : augmentationPath.getKey()) {          //for each edge in the augmentation path
            Edge edge = edges.get(eId);
            edge.setFlow(edge.flow() + augmentationPath.getValue());        //increase flow in the forward edge
            edge.setCapacity(edge.capacity() - augmentationPath.getValue());    //decrease capacity in the forward edge
            //increase capacity in the reverse edge
            Edge reverse = edges.get(String.valueOf(Integer.parseInt(eId) + 1));
            reverse.setCapacity(reverse.capacity() + augmentationPath.getValue());
        }
    }

    // Method to print Residual Graph 
    public static void printResidualGraphData(Graph graph) {
        System.out.println("\nResidual Graph");
        System.out.println("\n=============================\nCities:");
        for (City city : graph.getCities().values()) {
            System.out.print(city.toString());

            // for each city display the out edges 
            for (String eId : city.getEdgeIds()) {
                System.out.print("[" + eId + "] ");
            }
            System.out.println();
        }
        System.out.println("\n=============================\nEdges(Original(with even Id) and Reverse(with odd Id):");
        edges.forEach((eId, edge) ->
                System.out.println("[" + eId + "] " + edge.toString()));

        System.out.println("===============");
    }

    //=============================================================================
    //  Methods to access data from the graph. 
    //=============================================================================

    /**
     * Return the corresonding edge for a given key
     */

    public static Edge getEdge(String id) {
        return edges.get(id);
    }

    /**
     * find maximum flow
     */
    public static ArrayList<Pair<ArrayList<String>, Integer>> calcMaxflows(Graph graph, City from, City to) {
        computeResidualGraph(graph);
        augmentationPaths = new ArrayList<Pair<ArrayList<String>, Integer>>();
        for (Edge e : graph.getOriginalEdges()) {
            e.setFlow(0);                       //initialize flow to 0
        }
        Pair<ArrayList<String>, Integer> path = bfs(graph, from, to);   //find an augmentation path
        while (path != null) {              //while there is an augmentation path
            augmentationPaths.add(path);
            updateResidualGraph(path);      //update the residual graph
            path = bfs(graph, from, to);
        }
        return augmentationPaths;
    }

    //Use BFS to find a path from s to t along with the correponding bottleneck flow
    public static Pair<ArrayList<String>, Integer> bfs(Graph graph, City s, City t) {
        //null check
        if (s == null || t == null) {
            System.out.println("\nInvalid input");
            return null;
        }
        ArrayList<String> augmentationPath = new ArrayList<String>();
        HashMap<String, String> backPointer = new HashMap<String, String>(); //e.g. <b,a> means b is reached from a
        Queue<City> queue = new LinkedList<City>();
        queue.add(s);
        //initialize backPointer map
        for (City city : graph.getCities().values()) {
            backPointer.put(city.getId(), null);
        }
        while (!queue.isEmpty()) {
            City current = queue.poll();
            for (String eId : current.getEdgeIds()) {
                Edge edge = getEdge(eId);
                if (edge.capacity() > 0 && backPointer.get(edge.toCity().getId()) == null) {        //if the edge is not saturated and the city has not been visited
                    backPointer.put(edge.toCity().getId(), edge.fromCity().getId());
                    queue.add(edge.toCity());
                    if (edge.toCity().equals(t)) {
                        //found a path
                        String cityId = t.getId();      //start from t and backtrack to s
                        while (!cityId.equals(s.getId())) { //stop when we reach s
                            String fromCityId = backPointer.get(cityId);    //get the cityId of the city that leads to cityId
                            for (String eId2 : graph.getCity(fromCityId).getEdgeIds()) {    //find the edge that connects the two cities
                                Edge edge2 = getEdge(eId2); //get the edge
                                if (edge2.toCity().getId().equals(cityId)) {    //check if the edge is the one we are looking for
                                    augmentationPath.add(eId2); //add the edge to the path
                                    break;  //break out of the for loop
                                }
                            }
                            cityId = fromCityId;    //move to the next city
                        }
                        Collections.reverse(augmentationPath);
                        return new Pair<ArrayList<String>, Integer>(augmentationPath, getBottleNeck(augmentationPath));
                    }
                }
            }
        }
        return null;
    }


    public static int getBottleNeck(ArrayList<String> augmentationPaths){       //get the bottleneck flow
        int min = Integer.MAX_VALUE;
        for(String eId : augmentationPaths){
            Edge edge = getEdge(eId);
            if(edge.capacity() < min){
                min = edge.capacity();
            }
        }
        return min;
    }


}




