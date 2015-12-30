DO $$ BEGIN

PERFORM load_ranking(DATE '1969-12-29', 'Rod Laver', 1, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Tony Roche', 2, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Tom Okker', 3, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'John Newcombe', 4, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Ken Rosewall', 5, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Andres Gimeno', 6, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Arthur Ashe', 7, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Roy Emerson', 8, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Richard Pancho Gonzales', 9, NULL);
PERFORM load_ranking(DATE '1969-12-29', 'Fred Stolle', 10, NULL);

PERFORM load_ranking(DATE '1970-12-28', 'Rod Laver', 1, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Ken Rosewall', 2, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'John Newcombe', 3, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Arthur Ashe', 4, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Tony Roche', 5, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Cliff Richey', 6, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Stan Smith', 7, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Roger Taylor', 8, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Tom Okker', 9, NULL);
PERFORM load_ranking(DATE '1970-12-28', 'Ilie Nastase', 10, NULL);

PERFORM load_ranking(DATE '1971-12-27', 'Ken Rosewall', 1, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'John Newcombe', 2, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Rod Laver', 3, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Stan Smith', 4, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Arthur Ashe', 5, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Tom Okker', 6, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Ilie Nastase', 7, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Jan Kodes', 8, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Cliff Drysdale', 9, NULL);
PERFORM load_ranking(DATE '1971-12-27', 'Marty Riessen', 10, NULL);

PERFORM load_ranking(DATE '1972-09-25', 'Ken Rosewall', 1, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Rod Laver', 2, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Ilie Nastase', 3, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Arthur Ashe', 4, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Stan Smith', 5, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Tom Okker', 6, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Manuel Orantes', 7, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Cliff Drysdale', 8, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'John Newcombe', 9, NULL);
PERFORM load_ranking(DATE '1972-09-25', 'Marty Riessen', 10, NULL);

END $$;

COMMIT;