/*
 * Copyright (c) 2017 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.mef.nrp.cisco.xr.l2vpn.helper;

import org.opendaylight.unimgr.mef.nrp.api.TapiConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NetworkKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.NodeId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.Node;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev150608.network.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


/**
 * @author bartosz.michalik@amartus.com
 */
public class TapiHelper {
    public static Node node(String id) {
        NodeId nId = new NodeId(id);
        return new NodeBuilder()
                .setNodeId(nId)
                .setKey(new NodeKey(nId))
            .build();
    }

    public static InstanceIdentifier<Node> tapiNodeId(String id) {
        return InstanceIdentifier.builder(Network.class, new NetworkKey(new NetworkId(TapiConstants.PRESTO_NETWORK_ID)))
                .child(Node.class, new NodeKey(new NodeId(id))).build();
    }
}
