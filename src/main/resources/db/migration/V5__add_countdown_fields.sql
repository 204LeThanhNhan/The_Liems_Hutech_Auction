-- Add countdown mechanism fields to auction table
ALTER TABLE auction ADD COLUMN CountdownStartTime DATETIME NULL;
ALTER TABLE auction ADD COLUMN CountdownRound INT DEFAULT 0;
ALTER TABLE auction ADD COLUMN CountdownStatus VARCHAR(20) NULL;
ALTER TABLE auction ADD COLUMN ExtensionCount INT DEFAULT 0;
ALTER TABLE auction ADD COLUMN OriginalEndTime DATETIME NULL;

-- Add index for better query performance
CREATE INDEX idx_auction_countdown_status ON auction(CountdownStatus);
CREATE INDEX idx_auction_status_countdown ON auction(Status, CountdownStatus);
