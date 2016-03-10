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

           /* example code for inserting a route into the forwardingtable:
               BasicRoute r=new BasicRoute();
               r.destination= ...somedestination...;
               r.nextHop=...someneighbour...;
              forwardingTable.put(...somedestination... , r);
           */

           /* example code for checking whether some destination is already in the forwardingtable, and accessing it:
               if (forwardingTable.containsKey(dest)) {
                   BasicRoute r=forwardingTable.get(dest);
                   // do something with r.destination and r.nextHop; you can even modify them
               }
           */

        }

        // and send out one (or more, if you want) distance vector packets
        // the actual distance vector data must be stored in the DataTable structure
        DataTable dt = new DataTable(3);   // the 3 is the number of columns, you can change this
        // you'll probably want to put some useful information into dt here
        // by using the  dt.set(row, column, value)  method.

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
