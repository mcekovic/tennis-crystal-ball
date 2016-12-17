ALTER TABLE grand_slam_goat_points ADD COLUMN grand_slam_holder INTEGER;

UPDATE grand_slam_goat_points SET grand_slam_holder = 4;

ALTER TABLE grand_slam_goat_points ALTER COLUMN grand_slam_holder SET NOT NULL;