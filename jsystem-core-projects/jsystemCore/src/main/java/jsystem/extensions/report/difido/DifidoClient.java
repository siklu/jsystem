package jsystem.extensions.report.difido;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;

public class DifidoClient {

	private static final Logger log = Logger.getLogger(DifidoClient.class.getName());

	private final Properties execProps;
	private final String EXEC_PROPS_FILE = "execution.properties";

	private static final String BASE_URI_TEMPLATE = "http://%s:%d/api/";
	private final String baseUri;

	/**
	 * Accepts only 200 and 204 and returns the response body. The body is empty
	 * when the response has no entity (e.g. 204 No Content).
	 */
	private static final HttpClientResponseHandler<String> BODY_HANDLER = (ClassicHttpResponse response) -> {
		final int responseCode = response.getCode();
		final HttpEntity entity = response.getEntity();
		final String body = entity == null ? "" : EntityUtils.toString(entity);
		if (responseCode != 200 && responseCode != 204) {
			throw new IOException(
					"Request was not successful. Response is: " + responseCode + ".\n Response body: " + body);
		}
		return body;
	};

	public DifidoClient(String host, int port) {
		baseUri = String.format(BASE_URI_TEMPLATE, host, port);
		execProps = new Properties();
	}

	public int addExecution(ExecutionDetails details) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpPost method = new HttpPost(baseUri + "executions/");
			if (details != null) {
				final String descriptionJson = new ObjectMapper().writeValueAsString(details);
				method.setEntity(new StringEntity(descriptionJson, StandardCharsets.UTF_8));
				method.setHeader("Content-Type", "application/json");
			}
			final String body = client.execute(method, BODY_HANDLER);
			int executionId = Integer.parseInt(body.trim());
			File f = new File(EXEC_PROPS_FILE);
			if (f.exists()) {
				execProps.load(new FileInputStream(EXEC_PROPS_FILE));
				execProps.clear();
			}
			execProps.setProperty("execution.id", Integer.toString(executionId));
			execProps.store(new FileOutputStream(EXEC_PROPS_FILE), null);
			return executionId;
		}
	}

	public void updateScenarioName(int executionId) throws Exception {
		execProps.load(new FileInputStream(EXEC_PROPS_FILE));
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpPut method = new HttpPut(baseUri + "executions/" + executionId + "?serial=" + execProps.getProperty("execution.serial"));
			method.setHeader("Content-Type", "text/plain");
			client.execute(method, BODY_HANDLER);
		}
	}

	public void updateSerialNumber(int executionId) throws Exception {
		execProps.load(new FileInputStream(EXEC_PROPS_FILE));
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpPut method = new HttpPut(baseUri + "executions/" + executionId + "?serial=" + execProps.getProperty("execution.serial"));
			method.setHeader("Content-Type", "text/plain");
			client.execute(method, BODY_HANDLER);
		}
	}

	public void endExecution(int executionId) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpPut method = new HttpPut(baseUri + "executions/" + executionId + "?active=false");
			method.setHeader("Content-Type", "text/plain");
			client.execute(method, BODY_HANDLER);
		}
		execProps.clear();
	}

	public int addMachine(int executionId, MachineNode machine) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost method = new HttpPost(baseUri + "executions/" + executionId + "/machines/");
			final String json = new ObjectMapper().writeValueAsString(machine);
			method.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
			method.setHeader("Content-Type", "application/json");
			return Integer.parseInt(client.execute(method, BODY_HANDLER).trim());
		}
	}

	public void updateMachine(int executionId, int machineId, MachineNode machine) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPut method = new HttpPut(baseUri + "executions/" + executionId + "/machines/" + machineId);
			final String json = new ObjectMapper().writeValueAsString(machine);
			method.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
			method.setHeader("Content-Type", "application/json");
			client.execute(method, BODY_HANDLER);
		}
	}

	public void addTestDetails(int executionId, TestDetails testDetails) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost method = new HttpPost(baseUri + "executions/" + executionId + "/details");
			final String json = new ObjectMapper().writeValueAsString(testDetails);
			method.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
			method.setHeader("Content-Type", "application/json");
			client.execute(method, BODY_HANDLER);
		}
	}

	public void addFile(final int executionId, final String uid, final File file) throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost method = new HttpPost(baseUri + "executions/" + executionId + "/details/" + uid + "/file/");
			method.setEntity(MultipartEntityBuilder.create()
					.addBinaryBody("file", file)
					.build());
			client.execute(method, BODY_HANDLER);
		}
	}

}
