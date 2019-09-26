package leomrlima.consumer2;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class DeviceStatus {

	@Id
	private String deviceId;

	private String status;

	private int byteCount;
	
	@Deprecated
	DeviceStatus() {
	}

	@JsonbCreator
	public DeviceStatus(@JsonbProperty("deviceId") String deviceId, 
			@JsonbProperty("status") String status, @JsonbProperty("byteCount") int byteCount) {
		super();
		this.deviceId = deviceId;
		this.status = status;
		this.byteCount = byteCount;
	}

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
