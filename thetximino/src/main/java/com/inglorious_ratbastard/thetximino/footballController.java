package com.inglorious_ratbastard.thetximino;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class footballController {

	@GetMapping()
    public String getMatches(Model model) {
        String apiUrl = "https://api.football-data.org/v4/matches";
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();
                try {
                    JSONObject json = new JSONObject(jsonResponse);
                    JSONArray matches = json.getJSONArray("matches");

                    List<Map<String, Object>> matchList = new ArrayList<>();

                    for (int i = 0; i < matches.length(); i++) {
                        JSONObject match = matches.getJSONObject(i);

                        String competitionName = match.has("competition") ? match.getJSONObject("competition").getString("name") : "Unknown";
                        String homeTeamName = match.getJSONObject("homeTeam").getString("name");
                        String awayTeamName = match.getJSONObject("awayTeam").getString("name");
                        String homeTeamCrest = match.getJSONObject("homeTeam").getString("crest");
                        String awayTeamCrest = match.getJSONObject("awayTeam").getString("crest");
                        String matchStatus = match.optString("status", "Unknown");
                        JSONObject score = match.getJSONObject("score").getJSONObject("fullTime");
                        Object homeScore = score.isNull("home") ? "N/A" : score.get("home");
                        Object awayScore = score.isNull("away") ? "N/A" : score.get("away");
                        
                        matchStatus = matchStatus.replaceAll("[^a-zA-Z\\s]", " ");

                        Map<String, Object> matchInfo = new HashMap<>();
                        matchInfo.put("competition", competitionName);
                        matchInfo.put("homeTeam", homeTeamName);
                        matchInfo.put("awayTeam", awayTeamName);
                        matchInfo.put("homeCrest", homeTeamCrest);
                        matchInfo.put("awayCrest", awayTeamCrest);
                        matchInfo.put("homeScore", homeScore);
                        matchInfo.put("awayScore", awayScore);
                        matchInfo.put("status", matchStatus);

                        matchList.add(matchInfo);
                    }

                    model.addAttribute("matches", matchList);

                    
                } catch (Exception e) {
                    JSONArray json = new JSONArray(jsonResponse);
                    model.addAttribute("apiResponse", json.toString(4));
                }
            } else {
                model.addAttribute("apiResponse", "GET request failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            model.addAttribute("apiResponse", "Exception occurred: " + e.getMessage());
        }

        return "index";
    } 
	
  }

