/* package com.backofthenet.soccer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class homeController {

    private final DataCacheService dataService;

    public homeController(DataCacheService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Map<String, Object> cache = dataService.getAllData();

        model.addAttribute("leagues", cache.get("leagues"));
        model.addAttribute("matches", cache.get("matches"));
        model.addAttribute("standings", cache.get("standings"));
        model.addAttribute("scorers", cache.get("scorers"));

        return "index"; 
    }

    @GetMapping("/refresh")
    public String refreshData(Model model) {
        dataService.forceRefresh();
        Map<String, Object> cache = dataService.getAllData();
        model.addAttribute("message", "✅ Data manually refreshed!");
        model.addAttribute("leagues", cache.get("leagues"));
        model.addAttribute("matches", cache.get("matches"));
        model.addAttribute("standings", cache.get("standings"));
        model.addAttribute("scorers", cache.get("scorers"));
        return "index";
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
public class homeController { 
	
    private static Map<String, Object> cache = new HashMap<>();
    private static long lastFetchTime = 0;

    @GetMapping("/")
    public String getLeaguesAndTeams(Model model) {
        long now = System.currentTimeMillis();

        if (cache.isEmpty() || now - lastFetchTime > 30 * 60 * 1000) {
            System.out.println("⏳ Fetching fresh data from API...");
            cache = fetchLeaguesAndMatches();
            lastFetchTime = now;
        } else {
            System.out.println("✅ Using cached data");
        }

        model.addAllAttributes(cache);
        return "index";
    }

    private Map<String, Object> fetchLeaguesAndMatches() {
        Map<String, Object> data = new HashMap<>();

        String[] competitionCodes = {"CL", "PL", "BL1", "PD", "SA", "FL1", "DED", "PPL", "BSA", "WC"};
        List<Map<String, Object>> leagues = new ArrayList<>();
        List<Map<String, String>> championsLeagueTeams = new ArrayList<>();

        for (String code : competitionCodes) {
            String apiUrl = "https://api.football-data.org/v4/competitions/" + code + "/teams";
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
                    JSONObject competition = json.getJSONObject("competition");
                    String competitionName = competition.optString("name", "Unknown League");
                    String competitionEmblem = competition.optString("emblem", "");

                    JSONArray teams = json.getJSONArray("teams");
                    List<Map<String, String>> teamList = new ArrayList<>();

                    for (int i = 0; i < teams.length(); i++) {
                        JSONObject team = teams.getJSONObject(i);
                        Map<String, String> teamInfo = new HashMap<>();
                        teamInfo.put("name", team.optString("name", ""));
                        teamInfo.put("crest", team.optString("crest", ""));
                        teamList.add(teamInfo);
                    }

                    Map<String, Object> leagueData = new HashMap<>();
                    leagueData.put("code", code);
                    leagueData.put("name", competitionName);
                    leagueData.put("emblem", competitionEmblem);
                    leagueData.put("teams", teamList);

                    leagues.add(leagueData);

                    if (code.equals("CL")) {
                        championsLeagueTeams.addAll(teamList);
                    }

                } else {
                    System.out.println("Failed to fetch " + code + ": " + responseCode);
                }

            } catch (Exception e) {
                System.out.println("Error fetching " + code + ": " + e.getMessage());
            }
        }

        Map<String, String> randomCLMatch = getRandomMatch("CL", "Champions League");
        Map<String, String> randomPLMatch = getRandomMatch("PL", "Premier League");

        data.put("leagues", leagues);
        data.put("championsLeagueTeams", championsLeagueTeams);
        data.put("randomMatch", randomCLMatch);
        data.put("randomPLMatch", randomPLMatch);
        data.put("randomMatchJson", new JSONObject(randomCLMatch).toString());
        data.put("randomPLMatchJson", new JSONObject(randomPLMatch).toString());

        return data;
    }
    
    private Map<String, String> getRandomMatch(String competitionCode, String competitionName) {
        String[] statuses = {"SCHEDULED", "TIMED", "IN_PLAY", "FINISHED"};
        Map<String, String> matchInfo = new HashMap<>();

        for (String status : statuses) {
            String apiUrl = "https://api.football-data.org/v4/competitions/" + competitionCode + "/matches?status=" + status;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray matches = json.getJSONArray("matches");

                    if (matches.length() > 0) {
                        JSONObject match = matches.getJSONObject(new Random().nextInt(matches.length()));
                        JSONObject homeTeam = match.getJSONObject("homeTeam");
                        JSONObject awayTeam = match.getJSONObject("awayTeam");

                        matchInfo.put("competition", competitionName);
                        matchInfo.put("homeName", homeTeam.optString("name", "Unknown"));
                        matchInfo.put("homeCrest", homeTeam.optString("crest", ""));
                        matchInfo.put("awayName", awayTeam.optString("name", "Unknown"));
                        matchInfo.put("awayCrest", awayTeam.optString("crest", ""));
                        matchInfo.put("utcDate", match.optString("utcDate", ""));
                        matchInfo.put("status", match.optString("status", ""));
                        break; 
                    }
                }
            } catch (Exception e) {
                System.out.println("Error fetching " + competitionCode + " (" + status + "): " + e.getMessage());
            }
        } 
        
        return matchInfo;
    }

}
