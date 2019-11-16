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

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ZiggoNextBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rolf Vermeer - Initial contribution
 */

public class ZiggoNextBindingConstants {

    private static final String BINDING_ID = "ziggonext";

    // List of all Thing Type UIDs
    public static final ThingTypeUID ZIGGONEXT_THING_TYPE = new ThingTypeUID(BINDING_ID, "nextbox");

    // List of all Channel ids
    public static final String POWER_STATUS = "powerStatus";
    public static final String POWER = "power";
    public static final String SOURCE_NAME = "sourceName";
    public static final String KEY_CODE = "keyCode";

    // List of all (ThingType) Parameters
    public static final String PARAMETER_USERNAME = "username";
    public static final String PARAMETER_PASSWORD = "password";

    // List of all Properties
    public static final String PROPERTY_HOUSEHOLD_ID = "householdId";
    public static final String PROPERTY_DEVICE_ID = "deviceId";
}
