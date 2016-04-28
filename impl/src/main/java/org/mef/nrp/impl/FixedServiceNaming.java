package org.mef.nrp.impl;

public class FixedServiceNaming implements ServiceNaming {

    @Override
    public String getOuterName(String id) {
        return "GEN15-" + id;
    }

    @Override
    public String getInnerName(String id) {
        return "GEN15-p2p-" + id;
    }

}
