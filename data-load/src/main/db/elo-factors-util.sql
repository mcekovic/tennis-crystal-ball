SELECT 1 rank, round(avg(elo_rating)) overall, round(avg(hard_elo_rating)) hard, round(avg(clay_elo_rating)) clay, round(avg(grass_elo_rating)) grass, round(avg(carpet_elo_rating)) carpet FROM player_elo_ranking WHERE rank = 1
UNION SELECT 2, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 2
UNION SELECT 3, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 3
UNION SELECT 4, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 4
UNION SELECT 5, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 5
UNION SELECT 7, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 7
UNION SELECT 10, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 10
UNION SELECT 15, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 15
UNION SELECT 20, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 20
UNION SELECT 30, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 30
UNION SELECT 50, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 50
UNION SELECT 70, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 70
UNION SELECT 100, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 100
UNION SELECT 150, round(avg(elo_rating)), round(avg(hard_elo_rating)), round(avg(clay_elo_rating)), round(avg(grass_elo_rating)), round(avg(carpet_elo_rating)) FROM player_elo_ranking WHERE rank = 150
UNION SELECT 200, round(avg(elo_rating)), round(avg(elo_rating)), round(avg(elo_rating)), round(avg(elo_rating)), round(avg(elo_rating)) FROM player_elo_ranking WHERE rank = 200
ORDER BY rank;

SELECT round(avg(elo_rating)) overall, round(avg(hard_elo_rating)) hard, round(avg(clay_elo_rating)) clay, round(avg(grass_elo_rating)) grass, round(avg(carpet_elo_rating)) carpet
FROM player_elo_ranking
WHERE rank <= 50;