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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.ziggonext.internal.client.ZiggoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZiggoNextHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rolf Vermeer - Initial contribution
 */

public class ZiggoNextHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ZiggoNextHandler.class);

    private ZiggoNextBindingConfiguration configuration;

    private ZiggoNextHandlerConfiguration handlerConfig = new ZiggoNextHandlerConfiguration();

    private ZiggoClient ziggoClient;
    private HttpClient httpClient;

    private HashMap<String, String> players = new HashMap<String, String>();;
    private HashMap<String, String> channels = new HashMap<String, String>();;

    public ZiggoNextHandler(Thing thing, ZiggoNextBindingConfiguration configuration, HttpClient httpClient) {
        super(thing);

        this.configuration = configuration;
        this.httpClient = httpClient;

        logger.debug("Create a Ziggo Next Handler for thing '{}'", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            // TODO: handle data refresh
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        handlerConfig = getConfigAs(ZiggoNextHandlerConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = false; // <background task with long running initialization here>
            logger.debug("Set up connection with Ziggo with {} and  {}", handlerConfig.username,
                    handlerConfig.password);
            logger.debug("Binding has settings {} and  {}", configuration.username, configuration.password);

            if ((configuration.username == "" && handlerConfig.username == "")
                    || (configuration.password == "" && handlerConfig.password == "")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Account details not complete. These can be set in the binding settings.");
                return;
            }

            this.ziggoClient = new ZiggoClient(
                    (handlerConfig.username != "") ? handlerConfig.username : configuration.username,
                    (handlerConfig.password != "") ? handlerConfig.password : configuration.password, httpClient);

            this.players = new HashMap<String, String>();
            this.channels = new HashMap<String, String>();
            // _get_channels()

            thingReachable = true;

            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParmeters) {
        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParmeters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        // reinitialize with new configuration and persist changes
        dispose();
        updateConfiguration(configuration);
        initialize();
    }
}
