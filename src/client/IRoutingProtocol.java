package client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes the interface used for routing protocols
 * 
 * @author Jaco ter Braak & Frans van Dijk, University of Twente.
 * @version 06-03-2016
 */
/*
 * 
 * DO NOT EDIT
 */
public interface IRoutingProtocol {

    /**
     * This method is called by the framework before the simulation starts. The
     * protocol implementation should be initialized here.
     * 
     * @param linkLayer
     */
    void init(LinkLayer linkLayer);

    /**
     * This method is called by the framework each 'tick'.
     *
     * @param packets
     */
    void tick(Packet[] packets);

    /**
     * The method is called by the framework  during and after the simulation, to
     * retrieve the local forwarding table.
     * 
     * @return ForwardingTable
     */
    ConcurrentHashMap<Integer, ? extends AbstractRoute> getForwardingTable();
}
