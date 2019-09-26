package leomrlima.producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import leomrlima.common.event.DeviceEvent;
import leomrlima.common.kafka.Bootstrap;
import leomrlima.common.kafka.KafkaConnection;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String appId = System.getenv("PRODUCER_NAME");
		if (appId == null) {
			logger.warn("PRODUCER_NAME environment variable is null, please set it to something meaningul!");
			appId = UUID.randomUUID().toString();
		}

		logger.debug("Producer {} started", appId);

		// set up Kafka access
		Bootstrap bootstrap = new Bootstrap(appId);
		KafkaConnection connection = new KafkaConnection("deviceEvents", bootstrap);

		// set up host to receive connections
		try (ServerSocket ss = new ServerSocket(8982)) {
			logger.info("Server is ready");
			while (true) {
				try (Socket s = ss.accept()) {
					String gatewayId = s.getRemoteSocketAddress().toString();
					if (gatewayId.indexOf('/') >= 0) {
						gatewayId = gatewayId.substring(gatewayId.indexOf('/') + 1);
					}
					logger.info("New connection from {}", gatewayId);
					BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
					PrintWriter output = new PrintWriter(s.getOutputStream(), true);
					output.println("Hi!");

					for (String line = input.readLine(); line != null; line = input.readLine()) {
						logger.info("New info: {}", line);

						String[] info = line.split("\\|");
						DeviceEvent event = new DeviceEvent();
						event.deviceId = info[1];
						event.gatewayId = gatewayId;
						event.timestamp = System.currentTimeMillis();
						event.type = DeviceEvent.Type.valueOf(info[0]);
						if (info.length > 2) {
							event.payload = Base64.getDecoder().decode(info[2]);
						}

						connection.fire(event);

						output.println("OK!");
					}

					logger.info("{} disconnected", gatewayId);
				} catch (IOException e) {
					logger.error("IO Exception", e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// NOOP
					}
				} catch (Exception e) {
					logger.error("Exception", e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// NOOP
					}
				}
			}
		} catch (IOException e) {
			logger.error("IO Exception", e);
		}

		connection.close();

	}

}
