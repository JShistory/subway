package com.knu.subway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.subway.Dto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("http://swopenAPI.seoul.go.kr/api/subway/6f4c6e794f6265683730424b715154/json/realtimeStationArrival/0/5").build();
        this.objectMapper = objectMapper;
    }

    public List<Dto> getSubwayArrivals(String stationName) {
        Mono<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{stationName}").build(stationName))
                .retrieve()
                .bodyToMono(String.class);

        String responseBody = response.block();
        var dtos = new ArrayList<Dto>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
            JSONArray element = (JSONArray) jsonObject.get("realtimeArrivalList");
            for (int i = 0; i < element.size(); i++) {
                var tempEle = (JSONObject) element.get(i);
                var dto = new Dto();
                dto.setTrainLineNm((String) tempEle.get("trainLineNm"));
                dto.setStatnNm((String) tempEle.get("statnNm"));
                dto.setBarvlDt((String) tempEle.get("trainLineNm"));
                dto.setBtrainNo((String) tempEle.get("btrainNo"));
                dto.setBstatnId((String) tempEle.get("bstatnId"));
                dto.setBstatnNm((String) tempEle.get("bstatnNm"));
                dto.setRecptnDt((String) tempEle.get("recptnDt"));
                dto.setArvlMsg2((String) tempEle.get("arvlMsg2"));
                dto.setArvlMsg3((String) tempEle.get("arvlMsg3"));
                dto.setArvlCd((String) tempEle.get("arvlCd"));
                dtos.add(dto);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(dtos);
        if (responseBody == null || responseBody.isEmpty()) {
            return Collections.emptyList(); // or handle as needed
        }

        return dtos;
    }

    private List<Dto> parseResponse(String responseBody) {
        try {
            // Parse JSON into a single Dto object (assuming JSON starts with an object, not array)
            Dto dto = objectMapper.readValue(responseBody, Dto.class);
            return Collections.singletonList(dto);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }
}
