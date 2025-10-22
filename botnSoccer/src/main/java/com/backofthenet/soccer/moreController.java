package com.backofthenet.soccer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class moreController {
	
  @GetMapping("/more") 
  public String getLeagueStandings(@RequestParam(defaultValue = "PL") String league, Model model) {
	  
	  String apiUrl = "http://api.football-data.org/v4/competitions/" + league + "/stats";
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
            		            teamData.put("name", team.getString("name"));
            		            teamData.put("crest", team.getString("crest"));
            		            teamData.put("playedGames", teamStats.getInt("playedGames"));
            		            teamData.put("won", teamStats.getInt("won"));
            		            teamData.put("lost", teamStats.getInt("lost"));
            		            teamData.put("draw", teamStats.getInt("draw"));
            		            teamData.put("goalsFor", teamStats.getInt("goalsFor"));
            		            teamData.put("goalsAgainst", teamStats.getInt("goalsAgainst"));
            		            teamData.put("points", teamStats.getInt("points"));

            		            teamList.add(teamData);
            		        }

            		        model.addAttribute("teams", teamList);
            		    }
            		}

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
	  
	  return "more"; 
  } 
}
	