/**
 * AStar search (and Dijkstra search) uses a priority queue of partial paths
 * that the search is building.
 * Each partial path needs several pieces of information, to specify
 * the path to that point, its cost so far, and its estimated total cost
 */

public class PathItem implements Comparable<PathItem> {
    private final Stop stop;
    private final Edge edge;
    private final double lengthToNode;
    private final double estimate;

    public PathItem(Stop stop, Edge edge, double lengthToNode, double estimate) {
        this.stop = stop;
        this.edge = edge;
        this.lengthToNode = lengthToNode;
        this.estimate = estimate;
    }

    public Stop getStop() {
        return stop;
    }

    public Edge getEdge() {
        return edge;
    }

    public double getLength() {
        return lengthToNode;
    }

    /**
     * Compare this PathItem to another PathItem. The comparison is based on the total cost
     * @param other the object to be compared.
     * @return
     */
    @Override
    public int compareTo(PathItem other) {
        double thisTotalCost = this.lengthToNode + this.estimate;       // total cost of this path
        double otherTotalCost = other.lengthToNode + other.estimate;    // total cost of other path

        if (thisTotalCost < otherTotalCost) {
            return -1;
        } else if (thisTotalCost > otherTotalCost) {
            return 1;
        } else {
            return 0;
        }
    }
}
