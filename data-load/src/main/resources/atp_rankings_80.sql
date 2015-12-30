DO $$ BEGIN

PERFORM load_ranking(DATE '1981-12-28', 'John Mcenroe', 1, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Ivan Lendl', 2, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Jimmy Connors', 3, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Bjorn Borg', 4, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Jose Luis Clerc', 5, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Guillermo Vilas', 6, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Gene Mayer', 7, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Eliot Teltscher', 8, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Vitas Gerulaitis', 9, NULL);
PERFORM load_ranking(DATE '1981-12-28', 'Peter Mcnamara', 10, NULL);

PERFORM load_ranking(DATE '1982-12-27', 'John Mcenroe', 1, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Jimmy Connors', 2, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Ivan Lendl', 3, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Guillermo Vilas', 4, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Vitas Gerulaitis', 5, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Jose Luis Clerc', 6, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Mats Wilander', 7, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Gene Mayer', 8, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Yannick Noah', 9, NULL);
PERFORM load_ranking(DATE '1982-12-27', 'Peter Mcnamara', 10, NULL);

PERFORM load_ranking(DATE '1983-12-26', 'John Mcenroe', 1, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Ivan Lendl', 2, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Jimmy Connors', 3, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Mats Wilander', 4, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Yannick Noah', 5, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Jimmy Arias', 6, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Jose Higueras', 7, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Jose Luis Clerc', 8, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Kevin Curren', 9, NULL);
PERFORM load_ranking(DATE '1983-12-26', 'Gene Mayer', 10, NULL);

END $$;

COMMIT;