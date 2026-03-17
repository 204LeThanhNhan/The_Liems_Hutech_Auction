import { useState, useEffect } from 'react';
import '../styles/AuctionCountdown.css';

function AuctionCountdown({ auctionId, currentPrice, connected }) {
  const [countdownData, setCountdownData] = useState(null);
  const [secondsRemaining, setSecondsRemaining] = useState(0);
  const [round, setRound] = useState(0);

  // Fetch countdown status
  useEffect(() => {
    if (!auctionId) return;

    const fetchCountdownStatus = async () => {
      try {
        const response = await fetch(`http://localhost:8081/api/auctions/${auctionId}/countdown`);
        const data = await response.json();
        setCountdownData(data);
        setSecondsRemaining(data.secondsRemaining || 0);
        setRound(data.countdownRound || 0);
      } catch (error) {
        console.error('Error fetching countdown status:', error);
      }
    };

    fetchCountdownStatus();
    const interval = setInterval(fetchCountdownStatus, 2000); // Poll every 2 seconds

    return () => clearInterval(interval);
  }, [auctionId]);

  // Local countdown timer
  useEffect(() => {
    if (secondsRemaining <= 0) return;

    const timer = setInterval(() => {
      setSecondsRemaining(prev => Math.max(0, prev - 1));
    }, 1000);

    return () => clearInterval(timer);
  }, [secondsRemaining]);

  if (!countdownData) {
    return null;
  }

  const { countdownStatus, message } = countdownData;

  // Calculate progress percentage
  const getMaxSeconds = () => {
    if (round === 1) return 30;
    if (round === 2 || round === 3) return 10;
    return 30;
  };

  const maxSeconds = getMaxSeconds();
  const progressPercent = (secondsRemaining / maxSeconds) * 100;

  // Get status color
  const getStatusColor = () => {
    if (countdownStatus === 'SOLD_FINAL') return '#e74c3c';
    if (countdownStatus === 'SOLD_TEMP') return '#f39c12';
    if (round === 3) return '#e74c3c';
    if (round === 2) return '#f39c12';
    return '#3498db';
  };

  const statusColor = getStatusColor();

  // Format price
  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  return (
    <div className="auction-countdown-container">
      <div className="countdown-card" style={{ borderColor: statusColor }}>
        {/* Current Price */}
        <div className="countdown-price">
          <div className="price-label">Giá hiện tại</div>
          <div className="price-value">{formatPrice(currentPrice || 0)}</div>
        </div>

        {/* Countdown Display */}
        {countdownStatus === 'WAITING' ? (
          <div className="countdown-waiting">
            <div className="waiting-icon">⏳</div>
            <div className="waiting-text">{message}</div>
          </div>
        ) : countdownStatus === 'SOLD_FINAL' ? (
          <div className="countdown-sold-final">
            <div className="sold-icon">🔨</div>
            <div className="sold-text">{message}</div>
          </div>
        ) : countdownStatus === 'SOLD_TEMP' ? (
          <div className="countdown-sold-temp">
            <div className="temp-icon">⏸️</div>
            <div className="temp-text">{message}</div>
          </div>
        ) : (
          <>
            {/* Round Indicator */}
            <div className="countdown-round">
              <span className="round-label">LẦN {round}</span>
              <span className="round-timer">{secondsRemaining}s</span>
            </div>

            {/* Progress Bar */}
            <div className="countdown-progress-container">
              <div 
                className="countdown-progress-bar" 
                style={{ 
                  width: `${progressPercent}%`,
                  backgroundColor: statusColor,
                  transition: 'width 1s linear'
                }}
              />
            </div>

            {/* Status Text */}
            <div className="countdown-status-text" style={{ color: statusColor }}>
              {round === 1 && '30 giây - Lần 1'}
              {round === 2 && '10 giây - Lần 2'}
              {round === 3 && '10 giây - Lần 3 - CUỐI CÙNG!'}
            </div>
          </>
        )}

        {/* Connection Status */}
        {!connected && (
          <div className="countdown-disconnected">
            ⚠️ Mất kết nối
          </div>
        )}
      </div>
    </div>
  );
}

export default AuctionCountdown;
