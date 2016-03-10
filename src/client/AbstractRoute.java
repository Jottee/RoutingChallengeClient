package client;

/**
 * Object which describes a route entry in the forwarding table.
 * Can be extended to include additional data.
 * @author Jaco ter Braak & Frans van Dijk, University of Twente.
 * @version 09-03-2016
 */
public class AbstractRoute {
    public int destination;
    public int nextHop;
}
