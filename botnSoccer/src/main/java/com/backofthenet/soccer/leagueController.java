package com.backofthenet.soccer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class leagueController {

    private static final Map<String, Object> cache = new HashMap<>();
    private static long lastFetchTime = 0;

    @GetMapping("/league")
    public String getLeagueStandings(@RequestParam(defaultValue = "PL") String league, Model model) {
        long now = System.currentTimeMillis();

        String cacheKey = "league_" + league;
        if (!cache.containsKey(cacheKey) || now - lastFetchTime > 30 * 60 * 1000) {
            System.out.println("⏳ Fetching fresh standings for " + league + "...");
            Map<String, Object> leagueData = fetchLeagueStandings(league);
            cache.put(cacheKey, leagueData);
            lastFetchTime = now;
        } else {
            System.out.println("✅ Using cached standings for " + league);
        }

        @SuppressWarnings("unchecked")
		Map<String, Object> leagueData = (Map<String, Object>) cache.get(cacheKey);
        model.addAllAttributes(leagueData);

        return "league";
    }

    private Map<String, Object> fetchLeagueStandings(String league) {
        Map<String, Object> data = new HashMap<>();
        String apiUrl = "https://api.football-data.org/v4/competitions/" + league + "/standings";
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

                if (json.has("standings")) {
                    JSONArray standings = json.getJSONArray("standings");

                    if (standings.length() > 0) {
                        JSONObject totalStandings = standings.getJSONObject(0);
                        JSONArray table = totalStandings.getJSONArray("table");

                        List<Map<String, Object>> teamList = new ArrayList<>();

                        for (int i = 0; i < Math.min(20, table.length()); i++) {
                            JSONObject teamStats = table.getJSONObject(i);
                            JSONObject team = teamStats.getJSONObject("team");

                            Map<String, Object> teamData = new HashMap<>();
                            teamData.put("name", team.optString("name", "Unknown"));
                            teamData.put("crest", team.optString("crest", ""));
                            teamData.put("playedGames", teamStats.optInt("playedGames", 0));
                            teamData.put("won", teamStats.optInt("won", 0));
                            teamData.put("lost", teamStats.optInt("lost", 0));
                            teamData.put("draw", teamStats.optInt("draw", 0));
                            teamData.put("goalsFor", teamStats.optInt("goalsFor", 0));
                            teamData.put("goalsAgainst", teamStats.optInt("goalsAgainst", 0));
                            teamData.put("points", teamStats.optInt("points", 0));

                            teamList.add(teamData);
                        }

                        data.put("teams", teamList);
                    }
                }
            } else {
                System.out.println("⚠️ GET request failed with response code: " + responseCode);
                data.put("apiResponse", "GET request failed with response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("⚠️ Exception fetching " + league + ": " + e.getMessage());
            data.put("apiResponse", "Exception occurred: " + e.getMessage());
        }

        return data;
    }
}
