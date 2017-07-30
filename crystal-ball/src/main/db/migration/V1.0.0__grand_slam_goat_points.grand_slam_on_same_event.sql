ALTER TABLE grand_slam_goat_points ADD COLUMN consecutive_grand_slam_on_same_event INTEGER;
ALTER TABLE grand_slam_goat_points ADD COLUMN grand_slam_on_same_event REAL;

UPDATE grand_slam_goat_points SET consecutive_grand_slam_on_same_event = 1;
UPDATE grand_slam_goat_points SET grand_slam_on_same_event = 0.5;

ALTER TABLE grand_slam_goat_points ALTER COLUMN consecutive_grand_slam_on_same_event SET NOT NULL;
ALTER TABLE grand_slam_goat_points ALTER COLUMN grand_slam_on_same_event SET NOT NULL;