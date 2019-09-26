package leomrlima.common.event;

public class DeviceEvent {

	public static enum Type {
		REGISTERED, CONNECTED, DISCONNECTED, SEND_DATA, SENT_DATA, DELETED
	}

	public String deviceId;

	public String gatewayId;

	public long timestamp;

	public Type type;

	public byte[] payload;

	@Override
	public String toString() {
		return "DeviceEvent [deviceId=" + deviceId + ", gatewayId=" + gatewayId + ", timestamp=" + timestamp + ", type="
				+ type + ", payload=" + (payload == null ? 0 : payload.length) + " bytes]";
	}

}
