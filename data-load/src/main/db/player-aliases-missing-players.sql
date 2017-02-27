DELETE FROM player_alias;
INSERT INTO player_alias
(alias, name)
VALUES
   ('Andre Thielemans', 'Andre Tielemans'),
   ('Axel Geuer', 'Alex Geuer'),
   ('Albert Ramos Vinolas', 'Albert Ramos'),
   ('William Freer', 'Bill Freer'),
   ('Brendan OShea', 'Bill Oshea'),
   ('Diego Schwartzman', 'Diego Sebastian Schwartzman'),
   ('Duckhee Lee', 'Duck Hee Lee'),
   ('Eugene Cantin', 'Eugene T Cantin'),
   ('Frances Tiafoe', 'Francis Tiafoe'),
   ('Franko Skugor', 'Franco Skugor'),
   ('Fred Hemmes Sr', 'Fred Hemmes'),
   ('Hans Joachim Ploetz', 'Hans Jaochim Plotz'),
   ('Harry Sheridan', 'H Sheridan'),
   ('Jose Edison Mandarino', 'Jose Mandarino'),
   ('Inigo Cervantes', 'Inigo Cervantes Huegun'),
   ('Ivor Warwick', 'Ivor J Warwick'),
   ('Jaime Fillol Sr', 'Jaime Fillol'),
   ('Jamie Presslie', 'James Pressly'),
   ('John Satchwell Smith', 'Sj Smith'),
   ('Jose Statham', 'Jose Rubin Statham'),
   ('Juan Gisbert Sr', 'Juan Gisbert'),
   ('Junjo Kawamuri', 'Junzo Kawamori'),
   ('Marcelo Lara', 'Marcello Lara'),
   ('Mubarak Shannan Zayid', 'Mubarak Zaid'),
   ('Freddy Field', 'N Field'),
   ('Pancho JF Guzman', 'Pancho Guzman'),
   ('Patricio Rodriguez', 'Patricio Rodriguez Chi'),
   ('Sam Groth', 'Samuel Groth'),
   ('Stan Wawrinka', 'Stanislas Wawrinka'),
   ('Taylor Fritz', 'Taylor Harry Fritz'),
   ('Thomas Koch', 'Thomaz Koch'),
   ('Victor Estrella Burgos', 'Victor Estrella'),
   ('Victor Palman', 'Viktor Palman');

DO $$ BEGIN

PERFORM create_player('Cecil', 'Pedlow', NULL, 'IRL');
PERFORM create_player('Harry', 'Barniville', NULL, 'IRL');
PERFORM create_player('Dennis', 'Foley', NULL, 'IRL');
PERFORM create_player('Louis', 'Pretorius', NULL, 'RSA');
PERFORM create_player('Peter', 'Rigg', DATE '1948-10-25', 'AUS');
PERFORM create_player('Philip', 'Holton', NULL, 'AUS');
PERFORM create_player('Ashley', 'Hewitt', NULL, 'GBR');

END $$;

COMMIT;
