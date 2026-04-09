package com.sap.x.mcpserverdemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weather.gov")
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    @Tool(name = "getWeatherForecast", description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(
            @ToolParam(description = "Latitude coordinate") double latitude,
            @ToolParam(description = "Longitude coordinate") double longitude
    ) {
        try {
            // Step 1: Get the grid point info
            String pointResponse = restClient.get()
                    .uri("/points/{lat},{lon}", latitude, longitude)
                    .retrieve()
                    .body(String.class);

            JsonNode pointNode = objectMapper.readTree(pointResponse);
            String forecastUrl = pointNode.at("/properties/forecast").asText();

            // Step 2: Get the forecast
            String forecastResponse = RestClient.create().get()
                    .uri(forecastUrl)
                    .header("Accept", "application/geo+json")
                    .header("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                    .retrieve()
                    .body(String.class);

            JsonNode forecastNode = objectMapper.readTree(forecastResponse);
            JsonNode periods = forecastNode.at("/properties/periods");

            StringBuilder result = new StringBuilder();
            int count = Math.min(3, periods.size());
            for (int i = 0; i < count; i++) {
                JsonNode period = periods.get(i);
                result.append(String.format("%s: %d%s, Wind: %s %s, %s\n",
                        period.get("name").asText(),
                        period.get("temperature").asInt(),
                        period.get("temperatureUnit").asText(),
                        period.get("windSpeed").asText(),
                        period.get("windDirection").asText(),
                        period.get("shortForecast").asText()));
            }
            return result.toString();
        } catch (Exception e) {
            return "Error fetching forecast: " + e.getMessage();
        }
    }

    @Tool(name = "getAlerts", description = "Get weather alerts for a US state")
    public String getAlerts(
            @ToolParam(description = "Two-letter US state code (e.g. CA, NY)") String state
    ) {
        try {
            String response = restClient.get()
                    .uri("/alerts/active/area/{state}", state)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.get("features");

            if (features == null || features.isEmpty()) {
                return "No active alerts for " + state;
            }

            StringBuilder result = new StringBuilder();
            int count = Math.min(5, features.size());
            for (int i = 0; i < count; i++) {
                JsonNode props = features.get(i).get("properties");
                result.append(String.format("Event: %s\nArea: %s\nSeverity: %s\nDescription: %s\n\n",
                        props.get("event").asText(),
                        props.get("areaDesc").asText(),
                        props.get("severity").asText(),
                        props.get("headline").asText()));
            }
            return result.toString();
        } catch (Exception e) {
            return "Error fetching alerts: " + e.getMessage();
        }
    }
}