package com.backofthenet.soccer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Controller
public class standingController {

    private static final Map<String, Object> cache = new HashMap<>();
    private static final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; 

    private boolean isCacheValid(String key) {
        return cache.containsKey(key)
                && (System.currentTimeMillis() - cacheTimestamps.getOrDefault(key, 0L)) < CACHE_TTL_MS;
    }

    private void putCache(String key, Object data) {
        cache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    @GetMapping("/standings")
    public String getStandings(Model model) {
        model.addAttribute("worldCupGroups", getCachedData("worldCupGroups", this::fetchWorldCupStandings));
        model.addAttribute("euroGroups", getCachedData("euroGroups", this::fetchEuroStandings));
        model.addAttribute("championsLeaguePages", getCachedData("championsLeaguePages", this::fetchChampionsLeagueStandings));

        model.addAttribute("worldCupSeason", getCachedData("worldCupSeason", () -> fetchSeasonFilter("WC")));
        model.addAttribute("euroSeason", getCachedData("euroSeason", () -> fetchSeasonFilter("EC")));
        model.addAttribute("championsLeagueSeason", getCachedData("championsLeagueSeason", () -> fetchSeasonFilter("CL")));

        return "standings";
    }

    @SuppressWarnings("unchecked")
	@GetMapping("/uclstandings")
    @ResponseBody
    public List<Map<String, Object>> getUclStandings(@RequestParam String filter) {
        String cacheKey = "uclstandings_" + filter.toUpperCase();

        if (isCacheValid(cacheKey)) {
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> allTeams = fetchUclStandings(filter);
        putCache(cacheKey, allTeams);

        return allTeams;
    }

    @SuppressWarnings("unchecked")
	private <T> T getCachedData(String key, Supplier<T> fetcher) {
        if (isCacheValid(key)) {
            return (T) cache.get(key);
        }
        T data = fetcher.get();
        putCache(key, data);
        return data;
    }

    private List<Map<String, Object>> fetchUclStandings(String filter) {
        String apiUrl = "https://api.football-data.org/v4/competitions/CL/standings";
        List<Map<String, Object>> allTeams = new ArrayList<>();

        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode standingsArray = mapper.readTree(response).path("standings");

                for (JsonNode standingNode : standingsArray) {
                    for (JsonNode teamNode : standingNode.path("table")) {
                        Map<String, Object> teamData = new HashMap<>();
                        JsonNode team = teamNode.path("team");

                        teamData.put("position", teamNode.path("position").asInt());
                        teamData.put("name", team.path("name").asText());
                        teamData.put("crest", team.path("crest").asText());
                        teamData.put("won", teamNode.path("won").asInt());
                        teamData.put("lost", teamNode.path("lost").asInt());
                        teamData.put("draw", teamNode.path("draw").asInt());
                        teamData.put("goalsFor", teamNode.path("goalsFor").asInt());
                        teamData.put("goalsAgainst", teamNode.path("goalsAgainst").asInt());
                        teamData.put("goalDifference", teamNode.path("goalDifference").asInt());
                        teamData.put("points", teamNode.path("points").asInt());
                        allTeams.add(teamData);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        allTeams.sort(Comparator.comparingInt(t -> (int) t.get("position")));

        if ("T18".equalsIgnoreCase(filter)) {
            return allTeams.subList(0, Math.min(18, allTeams.size()));
        } else if ("L18".equalsIgnoreCase(filter)) {
            int start = Math.max(allTeams.size() - 18, 0);
            return allTeams.subList(start, allTeams.size());
        }
        return allTeams;
    }

    private Map<String, List<Map<String, Object>>> fetchWorldCupStandings() {
        return fetchGroupedStandings("WC");
    }

    private Map<String, List<Map<String, Object>>> fetchEuroStandings() {
        return fetchGroupedStandings("EC");
    }

    private Map<String, List<Map<String, Object>>> fetchChampionsLeagueStandings() {
        String apiUrl = "https://api.football-data.org/v4/competitions/CL/standings";
        List<Map<String, Object>> allTeams = new ArrayList<>();

        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode standingsArray = mapper.readTree(response).path("standings");

                for (JsonNode standingNode : standingsArray) {
                    for (JsonNode teamNode : standingNode.path("table")) {
                        Map<String, Object> teamData = new HashMap<>();
                        JsonNode team = teamNode.path("team");

                        teamData.put("position", teamNode.path("position").asInt());
                        teamData.put("name", team.path("name").asText());
                        teamData.put("crest", team.path("crest").asText());
                        teamData.put("won", teamNode.path("won").asInt());
                        teamData.put("lost", teamNode.path("lost").asInt());
                        teamData.put("draw", teamNode.path("draw").asInt());
                        teamData.put("goalsFor", teamNode.path("goalsFor").asInt());
                        teamData.put("goalsAgainst", teamNode.path("goalsAgainst").asInt());
                        teamData.put("goalDifference", teamNode.path("goalDifference").asInt());
                        teamData.put("points", teamNode.path("points").asInt());
                        allTeams.add(teamData);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, List<Map<String, Object>>> pages = new LinkedHashMap<>();
        for (int i = 0; i < allTeams.size(); i += 18) {
            pages.put("Page " + ((i / 18) + 1), allTeams.subList(i, Math.min(i + 18, allTeams.size())));
        }

        return pages;
    }

    private String fetchSeasonFilter(String leagueCode) {
        String apiUrl = "https://api.football-data.org/v4/competitions/" + leagueCode + "/standings";
        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                JsonNode filtersNode = new ObjectMapper().readTree(response).path("filters");
                if (!filtersNode.isMissingNode()) {
                    return filtersNode.path("season").asText();
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Map<String, List<Map<String, Object>>> fetchGroupedStandings(String leagueCode) {
        String apiUrl = "https://api.football-data.org/v4/competitions/" + leagueCode + "/standings";
        Map<String, List<Map<String, Object>>> groupedStandings = new HashMap<>();

        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode standingsArray = mapper.readTree(response).path("standings");

                for (JsonNode standingNode : standingsArray) {
                    String stage = standingNode.path("stage").asText();
                    if (!stage.equals("GROUP_STAGE") && !stage.equals("ALL")) continue;

                    String groupName = standingNode.path("group").asText("General");
                    List<Map<String, Object>> teamList = new ArrayList<>();

                    for (JsonNode teamNode : standingNode.path("table")) {
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

    private HttpURLConnection setupConnection(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));
        return connection;
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response.toString();
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }
}
