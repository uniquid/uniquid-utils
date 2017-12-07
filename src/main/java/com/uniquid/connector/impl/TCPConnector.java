package com.uniquid.connector.impl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.connector.Connector;
import com.uniquid.connector.ConnectorException;
import com.uniquid.connector.EndPoint;

/**
 * Implementation of a {@link Connector} that uses the MQTT protocol.
 */
public class TCPConnector implements Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(TCPConnector.class);

	private int port;
	private Queue<Socket> inputQueue;

	private ScheduledExecutorService receiverExecutorService;

	/**
	 * Creates a MQTTConnector that listen on the specified receiving topic and on the specified broker.  
	 * @param topic the topic to listen to
	 * @param broker the MQTT broker to use 
	 */
	private TCPConnector(int port) {

		this.port = port;
		this.inputQueue = new LinkedList<Socket>();
		
	}

	/**
	 * Builder for {@link TCPConnector}
	 */
	public static class Builder {
		private int _port;

		/**
		 * Set the listening topic
		 * @param _topic the topic to listen to
		 * @return the Builder
		 */
		public Builder set_port(int _port) {
			this._port = _port;
			return this;
		}

		/**
		 * Returns an instance of a {@link TCPConnector}
		 * @return an instance of a {@link TCPConnector}
		 */
		public TCPConnector build() {

			return new TCPConnector(_port);
		}

	}

	@Override
	public EndPoint accept() throws ConnectorException, InterruptedException {

		try {

			synchronized (inputQueue) {

				while (inputQueue.isEmpty()) {

					LOGGER.trace("inputQueue is empty. waiting");

					inputQueue.wait();

				}

				LOGGER.trace("inputQueue not empty! fetching element");
				
				Socket inputSocket = inputQueue.poll();

				LOGGER.trace("returning MQTTEndPoint");
				
				return new TCPEndPoint(inputSocket);

			}

		} catch (InterruptedException ex) {
			
			LOGGER.error("Catched InterruptedException", ex);
			
			throw ex;
			
		} catch (Exception ex) {
			
			LOGGER.error("Catched Exception", ex);

			throw new ConnectorException(ex);

		}

	}

	@Override
	public void start() {

		receiverExecutorService = Executors.newSingleThreadScheduledExecutor();

		final Runnable receiver = new Runnable() {

			@Override
			public void run() {
				
				try {
				
					ServerSocket serverSocket = new ServerSocket(port);
					
					while (!Thread.currentThread().isInterrupted()) {
						
						try {
							
							Socket socket = serverSocket.accept();
							
							// Create a JSON Message
							synchronized (inputQueue) {
	
								inputQueue.add(socket);
								inputQueue.notifyAll();
	
							}
		
						} catch (Throwable t) {
							
							LOGGER.error("Catched Exception", t);
							
						}
	
					}
				
				} catch (Exception ex) {
					
					LOGGER.error("Catched Exception", ex);
					
				}

			}

		};
		
		LOGGER.info("Starting receiving");

		// Start receiver
		receiverExecutorService.execute(receiver);

	}

	@Override
	public void stop() {
		
		LOGGER.info("Stopping MQTTConnector");

		receiverExecutorService.shutdownNow();

	}

}
