package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GForwardingConstruct;

/**
 * Useful for scenarios where {@link GForwardingConstruct} (de)activaiton can be completed in single shoot
 * @author bartosz.michalik@amartus.com
 */
public class DummyActivationDriver implements ActivationDriver {
    @Override
    public void commit() {}

    @Override
    public void rollback() {}

    @Override
    public void initialize(GFcPort from, GFcPort to, GForwardingConstruct context) throws Exception {}

    @Override
    public void activate() throws Exception {}

    @Override
    public void deactivate() throws Exception {}

    @Override
    public int priority() {return Integer.MIN_VALUE;}
}
