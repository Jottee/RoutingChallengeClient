package protocol;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import client.BasicRoute;
import client.DataTable;
import client.IRoutingProtocol;
import client.LinkLayer;
import client.Packet;

public class OwnRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;
    private ConcurrentHashMap<Integer, BasicRoute> forwardingTable = new ConcurrentHashMap<Integer, BasicRoute>();
    private Set<Integer> neighbours;

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
        BasicRoute ownRoute = new BasicRoute();
        ownRoute.linkcost = 0;
        ownRoute.destination = this.linkLayer.getOwnAddress();
        ownRoute.nextHop = this.linkLayer.getOwnAddress();
        forwardingTable.put(this.linkLayer.getOwnAddress(), ownRoute);
        neighbours = new HashSet<Integer>();
    }


    @Override
    public void tick(Packet[] packets) {
        System.out.println("tick; received " + packets.length + " packets");
        int i;

        // first process the incoming packets; loop over them:
        for (i = 0; i < packets.length; i++) {
            int neighbour = packets[i].getSourceAddress();          // from whom is the packet?
            int linkcost = this.linkLayer.getLinkCost(neighbour);   // what's the link cost from/to this neighbour?
            DataTable dt = packets[i].getData();                    // other data contained in the packet
            System.out.println("received packet from " + neighbour + " with " + dt.getNRows() + " rows of data");

            
            neighbours.add(neighbour);
            
            // reading one cell from the DataTable can be done using the  dt.get(row,column)  method
            for (int q = 0; q < dt.getNRows(); q++) {
            	 if (forwardingTable.containsKey(dt.get(q, 0)) && !(dt.get(q, 2) == linkLayer.getOwnAddress())) {
            		BasicRoute r = forwardingTable.get(dt.get(q, 0));
            		if (dt.get(q, 1) + linkcost < r.linkcost) {
            			r.destination = dt.get(q, 0);
                        r.nextHop = neighbour;
                        r.linkcost = linkcost + dt.get(q, 1);
                        forwardingTable.put(dt.get(q, 0) , r);
            		}
            	} else if (!(dt.get(q, 2) == linkLayer.getOwnAddress())){
            		BasicRoute r = new BasicRoute();
                    r.destination = dt.get(q, 0);
                    r.nextHop = neighbour;
                    r.linkcost = linkcost + dt.get(q, 1);
                    forwardingTable.put(dt.get(q, 0) , r);
            	}
            }
            Iterator<Integer> iter = neighbours.iterator();
            while (iter.hasNext()) {
            	int neigh = iter.next();
            	if (linkLayer.getLinkCost(neigh) == -1) {
            		iter.remove();
            		forwardingTable.get(neigh).linkcost = Integer.MAX_VALUE;
            		for (Map.Entry<Integer, BasicRoute> ie : forwardingTable.entrySet()) {
            			if (ie.getValue().nextHop == neigh) {
            				ie.getValue().linkcost = Integer.MAX_VALUE;
            			}
            		}
            	}
            }

        }

        // and send out one (or more, if you want) distance vector packets
        // the actual distance vector data must be stored in the DataTable structure
        DataTable dt = new DataTable(3);   // the 3 is the number of columns, you can change this
        // you'll probably want to put some useful information into dt here
        // by using the  dt.set(row, column, value)  method.
        int j = 0;
        for (Integer dest : forwardingTable.keySet()) {
        	 dt.set(j, 0, dest);
        	 dt.set(j, 1, forwardingTable.get(dest).linkcost);
        	 dt.set(j, 2, forwardingTable.get(dest).nextHop);
        	 j++;
        }
       

        // next, actually send out the packet, with our own address as the source address
        // and 0 as the destination address: that's a broadcast to be received by all neighbours.
        Packet pkt = new Packet(this.linkLayer.getOwnAddress(), 0, dt);
        this.linkLayer.transmit(pkt);
    }
    


    @Override
    public ConcurrentHashMap<Integer, BasicRoute> getForwardingTable() {
        return this.forwardingTable;
    }
}
