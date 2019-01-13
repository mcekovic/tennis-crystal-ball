# Tennis Crystal Ball

[![Build Status](https://travis-ci.org/mcekovic/tennis-crystal-ball.svg?branch=master)](https://travis-ci.org/mcekovic/tennis-crystal-ball)
[![Web Site](https://img.shields.io/website-up-down-green-red/https/www.ultimatetennisstatistics.com.svg)](https://www.ultimatetennisstatistics.com)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?maxAge=2592000)](http://www.apache.org/licenses/LICENSE-2.0)

Ultimate Tennis Statistics and Big Data Analysis with nice web GUI.

## Features

- 'GOAT' List - Best players of Open Era ranked by 'GOAT' points with customizable weights (overall and by surface)
- Player Profile - Player information, season summary, tournament results, matches, timeline, rivalries, ranking, performance indicators and statistics with charts, 'GOAT' points breakdown and records
- Timelines - Dominance ('GOAT' points distribution among top players and seasons), Grand Slam, Tour Finals, Masters, Olympics, Davis Cup, World Team Cup, Top Rankings and Surface timeline
- Head-to-Head - Head-to-head between two players with H2H matches, season summary, performance and statistics comparision, ranking, performance and statistics charts, 'GOAT' points breakdown and Hypothetical Matchup prediction based on the Neural Network Match Prediction Algorithm
- Heads-to-Heads - Heads-to-heads clusters among several players (i.e. among 'Big 4')
- Greatest Rivalries - Explore greatest rivalries, overall or by season, tournament level, surface or round
- Greatest Matches - Explore greatest matches ranked by special Match Greatness Score formula
- Ranking Tables - Player weekly ranking tables
- EloRatings - Player weekly [Elo rating](https://en.wikipedia.org/wiki/Elo_rating_system) tables (overall, by surface, outdoor or indoor, set or game) using customized Elo rating formula
- Ranking Charts - Player ranking and ranking point charts, compare players by constructing custom charts
- Peak Elo Ratings - Peak Elo ratings list for comparing players in their peaks (overall, by surface, set, service/return game and tie break)
- Top Performers - Find top performers in both performance and pressure situations categories
- Performance Charts - Performance charts for various performance categories, filtered by seasons
- Statistics Leaders - Find statistics leaders in different statistics categories (90+), including dominance and break points ratios
- Statistics Charts - Statistics charts for various statistics categories, including dominance and break points ratios
- Seasons - Browse seasons and check season records, tournaments, rankings, performance, statistics and 'GOAT' points distribution among top players
- Best Seasons - Find which are the players' best seasons of the Open Era based on 'GOAT' points (overall and by surface)
- Tournaments - Browse tournaments, see players with most titles, historical tournament levels and surfaces and average participation
- Tournaments Events - Browse all Open Era tournament events, see tournament event draw, performance, statistics, historical winners and records
- Tournament Forecasts - Tournament Event Forecasts for in-progress tournaments driven by Neural Network Match Prediction Algorithm
- Records Book - Various match, tournament result and ranking records, famous and infamous (best player that never...)
- Live Scores - Live Scores via [Livescore.in](https://www.livescore.in)

## Roadmap

- [In-progress Game, Set and Match Forecasts](https://github.com/mcekovic/tennis-crystal-ball/issues/154) - In-progress game, set and match forecast with probabilities for in-progress matches
- [Glicko Ratings](https://github.com/mcekovic/tennis-crystal-ball/issues/77) - Improve Elo Rating computation with [Glicko 2](https://en.wikipedia.org/wiki/Glicko_rating_system) variant, to better reflect periods when player is out of competition
- [Round Robin Tournament Forecast](https://github.com/mcekovic/tennis-crystal-ball/issues/97) - Round Robin Tournament forecasting for Tour Finals, in addition to ordinary knock-out tournament forecasting

### Technology

PostgreSQL, Java, Spring Boot, Thymeleaf, JQuery, Bootstrap, Google Charts, Groovy...

### Web Site
http://www.ultimatetennisstatistics.com

### Data Loaders
- For Jeff Sackmann ATP CSV repository: https://github.com/JeffSackmann/tennis_atp

### Screenshots

##### Home Page
![Home Page](https://github.com/mcekovic/open-box/blob/master/HomePage.png?raw=true)

##### GOAT List
![GOAT List](https://github.com/mcekovic/open-box/blob/master/GOATList.png?raw=true)

##### Player Profile
![Player Profile](https://github.com/mcekovic/open-box/blob/master/PlayerProfile.png?raw=true)

##### Player Tournaments
![Player Tournaments](https://github.com/mcekovic/open-box/blob/master/PlayerTournaments.png?raw=true)

##### Player Timeline
![Player Timeline](https://github.com/mcekovic/open-box/blob/master/PlayerTimeline.png?raw=true)

##### Player Rivalries
![Player Rivalries](https://github.com/mcekovic/open-box/blob/master/PlayerRivalries.png?raw=true)

##### Player GOAT Points
![Player GOAT Points](https://github.com/mcekovic/open-box/blob/master/PlayerGOATPoints.png?raw=true)

##### Dominance Timeline
![Dominance Timeline](https://github.com/mcekovic/open-box/blob/master/BigGunsTimeline.png?raw=true)

##### Surface Timeline
![Surface Timeline](https://github.com/mcekovic/open-box/blob/master/SurfaceTimeline.png?raw=true)

##### Greatest Rivalries
![Greatest Rivalries](https://github.com/mcekovic/open-box/blob/master/GreatestRivalries.png?raw=true)

##### Ranking Charts
![Ranking Points Chart](https://github.com/mcekovic/open-box/blob/master/RankingChart.png?raw=true)

![GOAT Points Chart](https://github.com/mcekovic/open-box/blob/master/RankingChart2.png?raw=true)

##### Peak Elo Ratings
![Peak Elo Ratings](https://github.com/mcekovic/open-box/blob/master/PeakEloRatings.png?raw=true)

##### Top Performers
![Top Performers](https://github.com/mcekovic/open-box/blob/master/TopPerformers.png?raw=true)

##### Statistics Leaders
![Statistics Leaders](https://github.com/mcekovic/open-box/blob/master/StatisticsLeaders.png?raw=true)

##### Seasons
![Seasons](https://github.com/mcekovic/open-box/blob/master/Seasons.png?raw=true)

##### Best Seasons
![Best Seasons](https://github.com/mcekovic/open-box/blob/master/BestSeasons.png?raw=true)

##### Tournaments
![Tournaments](https://github.com/mcekovic/open-box/blob/master/Tournaments.png?raw=true)

##### Records Book
![Records Book](https://github.com/mcekovic/open-box/blob/master/RecordsBook.png?raw=true)

### License

Tennis Crystal Ball and Ultimate Tennis Statistics source code is licensed under [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

[![Creative Commons License](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png)](http://creativecommons.org/licenses/by-nc-sa/4.0/)

'GOAT' Formula, customizations of Elo Ratings for tennis, Match Prediction, Tournament Forecast and other algorithms by Ultimate Tennis Statistics are licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

In short: Attribution is required. Non-commercial use only.
