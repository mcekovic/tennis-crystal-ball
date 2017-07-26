ALTER TABLE grand_slam_goat_points ADD COLUMN grand_slam_on_same_event INTEGER;

UPDATE grand_slam_goat_points SET grand_slam_on_same_event = 1;

ALTER TABLE grand_slam_goat_points ALTER COLUMN grand_slam_on_same_event SET NOT NULL;