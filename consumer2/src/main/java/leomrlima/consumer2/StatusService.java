package leomrlima.consumer2;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.mapping.keyvalue.KeyValueTemplate;

@Path("/status")
public class StatusService {

	private static final Logger logger = LoggerFactory.getLogger(StatusService.class);

	@Inject
	@Database(value = DatabaseType.KEY_VALUE, provider = BucketManagerProducer.STATUS_BUCKET)
	private KeyValueTemplate template;

	@PostConstruct
	private void init() {
		logger.info("StatusService started");
	}

	@GET
	@Path("{deviceId}")
	public Response getStatus(@PathParam("deviceId") String deviceId) {
		if (deviceId == null || deviceId.trim().length() == 0) {
			return Response.serverError().entity("Device ID cannot be blank").build();
		}
		Optional<DeviceStatus> status = template.get(deviceId, DeviceStatus.class);
		if (!status.isPresent()) {
			return Response.status(Response.Status.NOT_FOUND).entity("Device not found for ID: " + deviceId).build();
		}
		String json = JsonUtils.toJson(status.get()).toString();
		return Response.ok(json, MediaType.APPLICATION_JSON).build();
	}
}
