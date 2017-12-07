package com.uniquid.connector.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;

import com.uniquid.connector.ConnectorException;
import com.uniquid.connector.EndPoint;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.MessageSerializer;
import com.uniquid.messages.MessageType;
import com.uniquid.messages.UniquidMessage;
import com.uniquid.messages.serializers.JSONMessageSerializer;

/**
 * Implementation of a {@link EndPoint} used by {@link MQTTConnector}
 */
public class TCPEndPoint implements EndPoint {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TCPEndPoint.class);
	
	private Socket socket;
	
	private final FunctionRequestMessage providerRequest;
	private final FunctionResponseMessage providerResponse;
	
	private MessageSerializer messageSerializer = new JSONMessageSerializer();
	
	/**
	 * Creates a new instance from the byte array message and broker
	 * @param mqttMessageRequest the message received
	 * @param broker the broker to use
	 * @throws ConnectorException in case a problem occurs.
	 */
	TCPEndPoint(Socket socket) throws ConnectorException {
		
		this.socket = socket;

		try {
			
			DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

			byte[] length = new byte[5];
			int size = inputStream.read(length, 0, 5);

			ByteBuffer buffer = ByteBuffer.wrap(length);
			int num = buffer.getInt();

			int len = 0;
			byte[] msg = new byte[num];

			int r = inputStream.read(msg, 0, msg.length);
			
			UniquidMessage messageReceived = messageSerializer.deserialize(msg);
			
			if (MessageType.FUNCTION_REQUEST.equals(messageReceived.getMessageType())) {
				
				// Retrieve message
				providerRequest = (FunctionRequestMessage) messageReceived;
						
				providerResponse = new FunctionResponseMessage();
				providerResponse.setId(providerRequest.getId());
				
			} else {
			
				throw new Exception("Received an invalid message type " + messageReceived.getMessageType());
			
			}
		
		} catch (Exception ex) {
			
			throw new ConnectorException("Exception during creation of endpoint", ex);
		
		}
	}

	@Override
	public FunctionRequestMessage getInputMessage() {
		return providerRequest;
	}

	@Override
	public FunctionResponseMessage getOutputMessage() {
		return providerResponse;
	}

	@Override
	public void flush() throws ConnectorException {
		
		try {
			
			byte[] payload = messageSerializer.serialize(providerResponse);
			
			DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
			int messageSize = payload.length;
	
			byte[] prefix = ByteBuffer.allocate(5).order(ByteOrder.BIG_ENDIAN).putInt(messageSize).array();
			byte[] result = Arrays.concatenate(prefix, payload);
	
			dataOutputStream.write(result);
			dataOutputStream.flush();
		
		} catch (Exception ex) {
			
			LOGGER.error("Exception " + ex.getMessage(), ex);
			
			throw new ConnectorException(ex);
			
		} finally {
			
			try {
				
				socket.close();
				
			} catch (IOException ex) {
				
				LOGGER.error("Exception " + ex.getMessage(), ex);
			}
			
		}
		
	}

}