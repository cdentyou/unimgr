/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.unimgr.utils;

import org.apache.sshd.common.Closeable;
import org.apache.sshd.common.future.CloseFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

/**
 * @author bartosz.michalik@amartus.com
 */
public class Dao implements Closeable {



    enum TxType {R, W, RW}

    private LogicalDatastoreType store;
    private TxType txType;

//    public class

    @Override
    public CloseFuture close(boolean b) {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isClosing() {
        return false;
    }
}
