import javafx.util.Pair;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Write a description of class PageRank here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class PageRank
{
    //class members 
    private static double dampingFactor = .85;
    private static int iter = 10;
    /**
     * build the fromLinks and toLinks 
     */
    public static void computeLinks(Graph graph){
        for (Edge e : graph.getOriginalEdges()){
            City fromCity = e.fromCity();
            City toCity = e.toCity();
            fromCity.addToLinks(toCity);
            toCity.addFromLinks(fromCity);
        }

        //printPageRankGraphData(graph);  ////may help in debugging
    }

    public static void printPageRankGraphData(Graph graph){
        System.out.println("\nPage Rank Graph");
        for (City city : graph.getCities().values()){
            System.out.print("\nCity: "+city.toString());
            //for each city display the in edges 
            System.out.print("\nIn links to cities:");
            for(City c:city.getFromLinks()){

                System.out.print("["+c.getId()+"] ");
            }

            System.out.print("\nOut links to cities:");
            //for each city display the out edges 
            for(City c: city.getToLinks()){
                System.out.print("["+c.getId()+"] ");
            }
            System.out.println();;

        }    
        System.out.println("=================");
    }
    public static void computePageRank(Graph graph){
        Map<String, Double> pageRank = new HashMap<String, Double>();   //map to store the page rank of each city
        int nNodes = graph.getCities().size();              //number of nodes in the graph
        for (City city : graph.getCities().values()){
            pageRank.put(city.getId(), 1.0/nNodes);         //initial page rank of each city is 1/nNodes
        }
        int count = 1;
        while (count <= iter){
            Map<String, Double> newPageRank = new HashMap<String, Double>();        //map to store the new page rank of each city
            for (City city : graph.getCities().values()){
                double nRank = 0;                   //new rank of the city
                double neighbourShare = 0;          //share of the neighbour, a ratio of the rank of the neighbour and the number of out links of the neighbour
                for (City fromCity : city.getFromLinks()){
                    neighbourShare = pageRank.get(fromCity.getId())/fromCity.getToLinks().size();   //calculate the share of the neighbour, the fraction of the algorithm
                    nRank += neighbourShare;        //summation of the share of the neighbours
                }
                nRank = (1-dampingFactor)/nNodes+ dampingFactor*nRank;  //calculate the new rank of the city
                newPageRank.put(city.getId(), nRank);       //we store the new rank of the city in this map because we need to update the page rank of all the cities simultaneously
            }
            pageRank = newPageRank;            //update the page rank
            displayPageRank(pageRank, count, graph);
            count++;    //increment the count for the next iteration
        }
    }



    /**
     * Display the page rank of all the cities in the graph by iteration
     */
    public static void displayPageRank(Map<String, Double> pageRank, int iter, Graph graph){
        double sum = 0;
        System.out.println("\nIteration "+iter);
        for (String cityId : pageRank.keySet()) {
            City city = graph.getCity(cityId);
            System.out.println("City: " + city + " Rank: " + pageRank.get(cityId));
            sum += pageRank.get(cityId);
        }
        System.out.println("Sum: "+sum);
        System.out.println("=================");
        System.out.println("CHALLENGE: Most influential page for each city");
        for (String cityId : pageRank.keySet()) {       //for all cities we need to find the most influential page
            City city = graph.getCity(cityId);
            double max = 0;
            City maxCity = null;
            for (City fromCity : city.getFromLinks()){
                if (pageRank.get(fromCity.getId()) > max){      //if the page rank of the city is greater than the max, then it is the most influential page
                    max = pageRank.get(fromCity.getId());       //set the max to the page rank of the city
                    maxCity = fromCity;                         //set the max city to the city
                }
            }
            if (maxCity == null){
                System.out.println(city+ " has no incoming links");
            }
            else {
                System.out.println("Most influential page for " +city+ " is " + maxCity);
            }
        }
    }
}
