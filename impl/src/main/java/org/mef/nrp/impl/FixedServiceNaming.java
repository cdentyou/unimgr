package org.mef.nrp.impl;

public class FixedServiceNaming implements ServiceNaming {

    @Override
    public String getOuterName(String id) {
        return "EUR16-" + id;
    }

    @Override
    public String getInnerName(String id) {
        return "EUR16-p2p-" + id;
    }

}
