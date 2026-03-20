-- Fix null totalBids in auctions table
UPDATE auctions 
SET total_bids = 0 
WHERE total_bids IS NULL;

-- Set default value for future records
ALTER TABLE auctions 
MODIFY COLUMN total_bids INT DEFAULT 0;
