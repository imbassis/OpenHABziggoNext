package org.openhab.binding.ziggonext.internal.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection.Protocol;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*import paho.mqtt.client as mqtt
import logging
import requests
import json
import random
import string
from .idmaker import makeId
 */
/**
 * The {@link ZiggoClient} is responsible for handling setting up a
 * connection with the Ziggo MQTT server
 *
 * @author Rolf Vermeer - Initial contribution
 */

public class ZiggoClient {
    private static final org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection.MqttVersion V3 = null;

    private final Logger logger = LoggerFactory.getLogger(ZiggoClient.class);

    private final String API_URL_SESSION = "https://web-api-prod-obo.horizon.tv/oesp/v3/NL/nld/web/session";
    private final String API_URL_TOKEN = "https://web-api-prod-obo.horizon.tv/oesp/v3/NL/nld/web/tokens/jwt";
    private final String DEFAULT_HOST = "obomsg.prod.nl.horizon.tv";
    private final Integer DEFAULT_PORT = 443;

    private String username;
    private String password;
    private HttpClient httpClient;
    // private Object _onmessage_callback;
    private String householdId = "";
    private String jwToken = "";
    private Map<String, String> session = new HashMap<String, String>();

    public ZiggoClient(String username, String password, HttpClient httpClient) { // Object on_message_callback
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;

        logger.debug("ZiggoClient received username {} and password {}.", this.username, this.password);
        // _onmessage_callback = on_message_callback;

        retrieveSessionAndToken();

        // createMqttClient();
    }

    private void retrieveSessionAndToken() {
        this.session = getSession();
        logger.debug(this.session.get("_householdId"));
        this.householdId = this.session.get("householdId");
        String oespToken = this.session.get("oespToken");
        this.jwToken = getJwToken(oespToken);
    }

    private Map<String, String> getSession() {
        logger.debug("Try to get session...");
        ContentResponse response;

        Map<String, String> payload = new HashMap<String, String>();
        payload.put("username", this.username);
        payload.put("password", this.password);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(payload);

        try {
            response = httpClient.POST(API_URL_SESSION).content(new StringContentProvider(json), "application/json")
                    .header(HttpHeader.CONTENT_TYPE, "application/json").send();

            logger.debug("State: {}", response.getStatus());
            logger.debug("Response: {}", response.getContentAsString());

            JsonObject jsonObject = new Gson().fromJson(response.getContentAsString(), JsonObject.class);
            JsonElement jsonEl = jsonObject.get("customer");

            Map<String, String> outMap = new HashMap<String, String>();
            outMap.put("householdId", jsonObject.get("customer").getAsJsonObject().get("householdId").getAsString());
            outMap.put("oespToken", jsonObject.get("oespToken").getAsString());
            outMap.put("countryCode", jsonObject.get("customer").getAsJsonObject().get("countryCode").getAsString());
            outMap.put("deviceId", jsonObject.get("customer").getAsJsonObject().get("physicalDeviceId").getAsString());
            logger.debug("customer: {}", jsonEl.toString());

            return outMap;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, String> returnVal = new HashMap<String, String>();

        returnVal.put("customer", "RolfVermeer");

        return returnVal;
    }

    private String getJwToken(String oespToken) {
        logger.debug("Try to get JWT token with oespToken {}.", oespToken);

        try {
            httpClient.start();
            Request request = httpClient.newRequest(API_URL_TOKEN);
            request.method(HttpMethod.GET);
            request.header("X-OESP-Token", oespToken);
            request.header("X-OESP-Username", this.username);
            ContentResponse response = request.send();

            httpClient.stop();
            if (response.getStatus() == 200) {
                return new Gson().fromJson(response.getContentAsString(), JsonObject.class).get("token").getAsString();
            } else {
                logger.warn("Obtaining JWT token failed: {}, result body: {}", response.getStatus(),
                        response.getContentAsString());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    private void createMqttClient() {
        logger.debug("Creating MQTT client");
        String mqttClientId = IdMaker.makeId(30);

        MqttBrokerConnection mqttClient = new MqttBrokerConnection(Protocol.WEBSOCKETS, V3, DEFAULT_HOST, DEFAULT_PORT,
                false, mqttClientId); // MqttBrokerConnection(Protocol.WEBSOCKETS, DEFAULT_HOST, DEFAULT_PORT, false,
                                      // mqttClientId);

        MqttConnectionObserver mqttObserver = new MqttConnectionObserver() {
            @Override
            public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
                logger.debug("Mqtt state changed to {}", state);

                // mqttClient.subscribe("/#", mqttSubscriber);
            }
        };

        MqttMessageSubscriber mqttSubscriber = new MqttMessageSubscriber() {
            @Override
            public void processMessage(String topic, byte[] payload) {
                logger.debug("Received message on topic {}, data: {})", topic, payload.toString());
            }
        };

        mqttClient.setCredentials(householdId, jwToken);
        mqttClient.addConnectionObserver(mqttObserver);
        mqttClient.start();

        /*
         * _LOGGER.debug("Creating a MQTT client")
         * self.mqtt_clientId = makeId(30)
         * _LOGGER.debug("- host: " + DEFAULT_HOST)
         * _LOGGER.debug("- port: " + str(DEFAULT_PORT))
         *
         * self.__mqtt_client = mqtt.Client(
         * client_id=self.mqtt_clientId, transport="websockets"
         * )
         * self.__mqtt_client.user_data_set(
         * {"clientId": self.mqtt_clientId, "householdId": self.householdId}
         * )
         * self.__mqtt_client.username_pw_set(self.householdId, self.__jwToken)
         * self.__mqtt_client.tls_set()
         * # Register events.
         * self.__mqtt_client.on_connect = self.__on_connect
         * self.__mqtt_client.on_message = self.__on_message
         * self.__mqtt_client.on_disconnect = self.__on_disconnect
         * self.__mqtt_client.connect(DEFAULT_HOST, DEFAULT_PORT)
         * self.__mqtt_client.loop_start()
         * _LOGGER.debug("Connecting...")
         */
    }

    /*
     *
     * def __createMqttClient(self):
     *
     * def subscribe(self, topic):
     * self.__mqtt_client.subscribe(self.householdId + topic)
     *
     * def publish(self, topic, payload):
     * topic = self.householdId + topic
     * _LOGGER.debug("Publishing message:")
     * _LOGGER.debug("  topic: " + topic)
     * _LOGGER.debug("  payload: " + str(payload))
     * self.__mqtt_client.publish(topic, payload)
     *
     * def __on_connect(self, client, userdata, flags, resultCode):
     * if resultCode == 0:
     * # Connection was successful.
     * _LOGGER.debug("connected.")
     * self.isConnected = True
     * self.subscribe("/+/status")
     *
     * elif resultCode == 5:
     * # Connection refused - not authorised.
     * # This can occur if the current connection expires, so refresh the credentials and try again.
     * _LOGGER.debug("unauthorized.")
     * self.retrieveSessionAndToken()
     * self.__mqtt_client.username_pw_set(self._householdId, self._jwToken)
     * self.__mqtt_client.connect(DEFAULT_HOST, DEFAULT_PORT)
     * self.__mqtt_client.loop_start()
     * _LOGGER.debug("Connecting...")
     *
     * else:
     * _LOGGER.error("Could not establish a MQTT connection with the device.")
     *
     * def __on_message(self, client, userdata, message):
     * _LOGGER.debug("Mqtt message received:")
     * _LOGGER.debug("  topic: " + message.topic)
     * _LOGGER.debug("  payload: " + str(message.payload))
     * self.__on_message_callback(client, userdata, message)
     *
     * def __on_disconnect(self, client, userdata, resultCode):
     * _LOGGER.debug("Disconnected...")
     * self.isConnected = False
     *
     */
}
