package com.inglorious_ratbastard.thetximino;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Controller
public class standingController {

    @GetMapping("/standings")
    public String getStandings(Model model) { 
    	model.addAttribute("worldCupGroups", fetchWorldCupStandings());
        model.addAttribute("euroGroups", fetchEuroStandings()); 
        
        model.addAttribute("worldCupSeason", fetchSeasonFilter("WC"));
        model.addAttribute("euroSeason", fetchSeasonFilter("EC"));
        return "standings";
    }

    private Map<String, List<Map<String, Object>>> fetchWorldCupStandings() {
        return fetchGroupedStandings("WC");
    }

    private Map<String, List<Map<String, Object>>> fetchEuroStandings() {
        return fetchGroupedStandings("EC");
    }
    
    private String fetchSeasonFilter(String leagueCode) {
        String apiUrl = "https://api.football-data.org/v4/competitions/" + leagueCode + "/standings";
        String seasonFilter = "";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.toString());
                JsonNode filtersNode = rootNode.path("filters");
                if (!filtersNode.isMissingNode()) {
                    seasonFilter = filtersNode.path("season").asText();
                }
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return seasonFilter;
    }

    private Map<String, List<Map<String, Object>>> fetchGroupedStandings(String leagueCode) {
        String apiUrl = "https://api.football-data.org/v4/competitions/" + leagueCode + "/standings";
        Map<String, List<Map<String, Object>>> groupedStandings = new HashMap<>();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.toString()); 
                JsonNode standingsArray = rootNode.path("standings");

                for (JsonNode standingNode : standingsArray) {
                    String stage = standingNode.path("stage").asText();
                    String groupName = standingNode.path("group").asText("General");

                    if (!stage.equals("GROUP_STAGE") && !stage.equals("ALL")) {
                        continue;
                    }

                    JsonNode table = standingNode.path("table");
                    List<Map<String, Object>> teamList = new ArrayList<>();

                    for (JsonNode teamNode : table) {
                        JsonNode team = teamNode.path("team");
                        Map<String, Object> teamData = new HashMap<>();
                        teamData.put("name", team.path("name").asText());
                        teamData.put("crest", team.path("crest").asText());
                        teamData.put("playedGames", teamNode.path("playedGames").asInt());
                        teamData.put("won", teamNode.path("won").asInt());
                        teamData.put("lost", teamNode.path("lost").asInt());
                        teamData.put("draw", teamNode.path("draw").asInt());
                        teamData.put("goalsFor", teamNode.path("goalsFor").asInt());
                        teamData.put("goalsAgainst", teamNode.path("goalsAgainst").asInt());
                        teamData.put("points", teamNode.path("points").asInt());

                        teamList.add(teamData);
                    }
                   
                    groupedStandings.put(groupName, teamList);
                }
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return groupedStandings;
    }
}

