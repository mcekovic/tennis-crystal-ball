SELECT count(*) visitors, sum(CASE WHEN active THEN 1 ELSE 0 END) active_visitors,
	sum(hits) hits, avg(hits)::INTEGER hits_per_visit, count(*)::REAL / count(DISTINCT ip_address) visits_per_ip
FROM visitor;

SELECT country, count(*) visitors, sum(CASE WHEN active THEN 1 ELSE 0 END) active_visitors,
	sum(hits) hits, avg(hits)::INTEGER hits_per_visit, count(*)::REAL / count(DISTINCT ip_address) visits_per_ip
FROM visitor
GROUP BY country
ORDER BY visitors DESC, hits DESC;