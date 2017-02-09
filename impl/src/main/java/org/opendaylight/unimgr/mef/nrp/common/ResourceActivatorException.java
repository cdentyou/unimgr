/*
 * Copyright (c) 2016 Cisco Systems Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.unimgr.mef.nrp.common;

public class ResourceActivatorException extends Exception{

	private static final long serialVersionUID = 4242336212297338778L;

	public ResourceActivatorException() {
		super();
	}

	public ResourceActivatorException(String message) {
		super(message);
	}

	public ResourceActivatorException(String message, Throwable cause) {
		super(message, cause);
	}


}
