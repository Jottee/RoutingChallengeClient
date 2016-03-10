package protocol;

import client.*;

import java.util.concurrent.ConcurrentHashMap;

public class OwnRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;
    private ConcurrentHashMap<Integer, BasicRoute> forwardingTable = new ConcurrentHashMap<Integer, BasicRoute>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
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

            // you'll probably want to process the data, update the forwarding table, etc....

            // reading one cell from the DataTable can be done using the  dt.get(row,column)  method
            for (int q = 0; q < dt.getNRows(); q++) {
            	if (forwardingTable.containsKey(dt.get(q, 0))) {
            		BasicRoute r = forwardingTable.get(dt.get(q, 0));
            		if (dt.get(q, 1) + linkcost < r.linkcost) {
            			BasicRoute newR = new BasicRoute();
                        r.destination = dt.get(q, 0);
                        r.nextHop = neighbour;
                        r.linkcost = linkcost + dt.get(q, 1);
                        forwardingTable.put(dt.get(q, 0) , newR);
            		}
            		
            	} else {
            		BasicRoute r = new BasicRoute();
                    r.destination = dt.get(q, 0);
                    r.nextHop = neighbour;
                    r.linkcost = linkcost + dt.get(q, 1);
                    forwardingTable.put(neighbour , r);
            	}
            }

        }

        // and send out one (or more, if you want) distance vector packets
        // the actual distance vector data must be stored in the DataTable structure
        DataTable dt = new DataTable(2);   // the 3 is the number of columns, you can change this
        // you'll probably want to put some useful information into dt here
        // by using the  dt.set(row, column, value)  method.
        int j = 1;
        dt.set(0, 0, linkLayer.getOwnAddress());
        dt.set(0, 1, 0);
        for (Integer dest : forwardingTable.keySet()) {
        	 dt.set(j, 0, dest);
        	 dt.set(j, 1, forwardingTable.get(dest).linkcost);
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
