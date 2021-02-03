# Ultimate Tennis Statistics and Tennis Crystal Ball

[![Build Status](https://travis-ci.org/mcekovic/tennis-crystal-ball.svg?branch=master)](https://travis-ci.org/mcekovic/tennis-crystal-ball)
[![Build Status](https://github.com/mcekovic/tennis-crystal-ball/workflows/build/badge.svg)](https://github.com/mcekovic/tennis-crystal-ball/actions?query=workflow%3Abuild)
[![Web Site](https://img.shields.io/website/https/www.ultimatetennisstatistics.com.svg)](https://www.ultimatetennisstatistics.com)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mcekovic_tennis-crystal-ball&metric=alert_status)](https://sonarcloud.io/dashboard?id=mcekovic_tennis-crystal-ball)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?maxAge=2592000)](http://www.apache.org/licenses/LICENSE-2.0)
[![Top Language](https://img.shields.io/github/languages/top/mcekovic/tennis-crystal-ball)](https://github.com/mcekovic/tennis-crystal-ball/search?l=java)

![Logo](https://raw.githubusercontent.com/mcekovic/tennis-crystal-ball/master/tennis-stats/src/main/resources/static/images/uts-logo.png)
**Ultimate Tennis Statistics** is a Tennis Big Data Analysis tool with a nice web GUI.
**Tennis Crystal Ball** is a set of Match Prediction and Tournament Forecasts algorithms powered by AI and machine learning.

## Features

- 'GOAT' List - A.k.a. 'GOATometer' - Best players of Open Era ranked by 'GOAT' points with customizable weights (overall and by surface)
- Player Profile - Player information, season summary, tournament results, matches, timeline, rivalries, ranking, performance indicators and statistics with charts, 'GOAT' points breakdown and records
- Timelines - Dominance ('GOAT' points distribution among top players and seasons), Grand Slam, Tour Finals, Masters, Olympics, Davis Cup, Team Cups, Top Rankings and Surface timeline
- Head-to-Head - Head-to-head between two players with H2H matches, season summary, performance and statistics comparison, ranking, performance and statistics charts, 'GOAT' points breakdown and Hypothetical Matchup prediction based on the Neural Network Match Prediction Algorithm
- Heads-to-Heads - Heads-to-heads clusters among several players (i.e. among 'Big 4')
- Greatest Rivalries - Explore greatest rivalries, overall or by season, tournament level, surface or round
- Greatest Matches - Explore greatest matches ranked by special Match Greatness Score formula
- Ranking Tables - Player weekly ranking tables
- EloRatings - Player weekly [Elo rating](https://en.wikipedia.org/wiki/Elo_rating_system) tables (overall, by surface, outdoor or indoor, set or game) using customized Elo rating formula
- Ranking Charts - Player ranking and ranking point charts, compare players by constructing custom charts
- Peak Elo Ratings - Peak Elo ratings list for comparing players in their peaks (overall, by surface, set, service/return game and tie-break)
- Titles and Results - Titles and other results, filtered by various filters
- Titles and Results Charts - Titles and results charts, filtered by level, surface and seasons
- Top Performers - Find top performers in both performance and pressure situations categories
- Performance Charts - Performance charts for various performance categories, filtered by seasons
- Mental Toughness - Compare players by mental toughness using special mental toughness formula, filtered by various filters
- Statistics Leaders - Find statistics leaders in different statistical categories (100+), including dominance and break points ratios
- Top Match Stats - Top single match statistics figures filtered by various filters
- Statistics Charts - Statistics charts for various statistical categories, including dominance and break points ratios
- Seasons - Browse seasons and check season records, tournaments, rankings, performance, statistics and 'GOAT' points distribution among top players
- Best Seasons - Find which are the players' best seasons of the Open Era based on 'GOAT' points (overall and by surface)
- Tournaments - Browse tournaments, see players with most titles, historical tournament levels and surfaces and average participation
- Tournaments Events - Browse all Open Era tournament events, see tournament event draw, performance, statistics, historical winners and records
- Tournament Forecasts - Tournament Event Forecasts for in-progress tournaments driven by Neural Network Match Prediction Algorithm
- Records Book - Various match, tournament result and ranking records, famous and infamous (the best player that never...)
- Live Scores - Live Scores via [Enetscores](https://www.enetscores.com) by Enetpulse

### Technology

PostgreSQL, Java, Spring Boot, Thymeleaf, JQuery, Bootstrap, Google Charts, Groovy...

### Web Site
https://www.ultimatetennisstatistics.com

### Data Loaders
- For Jeff Sackmann ATP CSV repository: https://github.com/JeffSackmann/tennis_atp (as of commit cf2201c on 2 dec 2019)
- Database setup instructions: [#232](https://github.com/mcekovic/tennis-crystal-ball/issues/232)
- Docker image [mcekovic/uts-database](https://hub.docker.com/r/mcekovic/uts-database) with PostgreSQL database pre-populated with ATP tennis data as of season 2019: [#337](https://github.com/mcekovic/tennis-crystal-ball/issues/337) 

### Screenshots

##### Home Page
![Home Page](https://raw.githubusercontent.com/mcekovic/open-box/master/HomePage.png)

##### GOAT List
![GOAT List](https://raw.githubusercontent.com/mcekovic/open-box/master/GOATList.png)

##### Player Profile
![Player Profile](https://raw.githubusercontent.com/mcekovic/open-box/master/PlayerProfile.png)

##### Player Tournaments
![Player Tournaments](https://raw.githubusercontent.com/mcekovic/open-box/master/PlayerTournaments.png)

##### Player Timeline
![Player Timeline](https://raw.githubusercontent.com/mcekovic/open-box/master/PlayerTimeline.png)

##### Player Rivalries
![Player Rivalries](https://raw.githubusercontent.com/mcekovic/open-box/master/PlayerRivalries.png)

##### Player GOAT Points
![Player GOAT Points](https://raw.githubusercontent.com/mcekovic/open-box/master/PlayerGOATPoints.png)

##### Dominance Timeline
![Dominance Timeline](https://raw.githubusercontent.com/mcekovic/open-box/master/BigGunsTimeline.png)

##### Surface Timeline
![Surface Timeline](https://raw.githubusercontent.com/mcekovic/open-box/master/SurfaceTimeline.png)

##### Greatest Rivalries
![Greatest Rivalries](https://raw.githubusercontent.com/mcekovic/open-box/master/GreatestRivalries.png)

##### Ranking Charts
![Ranking Points Chart](https://raw.githubusercontent.com/mcekovic/open-box/master/RankingChart.png)

![GOAT Points Chart](https://raw.githubusercontent.com/mcekovic/open-box/master/RankingChart2.png)

##### Peak Elo Ratings
![Peak Elo Ratings](https://raw.githubusercontent.com/mcekovic/open-box/master/PeakEloRatings.png)

##### Top Performers
![Top Performers](https://raw.githubusercontent.com/mcekovic/open-box/master/TopPerformers.png)

##### Statistics Leaders
![Statistics Leaders](https://raw.githubusercontent.com/mcekovic/open-box/master/StatisticsLeaders.png)

##### Seasons
![Seasons](https://raw.githubusercontent.com/mcekovic/open-box/master/Seasons.png)

##### Best Seasons
![Best Seasons](https://raw.githubusercontent.com/mcekovic/open-box/master/BestSeasons.png)

##### Tournaments
![Tournaments](https://raw.githubusercontent.com/mcekovic/open-box/master/Tournaments.png)

##### Records Book
![Records Book](https://raw.githubusercontent.com/mcekovic/open-box/master/RecordsBook.png)

### License

Tennis Crystal Ball and Ultimate Tennis Statistics source code is licensed under [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

[![Creative Commons License](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png)](http://creativecommons.org/licenses/by-nc-sa/4.0/)

'GOAT' Formula, customizations of Elo Ratings for tennis, Match Prediction, Tournament Forecasts and other algorithms by Ultimate Tennis Statistics are licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

In short: Attribution is required. Non-commercial use only.
