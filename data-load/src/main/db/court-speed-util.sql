SELECT 10 * floor((court_speed + 5) / 10) speed, count(*) count, round(100.0 * count(*) / count, 1) pct,
	round(100.0 * avg(ace_pct)::NUMERIC, 1) ace_pct, round(100.0 * avg(sv_pts_won_pct)::NUMERIC - 50, 1) sv_pts_won_pct, round(100.0 * avg(sv_gms_won_pct)::NUMERIC - 50, 1) sv_gms_won_pct
FROM event_stats, (SELECT count(*) count FROM event_stats) event_count
GROUP BY ROLLUP(speed), count
ORDER BY speed;

SELECT round(avg(court_speed)::NUMERIC, 2) avg,
	round(100.0 * count(*) FILTER (WHERE court_speed < 50)::NUMERIC / count(*), 2) bottom,
	round(100.0 * count(*) FILTER (WHERE court_speed >= 50)::NUMERIC / count(*), 2) top
FROM event_stats;