/* package com.backofthenet.soccer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class matchesController {

    private final DataCacheService dataService;

    public matchesController(DataCacheService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/matches")
    public String showMatches(Model model) {
        Map<String, Object> cache = dataService.getAllData();
        model.addAttribute("matches", cache.get("matches"));
        return "matches"; 
    }
} */

package com.backofthenet.soccer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Controller
public class matchesController {

    private static Map<String, Object> cache = new HashMap<>();
    private static long lastFetchTime = 0;

    @GetMapping("/matches")
    public String getMatches(Model model) {
        long now = System.currentTimeMillis();

        if (cache.isEmpty() || now - lastFetchTime > 30 * 60 * 1000) {
            System.out.println("⏳ Fetching fresh match data from API...");
            cache = fetchMatches();
            lastFetchTime = now;
        } else {
            System.out.println("✅ Using cached match data");
        }

        model.addAllAttributes(cache);
        return "matches";
    }

    private Map<String, Object> fetchMatches() {
        Map<String, Object> data = new HashMap<>();
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

                JSONObject json = new JSONObject(response.toString());
                JSONArray matches = json.getJSONArray("matches");

                List<Map<String, Object>> matchList = new ArrayList<>();

                for (int i = 0; i < matches.length(); i++) {
                    JSONObject match = matches.getJSONObject(i);

                    String competitionName = match.has("competition")
                            ? match.getJSONObject("competition").optString("name", "Unknown")
                            : "Unknown";

                    JSONObject homeTeam = match.getJSONObject("homeTeam");
                    JSONObject awayTeam = match.getJSONObject("awayTeam");

                    String homeTeamName = homeTeam.optString("name", "Unknown");
                    String awayTeamName = awayTeam.optString("name", "Unknown");
                    String homeTeamCrest = homeTeam.optString("crest", "");
                    String awayTeamCrest = awayTeam.optString("crest", "");

                    String matchStatus = match.optString("status", "Unknown").replaceAll("[^a-zA-Z\\s]", " ");

                    JSONObject score = match.getJSONObject("score").optJSONObject("fullTime");
                    Object homeScore = (score == null || score.isNull("home")) ? "N/A" : score.get("home");
                    Object awayScore = (score == null || score.isNull("away")) ? "N/A" : score.get("away");

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

                data.put("matches", matchList);

            } else {
                System.out.println("⚠️ GET request failed with response code: " + responseCode);
                data.put("apiResponse", "Failed to fetch matches: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("⚠️ Exception fetching matches: " + e.getMessage());
            data.put("apiResponse", "Exception occurred: " + e.getMessage());
        }

        return data;
    }
}
