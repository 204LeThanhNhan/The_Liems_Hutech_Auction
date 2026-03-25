-- Remove unused columns from auction table
ALTER TABLE auction 
DROP COLUMN IF EXISTS highest_bidder_id,
DROP COLUMN IF EXISTS min_bid_increment;
