package org.mef.nrp.impl;

import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.GFcPort;

public interface ResourceActivator {

    public void activate(String nodeName, String outerName, String innerName, GFcPort flowPoint, GFcPort neighbor, long mtu);

    public void deactivate(String nodeName, String outerName, String innerName, GFcPort flowPoint, GFcPort neighbor, long mtu);
}
