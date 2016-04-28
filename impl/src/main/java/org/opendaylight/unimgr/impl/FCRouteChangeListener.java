/*
 * Copyright (c) 2016 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.unimgr.api.UnimgrDataTreeChangeListener;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.FcRouteList;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRoute;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.fcroutelist.FcRouteKey;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.g_fcroute.FcList;
import org.opendaylight.yang.gen.v1.uri.onf.coremodel.corenetworkmodule.objectclasses.rev160413.g_fcroute.FcListKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NRP top level change model listener
 * @author bartosz.michalik@amartus.com
 */
public class FCRouteChangeListener extends UnimgrDataTreeChangeListener<FcRoute> {
    private static final Logger LOG = LoggerFactory.getLogger(FCRouteChangeListener.class);
    private final ListenerRegistration<FCRouteChangeListener> listener;

    public FCRouteChangeListener(DataBroker dataBroker) {
        super(dataBroker);
        final InstanceIdentifier<FcRoute> fwPath = getFwConstructsPath();
        final DataTreeIdentifier<FcRoute> dataTreeIid = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, fwPath);
        listener = dataBroker.registerDataTreeChangeListener(dataTreeIid, this);
        LOG.info("FCRouteChangeListener created and registered");
    }

    @Override
    public void add(DataTreeModification<FcRoute> newDataObject) {
        LOG.info("FC added");
    }

    @Override
    public void remove(DataTreeModification<FcRoute> removedDataObject) {
        LOG.info("FC removed");
    }

    @Override
    public void update(DataTreeModification<FcRoute> modifiedDataObject) {
        LOG.info("FC updated");
    }

    @Override
    public void close() throws Exception {
        listener.close();
    }

    public InstanceIdentifier<FcRoute> getFwConstructsPath() {
        final InstanceIdentifier<FcRoute> path = InstanceIdentifier
                .builder(FcRouteList.class)
                .child(FcRoute.class).build();

        return path;

    }
}
