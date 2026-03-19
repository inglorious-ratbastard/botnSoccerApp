package com.backofthenet.soccer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class DataCacheService {

    @Value("${football.api.token}")
    private String apiToken;

    @PostConstruct
    public void init() {
        System.out.println("TOKEN: " + apiToken);
    }

    @Autowired
    private RefreshController refreshController;

    private static final long MATCHES_TTL = 5 * 60 * 1000;      
    private static final long STANDINGS_TTL = 30 * 60 * 1000;   
    private static final long SCORERS_TTL = 30 * 60 * 1000;     
    private static final long TEAMS_TTL = 24 * 60 * 60 * 1000;  

    private static final int MAX_RETRIES = 3;

    private static long lastRequestTime = 0;
    private static final long MIN_INTERVAL_RT = 6000;
    
    private List<Map<String, Object>> leaguesCache = new ArrayList<>();
    private List<Map<String, Object>> matchesCache = new ArrayList<>();
    private Map<String, List<Map<String, Object>>> standingsCache = new HashMap<>();
    private List<Map<String, Object>> scorersCache = new ArrayList<>();

    private long leaguesLastFetch = 0;
    private long matchesLastFetch = 0;
    private long standingsLastFetch = 0;
    private long scorersLastFetch = 0;

    public Map<String, Object> getAllData() {
        Map<String, Object> data = new HashMap<>();
        data.put("leagues", getLeagues());
        data.put("matches", getMatches());
        data.put("standings", getStandings());
        data.put("scorers", getScorers());
        return data;
    }

    private boolean cacheExpired(long lastFetch, long ttl) {
        return System.currentTimeMillis() - lastFetch > ttl;
    }

    private synchronized List<Map<String, Object>> getLeagues() {
        if (!leaguesCache.isEmpty() && !cacheExpired(leaguesLastFetch, TEAMS_TTL)) {
            System.out.println("✅ Using cached league/team data");
            return leaguesCache;
        }
        System.out.println("⏳ Fetching fresh league/team data...");
        leaguesCache = fetchLeagues();
        leaguesLastFetch = System.currentTimeMillis();
        refreshController.notifyCacheRefresh(); 
        return leaguesCache;
    }

    private List<Map<String, Object>> fetchLeagues() {
        String[] competitionCodes = {"CL","PL","BL1","PD","SA","FL1","DED","PPL","BSA","WC"};
        List<Map<String, Object>> leagues = new ArrayList<>();
        for (String code : competitionCodes) {
            try {
                String apiUrl = "https://api.football-data.org/v4/competitions/" + code + "/teams";
                JSONObject json = new JSONObject(readUrl(apiUrl));
                JSONObject competition = json.getJSONObject("competition");
                JSONArray teams = json.getJSONArray("teams");

                Map<String, Object> leagueData = new HashMap<>();
                leagueData.put("code", code);
                leagueData.put("name", competition.optString("name", ""));
                leagueData.put("emblem", competition.optString("emblem", ""));
                leagueData.put("teams", extractTeams(teams));
                leagues.add(leagueData);
            } catch (Exception e) {
                System.out.println("League fetch error for " + code + ": " + e.getMessage());
            }
        }
        return leagues;
    }

    private List<Map<String, String>> extractTeams(JSONArray teams) {
        List<Map<String, String>> teamList = new ArrayList<>();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = teams.getJSONObject(i);
            Map<String, String> teamInfo = new HashMap<>();
            teamInfo.put("name", team.optString("name"));
            teamInfo.put("crest", team.optString("crest"));
            teamList.add(teamInfo);
        }
        return teamList;
    }

    private synchronized List<Map<String, Object>> getMatches() {
        if (!matchesCache.isEmpty() && !cacheExpired(matchesLastFetch, MATCHES_TTL)) {
            System.out.println("✅ Using cached match data");
            return matchesCache;
        }
        System.out.println("⏳ Fetching fresh match data...");
        matchesCache = fetchMatches();
        matchesLastFetch = System.currentTimeMillis();
        refreshController.notifyCacheRefresh(); 
        return matchesCache;
    }	

    private List<Map<String, Object>> fetchMatches() {
        List<Map<String, Object>> matches = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(readUrl("https://api.football-data.org/v4/matches"));
            JSONArray matchArray = json.getJSONArray("matches");
            for (int i = 0; i < matchArray.length(); i++) {
                JSONObject match = matchArray.getJSONObject(i);
                JSONObject home = match.getJSONObject("homeTeam");
                JSONObject away = match.getJSONObject("awayTeam");

                Map<String, Object> matchInfo = new HashMap<>();
                matchInfo.put("competition", match.getJSONObject("competition").optString("name", ""));
                matchInfo.put("homeTeam", home.optString("name", ""));
                matchInfo.put("awayTeam", away.optString("name", ""));
                matchInfo.put("homeCrest", home.optString("crest", ""));
                matchInfo.put("awayCrest", away.optString("crest", ""));
                matchInfo.put("status", match.optString("status", ""));
                matches.add(matchInfo);
            }
        } catch (Exception e) {
            System.out.println("Matches fetch error: " + e.getMessage());
        }
        return matches;
    }
    
    private synchronized Map<String, List<Map<String, Object>>> getStandings() {
        if (!standingsCache.isEmpty() && !cacheExpired(standingsLastFetch, STANDINGS_TTL)) {
            System.out.println("✅ Using cached standings");
            return standingsCache;
        }
        System.out.println("⏳ Fetching fresh standings...");
        standingsCache = fetchStandings();
        standingsLastFetch = System.currentTimeMillis();
        refreshController.notifyCacheRefresh(); 
        return standingsCache;
    }

    private Map<String, List<Map<String, Object>>> fetchStandings() {
        Map<String, List<Map<String, Object>>> standings = new HashMap<>();
        try {
            JSONObject json = new JSONObject(readUrl("https://api.football-data.org/v4/competitions/CL/standings"));
            JSONArray groups = json.getJSONArray("standings");
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                JSONArray table = group.getJSONArray("table");
                List<Map<String, Object>> groupTeams = new ArrayList<>();
                for (int j = 0; j < table.length(); j++) {
                    JSONObject entry = table.getJSONObject(j);
                    JSONObject team = entry.getJSONObject("team");
                    Map<String, Object> teamData = new HashMap<>();
                    teamData.put("name", team.optString("name"));
                    teamData.put("crest", team.optString("crest"));
                    teamData.put("points", entry.optInt("points"));
                    groupTeams.add(teamData);
                }
                standings.put(group.optString("group", "Group A"), groupTeams);
            }
        } catch (Exception e) {
            System.out.println("Standings fetch error: " + e.getMessage());
        }
        return standings;
    }

    private synchronized List<Map<String, Object>> getScorers() {
        if (!scorersCache.isEmpty() && !cacheExpired(scorersLastFetch, SCORERS_TTL)) {
            System.out.println("✅ Using cached scorers");
            return scorersCache;
        }
        System.out.println("⏳ Fetching fresh scorers...");
        scorersCache = fetchScorers();
        scorersLastFetch = System.currentTimeMillis();
        refreshController.notifyCacheRefresh(); 
        return scorersCache;
    }

    private List<Map<String, Object>> fetchScorers() {
        List<Map<String, Object>> scorers = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(readUrl("https://api.football-data.org/v4/competitions/PL/scorers"));
            JSONArray players = json.getJSONArray("scorers");
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                JSONObject p = player.getJSONObject("player");
                JSONObject team = player.getJSONObject("team");
                Map<String, Object> info = new HashMap<>();
                info.put("name", p.optString("name"));
                info.put("team", team.optString("name"));
                info.put("crest", team.optString("crest"));
                info.put("goals", player.optInt("goals"));
                scorers.add(info);
            }
        } catch (Exception e) {
            System.out.println("Scorers fetch error: " + e.getMessage());
        }
        return scorers;
    }

    private String readUrl(String apiUrl) throws Exception {
        throttle();
        int attempt = 0;
        int baseDelay = 10000;
        while (attempt < MAX_RETRIES) {
            attempt++;
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Auth-Token", apiToken);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    return sb.toString();
                }
            } else if (responseCode == 429) {
                String retryAfter = con.getHeaderField("Retry-After");
                int waitTime = retryAfter != null ? Integer.parseInt(retryAfter) * 1000 : baseDelay * attempt;
                System.out.println("⏳ Rate limit hit. Waiting " + (waitTime / 1000) + "s...");
                Thread.sleep(waitTime);
            } else {
                throw new RuntimeException("Server returned HTTP " + responseCode + " for URL: " + apiUrl);
            }

            con.disconnect();
        }

        throw new RuntimeException("Failed after retries for URL: " + apiUrl);
    }

    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        if (now - lastRequestTime < MIN_INTERVAL_RT) {
            try {
                Thread.sleep(MIN_INTERVAL_RT - (now - lastRequestTime));
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
