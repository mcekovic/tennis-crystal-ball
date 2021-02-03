SELECT extract(YEAR FROM date) season, surface, source,
	round(100.0 * avg(margin)::DECIMAL, 4) margin,
	round(100.0 * count(*) FILTER (WHERE predicted) / count(*), 4) rate,
	round((-sum(ln(winner_probability)) / count(*))::DECIMAL, 4) log_loss,
	round((sum(power(loser_probability, 2)) / count(*))::DECIMAL, 4) brier,
   round((sum(greatest(winner_probability, loser_probability)) / (count(*) FILTER ( WHERE predicted)))::DECIMAL, 4) calibration,
	round((100.0 * (sum(winner_price * winner_probability)) / count(*))::DECIMAL, 4) payout
FROM match_price_v
INNER JOIN match USING (match_id)
GROUP BY CUBE(season, surface, source)
ORDER BY season DESC, surface NULLS FIRST, source NULLS FIRST;