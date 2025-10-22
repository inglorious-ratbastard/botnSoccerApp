/* package com.backofthenet.soccer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class statsController {

    @Autowired
    private DataCacheService dataCacheService;

    @GetMapping("/stats")
    public String getLeagueScorers(@RequestParam(defaultValue = "PL") String league, Model model) {
       
        Map<String, Object> allData = dataCacheService.getAllData();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allScorers = (List<Map<String, Object>>) allData.get("scorers");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allLeagues = (List<Map<String, Object>>) allData.get("leagues");

        Map<String, Object> leagueInfo = allLeagues.stream()
                .filter(l -> league.equalsIgnoreCase((String) l.get("code")))
                .findFirst()
                .orElseGet(() -> {
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("name", league);
                    fallback.put("code", league);
                    fallback.put("emblem", "");
                    return fallback;
                });

        List<Map<String, Object>> filteredScorers = new ArrayList<>();
        if (allScorers != null) {
            filteredScorers = allScorers;
        }

        model.addAttribute("players", filteredScorers);
        model.addAttribute("leagueName", leagueInfo.get("name"));
        model.addAttribute("leagueCode", leagueInfo.get("code"));
        model.addAttribute("leagueEmblem", leagueInfo.get("emblem"));

        return "stats";
    }
} */ 

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
public class statsController {

    private static final Map<String, CacheEntry> cache = new HashMap<>();
    private static final long CACHE_EXPIRY = 10 * 60 * 1000; 

    @GetMapping("/stats")
    public String getLeagueScorers(@RequestParam(defaultValue = "PL") String league, Model model) {
        String apiUrl = "https://api.football-data.org/v4/competitions/" + league + "/scorers";

        Object cachedData = getCachedData(apiUrl);
        if (cachedData != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = (Map<String, Object>) cachedData;
            model.addAttribute("players", cached.get("players"));
            model.addAttribute("leagueName", cached.get("leagueName"));
            model.addAttribute("leagueCode", cached.get("leagueCode"));
            return "stats";
        }

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
                JSONArray scorers = json.getJSONArray("scorers");
                JSONObject competition = json.getJSONObject("competition");

                List<Map<String, Object>> playerList = new ArrayList<>();

                for (int i = 0; i < scorers.length(); i++) {
                    JSONObject scorer = scorers.getJSONObject(i);
                    JSONObject player = scorer.getJSONObject("player");
                    JSONObject team = scorer.getJSONObject("team");

                    Map<String, Object> playerData = new HashMap<>();
                    playerData.put("name", player.optString("name", "Unknown"));
                    playerData.put("nationality", player.optString("nationality", "Unknown"));
                    playerData.put("team", team.optString("name", "Unknown"));
                    playerData.put("crest", team.optString("crest", ""));
                    playerData.put("section", competition.optString("name", league));
                    playerData.put("goals", scorer.optInt("goals", 0));
                    playerData.put("assists", scorer.optInt("assists", 0));
                    playerData.put("penalties", scorer.optInt("penalties", 0));

                    playerList.add(playerData);
                }

                model.addAttribute("players", playerList);
                model.addAttribute("leagueName", competition.optString("name", league));
                model.addAttribute("leagueCode", league);

                Map<String, Object> cacheData = new HashMap<>();
                cacheData.put("players", playerList);
                cacheData.put("leagueName", competition.optString("name", league));
                cacheData.put("leagueCode", league);

                cache.put(apiUrl, new CacheEntry(cacheData));

            } else {
                model.addAttribute("apiResponse", "GET request failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            model.addAttribute("apiResponse", "Exception occurred: " + e.getMessage());
        }

        return "stats";
    }

    private Object getCachedData(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_EXPIRY) {
            return entry.data;
        }
        cache.remove(key);
        return null;
    }

    private static class CacheEntry {
        Object data;
        long timestamp;

        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
