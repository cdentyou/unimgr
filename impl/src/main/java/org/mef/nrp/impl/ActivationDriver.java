package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

/**
 * Used to state change of the underlying infrastructure
 * @author bartosz.michalik@amartus.com
 */
public interface ActivationDriver {

    /**
     * Called in case all drivers in the transaction has succeeded
     */
    void commit();

    /**
     * Called in case any of drivers in the transaction has failed
     */
    void rollback();

    /**
     * Set state for the driver
     * @param from near end
     * @param to far end
     * @param context context
     * @throws Exception
     */
    void initialize(GFcPort from, GFcPort to, GForwardingConstruct context) throws Exception;

    /**
     * Activates the port from
     */
    void activate() throws Exception;
    /**
     * Deactivates the port from
     */
    void deactivate() throws Exception;


    /**
     * Influences the order in which drivers are called within the transaction
     * @return
     */
    int priority();



}
