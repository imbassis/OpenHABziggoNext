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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZiggoNextHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rolf Vermeer - Initial contribution
 */
// @NonNullByDefault
@Component(configurationPid = "binding.ziggonext", service = ThingHandlerFactory.class)
public class ZiggoNextHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ZiggoNextHandler.class);
    private HttpClient httpClient;

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(ZiggoNextBindingConstants.ZIGGONEXT_THING_TYPE);

    final ZiggoNextBindingConfiguration configuration = new ZiggoNextBindingConfiguration();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        // This still doesn't do what I'd hope it to do...
        configuration.update(new Configuration(config).as(ZiggoNextBindingConfiguration.class));
        logger.debug("Updated {}, {}", configuration.username, configuration.password);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Thinghandler createhandler ThingtypeUID: " + thingTypeUID.toString());

        if (ZiggoNextBindingConstants.ZIGGONEXT_THING_TYPE.equals(thingTypeUID)) {
            logger.debug("Return new ZiggoNextHandler");
            return new ZiggoNextHandler(thing, configuration, httpClient);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
