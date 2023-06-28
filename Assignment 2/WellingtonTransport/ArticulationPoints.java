import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

//=============================================================================
//   TODO   Finding Articulation Points
//   Finds all the articulation points in the undirected graph, without walking edges
//   Labels each stop with the number of the subgraph it is in
//   sets the subGraphCount of the graph to the number of subgraphs.
//=============================================================================

public class ArticulationPoints {

    // Based on....

    // Returns the collection of nodes that are articulation points
    // in the UNDIRECTED graph with no walking edges.
    //

    /**
     * Finds all the articulation points in the undirected graph, without walking edges
     * @param graph
     * @return articulationPoints
     */
    public static Collection<Stop> findArticulationPoints(Graph graph) {
        System.out.println("calling findArticulationPoints");
        graph.computeNeighbours();   // To ensure that all stops have a set of (undirected) neighbour stops

        HashSet<Stop> articulationPoints = new HashSet<Stop>();
        Map<Stop, Integer> depthMap = new HashMap<>();  //map of stop to depth
        for (Stop stop : graph.getStops()) {
            depthMap.put(stop, -1);                     //initialise all depths to -1 meaning they have not been visited
        }
        //Stop start = graph.getStops().iterator().next();  get the first stop in the graph
        for (Stop stop : graph.getStops()) {
            if (depthMap.get(stop) == -1) {         //Instead of starting at the first stop, we run the algorithm on every unvisited stop.
                depthMap.put(stop, 0);
                int numChildren = 0;                //Because we are running the algorithm on every unvisited stop, we need to keep track of the number of children
                for (Stop neighbour : stop.getNeighbours()) {
                    if (depthMap.get(neighbour) == -1) {    //if the neighbour has not been visited, then we increment the number of children and run the dfs algorithm on the neighbour
                        numChildren++;
                        dfs(neighbour, stop, 1, depthMap, articulationPoints);
                    }
                }
                if (numChildren > 1) {  //if the number of children is greater than 1, then it is an articulation point
                    articulationPoints.add(stop);
                }
            }
        }
        return articulationPoints;
    }

    private static int dfs(Stop stop, Stop fromStop, int depth, Map<Stop, Integer> depthMap, Set<Stop> articulationPoints) {
        depthMap.put(stop, depth);              //put the depth of the stop into the map
        int reachBack = depth;
        for (Stop neighbour : stop.getNeighbours()) {
            if (neighbour == fromStop) {
                continue;
            } else if (depthMap.get(neighbour) != -1) {                         //if the neighbour has been visited, then we update the reachBack value
                reachBack = Math.min(reachBack, depthMap.get(neighbour));       //the reachBack value is the minimum of the current reachBack value and the depth of the neighbour because we want to find the lowest depth
            } else {
                int childReachBack = dfs(neighbour, stop, depth + 1, depthMap, articulationPoints); //if the neighbour has not been visited, then we run the dfs algorithm on the neighbour
                if (childReachBack >= depth) {  //if the childReachBack value is greater than or equal to the depth, then it is an articulation point
                    articulationPoints.add(stop);
                }
                reachBack = Math.min(reachBack, childReachBack);    //the reachBack value is now the minimum of the current reachBack value and the childReachBack value
            }
        }
        return reachBack;
    }
}


