package com.uniquid.userclient.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.messages.UniquidMessage;
import com.uniquid.messages.serializers.JSONMessageSerializer;
import com.uniquid.userclient.UserClient;
import com.uniquid.userclient.UserClientException;


/**
 * Implementation of {@link UserClient} that uses MQTT protocol
 */
public class MQTTUserClient implements UserClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTUserClient.class);
	
	private String broker;
	private int timeoutInSeconds;
	private String destinationTopic;
	private String senderAddress;
	private JSONMessageSerializer messageSerializer;
	
	/**
	 * Creates an instance from broker, destination topic and timeout
	 * @param broker the broker to use
	 * @param destinationTopic the topic that will receive the message
	 * @param timeoutInSeconds the timeout in seconds to wait for a response
	 */
	public MQTTUserClient(final String broker, final String destinationTopic, final int timeoutInSeconds,
			String senderAddress) {
		this.broker = broker;
		this.destinationTopic = destinationTopic;
		this.timeoutInSeconds = timeoutInSeconds;
		this.senderAddress = senderAddress;
		this.messageSerializer = new JSONMessageSerializer();
		
	}

	@Override
	public UniquidMessage execute(final UniquidMessage userRequest) throws UserClientException {
	
		return sendRecv(userRequest);
		
	}
	
	public UniquidMessage sendRecv(final UniquidMessage userRequest) throws UserClientException {
		
		LOGGER.info("Sending output message to {}", destinationTopic);
		
		BlockingConnection connection = null;
		
		try {
			final MQTT mqtt = new MQTT();
			
			mqtt.setHost(broker);
			
			connection = mqtt.blockingConnection();
			connection.connect();
			
			// to subscribe
			final Topic[] topics = { new Topic(senderAddress, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			byte[] payload = messageSerializer.serialize(userRequest);

			// consume
			connection.publish(destinationTopic, payload, QoS.AT_LEAST_ONCE, false);
			
			final Message message = connection.receive(timeoutInSeconds, TimeUnit.SECONDS);
			
			if (message == null) {

				throw new TimeoutException();

			}

			payload = message.getPayload();

			message.ack();

			// Create a JSON Message
			return messageSerializer.deserialize(payload);
			
		} catch (Throwable t) {
			
			LOGGER.error("Exception", t);
			
			throw new UserClientException("Exception", t);
			
		} finally {
			
			// disconnect
			try {

				if (connection != null) {
				
					LOGGER.info("Disconnecting");
					
					connection.disconnect();
					
				}

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		} 
	
	}
	
	public void send(final UniquidMessage userRequest) throws UserClientException {
		
		LOGGER.info("Sending output message to {}", destinationTopic);
		
		BlockingConnection connection = null;
		
		try {
			final MQTT mqtt = new MQTT();
			
			mqtt.setHost(broker);
			
			connection = mqtt.blockingConnection();
			connection.connect();
			
			byte[] payload = messageSerializer.serialize(userRequest);

			// consume
			connection.publish(destinationTopic, payload, QoS.AT_LEAST_ONCE, false);
			
		} catch (Throwable t) {
			
			LOGGER.error("Exception", t);
			
			throw new UserClientException("Exception", t);
			
		} finally {
			
			// disconnect
			try {

				if (connection != null) {
				
					LOGGER.info("Disconnecting");
					
					connection.disconnect();
					
				}

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		} 
	
	}

}