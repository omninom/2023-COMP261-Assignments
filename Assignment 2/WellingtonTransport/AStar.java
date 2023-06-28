/**
 * Implements the A* search algorithm to find the shortest path
 * in a graph between a start node and a goal node.
 * It returns a Path consisting of a list of Edges that will
 * connect the start node to the goal node.
 */

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;


public class AStar {


    private static String timeOrDistance = "distance";    // way of calculating cost: "time" or "distance"


    /**
     * A Star search algorithm to find the shortest path between two stops
     * @param start
     * @param goal
     * @param timeOrDistance
     * @return
     */
    public static List<Edge> findShortestPath(Stop start, Stop goal, String timeOrDistance) {
        if (start == null || goal == null) {return null;}
        AStar.timeOrDistance= (timeOrDistance.equals("time"))?"time":"distance";   //FIXED: needed to add "Astar." before timeOrDistance to access the static variable
        PriorityQueue<PathItem> fringe = new PriorityQueue<>();     // fringe is a priority queue of PathItems
        HashSet<Stop> visited = new HashSet<>();
        Map<Stop, Edge> backpointers = new HashMap<>();             // backpointers is a map from a stop to the edge that leads to it
        fringe.add(new PathItem(start, null, 0, heuristic(start, goal)));
        while (!fringe.isEmpty()) {
            PathItem current = fringe.poll();
            if (!visited.contains(current.getStop())){
                visited.add(current.getStop());
                backpointers.put(current.getStop(), current.getEdge());     // add current stop to backpointers
                if (current.getStop().equals(goal)) {
                    return ReconstructPath(start,goal,backpointers);        //if the current stop is the goal, reconstruct the path
                }
                for (Edge edge : current.getStop().getForwardEdges()) {     // for each edge from current stop
                    if (!visited.contains(edge.toStop())) {
                        double lengthToNeigbour = current.getLength() + edgeCost(edge); // length of path to neighbour
                        double heuristic = heuristic(edge.toStop(), goal);  // heuristic estimate of cost to goal
                        fringe.add(new PathItem(edge.toStop(), edge, lengthToNeigbour, heuristic));     // add neighbour to fringe
                    }
                }
            }
        }
        return null;
    }

    /** Reconstruct the path from start to goal using the backpointers */
    public static List<Edge> ReconstructPath(Stop start, Stop goal, Map<Stop, Edge> backpointers){
        List<Edge> path = new ArrayList<Edge>();
        Stop current = goal;
        while (!current.equals(start)){             // while the current stop is not the start stop
            path.add(backpointers.get(current));        // add the edge from the current stop to the path
            current = backpointers.get(current).fromStop(); // set the current stop to the stop at the other end of the edge, travelling in reverse
        }
        Collections.reverse(path);      //finally reverse the path so that it is in the correct order
        return path;
    }




    /** Return the heuristic estimate of the cost to get from a stop to the goal */
    public static double heuristic(Stop current, Stop goal) {
        if (timeOrDistance=="distance"){ return current.distanceTo(goal);}
        else if (timeOrDistance=="time"){return current.distanceTo(goal) / Transport.TRAIN_SPEED_MPS;}
        else {return 0;}
    }

    /** Return the cost of traversing an edge in the graph */
    public static double edgeCost(Edge edge){
        if (timeOrDistance=="distance"){ return edge.distance();}
        else if (timeOrDistance=="time"){return edge.time();}
        else {return 1;}
    }




}
