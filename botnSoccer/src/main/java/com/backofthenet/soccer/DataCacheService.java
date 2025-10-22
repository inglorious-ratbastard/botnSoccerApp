/* package com.backofthenet.soccer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class DataCacheService {

    private static final long CACHE_TTL = 30 * 60 * 1000; 
    private static Map<String, Object> cache = new HashMap<>();
    private static long lastFetchTime = 0;

    public synchronized Map<String, Object> getAllData() {
        long now = System.currentTimeMillis();
        if (cache.isEmpty() || (now - lastFetchTime > CACHE_TTL)) {
            System.out.println("🔄 Refreshing all football data...");
            cache = fetchAll();
            lastFetchTime = now;
        } else {
            System.out.println("✅ Using cached football data");
        }
        return cache;
    }

    private Map<String, Object> fetchAll() {
        Map<String, Object> data = new HashMap<>();

        data.put("leagues", fetchLeagues());
        data.put("matches", fetchMatches());
        data.put("standings", fetchStandings());
        data.put("scorers", fetchScorers());

        return data;
    }

    private List<Map<String, Object>> fetchLeagues() {
        String[] competitionCodes = {"CL", "PL", "BL1", "PD", "SA", "FL1", "DED", "PPL", "BSA", "WC"};
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
                System.out.println("❌ League fetch error for " + code + ": " + e.getMessage());
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

    private List<Map<String, Object>> fetchMatches() {
        List<Map<String, Object>> matches = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(readUrl("https://api.football-data.org/v4/matches"));
            JSONArray matchArray = json.getJSONArray("matches");

            for (int i = 0; i < matchArray.length(); i++) {
                JSONObject match = matchArray.getJSONObject(i);
                Map<String, Object> matchInfo = new HashMap<>();

                JSONObject home = match.getJSONObject("homeTeam");
                JSONObject away = match.getJSONObject("awayTeam");
                matchInfo.put("competition", match.getJSONObject("competition").optString("name", ""));
                matchInfo.put("homeTeam", home.optString("name", ""));
                matchInfo.put("awayTeam", away.optString("name", ""));
                matchInfo.put("homeCrest", home.optString("crest", ""));
                matchInfo.put("awayCrest", away.optString("crest", ""));
                matchInfo.put("status", match.optString("status", ""));

                matches.add(matchInfo);
            }
        } catch (Exception e) {
            System.out.println("❌ Matches fetch error: " + e.getMessage());
        }
        return matches;
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
            System.out.println("❌ Standings fetch error: " + e.getMessage());
        }
        return standings;
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
            System.out.println("❌ Scorers fetch error: " + e.getMessage());
        }
        return scorers;
    }

    private String readUrl(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    public synchronized void forceRefresh() {
        System.out.println("🔄 Manual refresh triggered...");
        cache = fetchAll();
        lastFetchTime = System.currentTimeMillis();
    }

} */ 

package com.backofthenet.soccer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class DataCacheService {

    private static final long CACHE_TTL = 30 * 60 * 1000; 
    private static final long MIN_INTERVAL = 5_000; 
    private static final int MAX_RETRIES = 3;

    private static Map<String, Object> cache = new HashMap<>();
    private static Map<String, Long> lastRequestTime = new HashMap<>();
    private static long lastFetchTime = 0;

    public synchronized Map<String, Object> getAllData() {
        long now = System.currentTimeMillis();
        if (cache.isEmpty() || (now - lastFetchTime > CACHE_TTL)) {
            System.out.println("🔄 Refreshing all football data...");
            cache = fetchAll();
            lastFetchTime = now;
        } else {
            System.out.println("✅ Using cached football data");
        }
        return cache;
    }

    private Map<String, Object> fetchAll() {
        Map<String, Object> data = new HashMap<>();

        data.put("leagues", fetchLeagues());
        data.put("matches", fetchMatches());
        data.put("standings", fetchStandings());
        data.put("scorers", fetchScorers());

        return data;
    }

    private List<Map<String, Object>> fetchLeagues() {
        String[] competitionCodes = {"CL", "PL", "BL1", "PD", "SA", "FL1", "DED", "PPL", "BSA", "WC"};
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
                System.out.println("❌ League fetch error for " + code + ": " + e.getMessage());
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

    private List<Map<String, Object>> fetchMatches() {
        List<Map<String, Object>> matches = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(readUrl("https://api.football-data.org/v4/matches"));
            JSONArray matchArray = json.getJSONArray("matches");

            for (int i = 0; i < matchArray.length(); i++) {
                JSONObject match = matchArray.getJSONObject(i);
                Map<String, Object> matchInfo = new HashMap<>();

                JSONObject home = match.getJSONObject("homeTeam");
                JSONObject away = match.getJSONObject("awayTeam");
                matchInfo.put("competition", match.getJSONObject("competition").optString("name", ""));
                matchInfo.put("homeTeam", home.optString("name", ""));
                matchInfo.put("awayTeam", away.optString("name", ""));
                matchInfo.put("homeCrest", home.optString("crest", ""));
                matchInfo.put("awayCrest", away.optString("crest", ""));
                matchInfo.put("status", match.optString("status", ""));

                matches.add(matchInfo);
            }
        } catch (Exception e) {
            System.out.println("❌ Matches fetch error: " + e.getMessage());
        }
        return matches;
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
            System.out.println("❌ Standings fetch error: " + e.getMessage());
        }
        return standings;
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
            System.out.println("❌ Scorers fetch error: " + e.getMessage());
        }
        return scorers;
    }

    private String readUrl(String apiUrl) throws Exception {
        throttle(apiUrl);
        int attempt = 0;
        int baseDelay = 10_000; 

        while (attempt < MAX_RETRIES) {
            attempt++;
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Auth-Token", System.getenv("FOOTBALL_TOKEN"));
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
                System.out.println("⏳ Rate limit hit for " + apiUrl + ". Waiting " + (waitTime / 1000) + "s before retry (" + attempt + "/" + MAX_RETRIES + ")");
                Thread.sleep(waitTime);
            } else {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while (err != null && (line = err.readLine()) != null) sb.append(line);
                    throw new RuntimeException("Server returned HTTP " + responseCode + " for URL: " + apiUrl + " | " + sb);
                }
            }

            con.disconnect();
        }

        throw new RuntimeException("Failed to fetch data after " + MAX_RETRIES + " retries for URL: " + apiUrl);
    }

    private synchronized void throttle(String apiUrl) {
        long now = System.currentTimeMillis();
        long last = lastRequestTime.getOrDefault(apiUrl, 0L);
        if (now - last < MIN_INTERVAL) {
            try {
                Thread.sleep(MIN_INTERVAL - (now - last));
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime.put(apiUrl, System.currentTimeMillis());
    }

    public synchronized void forceRefresh() {
        System.out.println("🔄 Manual refresh triggered...");
        cache = fetchAll();
        lastFetchTime = System.currentTimeMillis();
    }
}
