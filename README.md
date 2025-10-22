#### Back Of The Net Soccer Analytics
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white)<br>
![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)
![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white)
![Eclipse](https://img.shields.io/badge/Eclipse-FE7A16.svg?style=for-the-badge&logo=Eclipse&logoColor=white)
##### A responsive web application that provides soccer fans with gameday scores, player stats, league standings, and quick links to major soccer competitions. Built with Thymeleaf, UIkit, and Java, using the Football-Data.org API for live data.
---
#### Features
##### League Stats: View top scorers, assists, and penalties for major leagues.
##### Gameday Scores: Quick access to current match scores.
##### Standings: FIFA World Cup, UEFA Champions League, and European Championship standings with accordion display.
##### Quick Links: Easy access to official league websites like Premier League, Bundesliga, LaLiga, Ligue 1, Serie A, Eredivisie, Primeira Liga, and Campeonato Brasileiro Série A.
#### Interactive UI:
##### Hover effects with animated soccer balls on “More” cards.
##### Responsive UI with UIkit grid and navbar.
##### Modals: Privacy Policy, Terms of Use, and Contact form for user inquiries.
---
#### UI & UX Notes
##### Fully responsive using UIkit grids.
##### Hover effects with animated soccer balls in the “More” section.
##### Dropdown menus for standings for FIFA, UEFA, and Euro competitions.
##### Modals are fully accessible and themed to match the site.
---
#### Tech Stack
##### Frontend: HTML5, Thymeleaf, CSS, UIkit
##### Backend: Java Spring Boot (assumed from Thymeleaf usage)
##### API: Football-Data.org
##### Icons & Assets: UIkit icons, custom SVG logos, soccer ball animation
---
#### Installation & Setup
##### Clone the repository:
```
git clone <repository-url>
cd back-of-the-net
```
##### Set up Java & Spring Boot:
##### Ensure you have Java 17+ and Maven installed.
##### Configure Football-Data.org API key:
##### Add your API key to ```application.properties``` or your backend service configuration.
##### Run the application:
```
mvn spring-boot:run
```
##### Access the app:
##### Open ```http://localhost:8080``` in your browser.
---
#### Project Structure
```
/src/main/resources/templates/ <br>
  ├─ fragments/ <br>
  │   ├─ head.html <br>
  │   ├─ navbar.html <br>
  │   └─ footer.html <br>
  ├─ league.html <br>
  ├─ more.html <br>
  └─ ... other templates <br>

/src/main/resources/static/ <br>
  ├─ css/style.css <br>
  ├─ js/scripts.js <br>
  └─ images/logo.png, soccer-bg.jpg, favicon.png <br>
```
--- 
#### License & Attribution
##### Powered by Football-Data.org API
##### Icons and images are either UIkit or publicly available (e.g., soccer ball SVG).
---
#### Future Improvements
##### Add user authentication for personalized stats.
##### Implement live match updates with WebSockets.
##### Enhance mobile navigation with off-canvas menus.
##### Integrate automated form submissions for contact modal.
---
