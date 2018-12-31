SELECT extract(YEAR FROM date) season, surface, source,
	round((100.0 * avg(1.0 / winner_price + 1.0 / loser_price - 1.0))::DECIMAL, 4) margin,
	round((100.0 * (count(*) FILTER ( WHERE winner_price < loser_price)) / count(*))::DECIMAL, 4) rate,
	round((sum(greatest(winner_price, loser_price) / (winner_price + loser_price)) / (count(*) FILTER ( WHERE winner_price < loser_price)))::DECIMAL, 4) calibration,
	round((-sum(ln(loser_price / (winner_price + loser_price))) / count(*))::DECIMAL, 4) log_loss,
	round((sum(power(winner_price / (winner_price + loser_price), 2)) / count(*))::DECIMAL, 4) brier,
	round((100.0 * (sum(winner_price) FILTER ( WHERE winner_price < loser_price)::DECIMAL - count(*)) / count(*))::DECIMAL, 4) profit_w,
	round((100.0 * (sum(winner_price) FILTER ( WHERE winner_price > loser_price)::DECIMAL - count(*)) / count(*))::DECIMAL, 4) profit_l
FROM match_price
INNER JOIN match USING (match_id)
GROUP BY CUBE(season, surface, source)
ORDER BY season DESC, surface NULLS FIRST, source NULLS FIRST;