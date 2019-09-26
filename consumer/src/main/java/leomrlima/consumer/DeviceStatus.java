package leomrlima.consumer;

public class DeviceStatus {

	public String deviceId;

	public String status;

	public int byteCount;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getByteCount() {
		return byteCount;
	}

	public void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}

	@Override
	public String toString() {
		return "DeviceStatus [deviceId=" + deviceId + ", status=" + status + ", byteCount=" + byteCount + "]";
	}
}
