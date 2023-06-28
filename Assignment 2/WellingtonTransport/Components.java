import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

//=============================================================================
//   TODO   Finding Components
//   Finds all the strongly connected subgraphs in the graph
//   Labels each stop with the number of the subgraph it is in
//   sets the subGraphCount of the graph to the number of subgraphs.
//   Uses Kosaraju's_algorithm   (see lecture slides, based on
//   https://en.wikipedia.org/wiki/Kosaraju%27s_algorithm)
//=============================================================================

public class Components{

    // Use a visited set to record which stops have been visited
    // If using Kosaraju's, us a Map<Stop,Stop> to record the root node of each stop.

    
    public static void findComponents(Graph graph) {
        System.out.println("calling findComponents");
        graph.resetSubGraphIds();
        for (Stop stop : graph.getStops()) {
            stop.setSubGraphId(-1);                         // set all subgraph ids to -1
        }
        int componentNum = 0;
        List<Stop> stopList = new ArrayList<Stop>();
        HashSet<Stop> visited = new HashSet<Stop>();
        for (Stop stop : graph.getStops()) {
            if (!visited.contains(stop)) {
                forwardVisit(stop, stopList, visited);      //if the stop has not been visited, visit it forward
            }
        }
        for (int i = stopList.size() - 1; i >= 0; i--) {    //visit the stops backward
            Stop stop = stopList.get(i);
            if (stop.getSubGraphId() == -1) {
                backwardVisit(stop, componentNum);          //if the stop has not been visited, visit it backward
                componentNum++;                             //increase the component number
            }
        }
        graph.setSubGraphCount(componentNum);               //set the subgraph count to the number of components
    }

    public static void forwardVisit(Stop stop, List<Stop> stopList, HashSet<Stop> visited) {
        visited.add(stop);
        for (Edge edge : stop.getForwardEdges()) {              //visit all the forward edges
            Stop nextStop = edge.toStop();                      //neighbour stop
            if (!visited.contains(nextStop)) {
                forwardVisit(nextStop, stopList, visited);      //recursively visit the neighbour stop
            }
        }
        stopList.add(stop);
    }

    public static void backwardVisit(Stop stop, int componentNum) {
        stop.setSubGraphId(componentNum);                       //set the subgraph id to the component number
        for (Edge edge : stop.getBackwardEdges()) {
            Stop nextStop = edge.fromStop();                    //neighbour stop
            if (nextStop.getSubGraphId() == -1) {
                backwardVisit(nextStop, componentNum);          //recursively visit the neighbour stop
            }
        }
    }


}
