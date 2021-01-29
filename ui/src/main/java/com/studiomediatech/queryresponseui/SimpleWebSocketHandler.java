package com.studiomediatech.queryresponseui;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiomediatech.queryresponse.util.Logging;
import com.studiomediatech.queryresponseui.events.AsyncEventEmitter;
import com.studiomediatech.queryresponseui.events.EventEmitter;
import com.studiomediatech.queryresponseui.events.QueryIssuedEvent;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleWebSocketHandler extends TextWebSocketHandler implements Logging {

	private static final int SEND_TIME_LIMIT = 6 * 1000;
	private static final int SEND_BUFFER_SIZE_LIMIT = 512 * 1024;

	private final Map<String, WebSocketSession> sessionsById = new ConcurrentHashMap<>();

	private final EventEmitter emitter;
	private final ObjectMapper objectMapper;

	public SimpleWebSocketHandler(AsyncEventEmitter emitter) {

		this.emitter = emitter;

		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		sessionsById.put(session.getId(),
				new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, SEND_BUFFER_SIZE_LIMIT));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		sessionsById.remove(session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		emitter.emitEvent(QueryIssuedEvent.valueOf(message.getPayload(), session.getId()));
	}

	public void handleCountQueriesAndResponses(long countQueriesSum, long countResponsesSum, long countFallbacksSum,
			double successRate, List<Double> successRates) {

		String json = String.format(Locale.US,
				"{\"metrics\": {" + "\"count_queries\": %d," + "\"count_responses\": %d," + "\"count_fallbacks\": %d,"
						+ "\"success_rate\": %f," + "\"success_rates\": %s" + "}}",
				countQueriesSum, countResponsesSum, countFallbacksSum, successRate, successRates);

		publishTextMessageWithPayload(json);
	}

	public void handleLatency(long minLatency, long maxLatency, double avgLatency, List<Double> latencies) {

		String json = String.format(Locale.US,
				"{\"metrics\": {" + (minLatency != -1 ? "\"min_latency\": %d," : "")
						+ (maxLatency != -1 ? "\"max_latency\": %d," : "") + "\"avg_latency\": %f,"
						+ "\"avg_latencies\": %s" + "}}",
				minLatency, maxLatency, avgLatency, latencies);

		publishTextMessageWithPayload(json);
	}

	public void handleThroughput(double queries, double responses, double avg, List<Double> throughputs) {

		String json = String.format(Locale.US,
				"{\"metrics\": {" + "\"throughput_queries\": %f," + "\"throughput_responses\": %f,"
						+ "\"avg_throughput\": %f," + "\"avg_throughputs\": %s" + "}}",
				queries, responses, avg, throughputs);

		publishTextMessageWithPayload(json);
	}

	public void handleNodes(Map<String, List<QueryPublisher.Stat>> nodes) {

		try {
			StringBuilder sb = new StringBuilder();

			sb.append("{\"nodes\": ");
			sb.append(objectMapper.writeValueAsString(nodes));
			sb.append("}");

			publishTextMessageWithPayload(sb.toString());
		} catch (JsonProcessingException e) {
			log().error("Failed to create nodes payload", e);
		}
	}

	private void publishTextMessageWithPayload(String json) {

		TextMessage message = new TextMessage(json.getBytes(StandardCharsets.UTF_8));

		for (WebSocketSession s : sessionsById.values()) {
			try {
				s.sendMessage(message);
			} catch (IOException e) {
				log().error("Could not publish text message to websocket", e);
			}
		}
	}

	public void handleResponse(Collection<Object> response, String id) {

		try {
			StringBuilder sb = new StringBuilder();

			sb.append("{\"response\": ");
			sb.append(objectMapper.writeValueAsString(response));
			sb.append("}");

			publishTextMessageWithPayloadToSession(id, sb.toString());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void publishTextMessageWithPayloadToSession(String id, String json) {

		TextMessage message = new TextMessage(json.getBytes(StandardCharsets.UTF_8));

		WebSocketSession s = sessionsById.get(id);

		if (s == null) {
			log().warn("Could not find websocket session for {} in {}", id, sessionsById);

			return;
		}

		try {
			s.sendMessage(message);
		} catch (IOException e) {
			log().error("Could not publish text message to websocket", e);
		}
	}
}
