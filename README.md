### botnSoccerApp
#### Back of the Net Soccer Analysis

##### **Back of the Net** is a Spring Boot web application that aggregates and displays football data from the [Football-Data.org API](https://www.football-data.org/). The app provides live and historical match results, league standings, top scorers, and other soccer statistics for major competitions worldwide.

---

#### <ins>Features:</ins>

#### - **League and Team Overview**
##### - Fetches data for major competitions including UEFA Champions League (CL), Premier League (PL), Bundesliga (BL1), La Liga (PD), Serie A (SA), Ligue 1 (FL1), Eredivisie (DED), Primeira Liga (PPL), Brasileirao (BSA), and FIFA World Cup (WC).
#####  - Displays team names and crests (logos).

#### - **Match Data**
##### - Shows scheduled, live, and finished matches.
#####  - Provides home/away teams, crests, scores, and status.

#### - **League Standings**
#####  - Full standings for selected leagues.
#####  - Supports Champions League, European Championship, and World Cup group standings.

#### - **Top Scorers & Stats**
##### - Displays players with most goals, assists, and penalties per league.

#### - **Caching & Performance**
#####  - Caches API responses for matches, standings, scorers, and leagues to minimize API calls.
#####  - Custom TTL (time-to-live) for different types of data.
#####  - Automatic cache refresh notifications.

#### - **Random Match Feature**
#####  - Fetches a random upcoming or in-play match for Champions League and Premier League.

---

#### <ins>Technologies Used</ins>

##### - **Backend:** Java 17+, Spring Boot
##### - **Frontend:** Thymeleaf templates for HTML rendering
##### - **API Integration:** [Football-Data.org API](https://www.football-data.org/)
##### - **JSON Processing:** org.json and Jackson
##### - **Caching:** In-memory caching with TTL per data type

---
#### <ins>Project Structure</ins>

##### SoccerMain.java – Spring Boot main application entry point.

##### DataCacheService.java – Handles API fetching, caching, and throttling.

##### <ins>Controllers</ins>:

##### homeController.java – Home page data.

##### matchesController.java – Match listings.

##### leagueController.java – Individual league standings.

##### standingController.java – UCL, World Cup, Euro standings.

##### statsController.java – League scorers and stats.

##### moreController.java – Extended statistics and API responses.

##### RefreshController.java – Cache monitoring and refresh endpoints.

#### <ins>Thymeleaf templates</ins>:

##### index.html, matches.html, league.html, standings.html, stats.html, more.html
---
#### <ins>API Endpoints</ins>:
#### Endpoint	Description
##### ```/``` Home page with leagues, Champions League teams, and random matches.
##### ```/matches``` List of all matches with scores and status.
##### ```/league?league=PL```	League standings for a specific competition (default: Premier League).
##### ```/standings```	Grouped standings for World Cup, Euro, and Champions League.
##### ```/uclstandings?filter=T18```	Top 18 teams in Champions League standings.
##### ```/stats?league=PL```	Top scorers and stats for a specific league.
##### ```/more?league=PL```	Additional league statistics.
##### ```/api/cache-refresh```	Returns timestamp of last cache refresh.
##### ```/api/force-refresh``` Triggers a forced cache refresh.
---
#### <ins>Data Caching</ins>:

##### The app caches different types of data to reduce API calls:

##### <ins>Data Type TTL</ins>:
##### - Matches	5 minutes
##### - Standings	30 minutes
##### - Scorers	30 minutes
##### - Leagues & Teams	24 hours

##### The RefreshController provides endpoints to monitor and force cache refreshes.
---
#### <ins>Notes</ins>:

##### Rate-limiting is handled using throttling and retry mechanisms to comply with Football-Data API limits.

##### All external API calls are synchronized to prevent excessive simultaneous requests.

##### Random match fetching prioritizes upcoming or in-play matches.
---
#### <ins>License</ins>:

##### This project is open-source under the MIT License.
