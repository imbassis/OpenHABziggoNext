/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ziggonext.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ZiggoNextBindingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Rolf Vermeer - Initial contribution
 */
@NonNullByDefault
public class ZiggoNextBindingConfiguration {
    public String username = "";
    public String password = "";
	
	public void update(ZiggoNextBindingConfiguration newConfiguration){
		this.username = newConfiguration.username;
		this.password = newConfiguration.password;
	}
}
