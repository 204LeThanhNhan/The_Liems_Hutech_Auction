import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Countdown from 'react-countdown';
import Navbar from '../components/Navbar';
import '../styles/AuctionsList.css';

function AuctionsList() {
  const navigate = useNavigate();
  const [auctions, setAuctions] = useState([]);
  const [filteredAuctions, setFilteredAuctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // all, pending, active
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  useEffect(() => {
    fetchAuctions();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [auctions, filter, dateFrom, dateTo]);

  const fetchAuctions = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/auctions');
      // Filter only PENDING and ACTIVE auctions
      const activeAuctions = response.data.filter(
        auction => auction.status === 'PENDING' || auction.status === 'ACTIVE'
      );
      setAuctions(activeAuctions);
    } catch (error) {
      console.error('Error fetching auctions:', error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...auctions];

    // Filter by status
    if (filter !== 'all') {
      filtered = filtered.filter(auction => auction.status === filter.toUpperCase());
    }

    // Filter by date range
    if (dateFrom) {
      filtered = filtered.filter(auction => 
        new Date(auction.startTime) >= new Date(dateFrom)
      );
    }
    if (dateTo) {
      filtered = filtered.filter(auction => 
        new Date(auction.startTime) <= new Date(dateTo + 'T23:59:59')
      );
    }

    // Sort by start time
    filtered.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

    setFilteredAuctions(filtered);
  };

  const handleAuctionClick = (auction) => {
    if (auction.status === 'ACTIVE') {
      navigate(`/bidding/${auction.auctionId}`);
    } else {
      navigate(`/auction/${auction.auctionId}`);
    }
  };

  const countdownRenderer = ({ days, hours, minutes, seconds, completed }) => {
    if (completed) {
      return <span className="countdown-text">Đã bắt đầu</span>;
    }
    const parts = [];
    if (days > 0) parts.push(`${days} ngày`);
    if (hours > 0) parts.push(`${hours} giờ`);
    if (minutes > 0) parts.push(`${minutes} phút`);
    if (seconds > 0 || parts.length === 0) parts.push(`${seconds} giây`);
    return <span className="countdown-text">{parts.join(' ')}</span>;
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="loading-container">Đang tải...</div>
      </div>
    );
  }

  return (
    <div className="auctions-list-page">
      <Navbar />
      
      <div className="auctions-list-container">
        <div className="page-header">
          <h1>Phiên Đấu Giá</h1>
          <p>Tham gia đấu giá các sản phẩm hấp dẫn</p>
        </div>

        {/* Filters */}
        <div className="filters-section">
          <div className="filter-group">
            <label>Trạng thái</label>
            <select value={filter} onChange={(e) => setFilter(e.target.value)}>
              <option value="all">Tất cả</option>
              <option value="pending">Sắp bắt đầu</option>
              <option value="active">Đang diễn ra</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Từ ngày</label>
            <input
              type="date"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>Đến ngày</label>
            <input
              type="date"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
            />
          </div>

          <button 
            className="btn-clear-filter"
            onClick={() => {
              setFilter('all');
              setDateFrom('');
              setDateTo('');
            }}
          >
            Xóa bộ lọc
          </button>
        </div>

        {/* Results */}
        <div className="results-info">
          Tìm thấy <strong>{filteredAuctions.length}</strong> phiên đấu giá
        </div>

        {/* Auctions Grid */}
        {filteredAuctions.length === 0 ? (
          <div className="no-results">
            <span style={{fontSize: '3rem'}}>🔍</span>
            <p>Không tìm thấy phiên đấu giá nào</p>
          </div>
        ) : (
          <div className="auctions-grid">
            {filteredAuctions.map((auction) => (
              <div 
                key={auction.auctionId} 
                className="auction-item"
                onClick={() => handleAuctionClick(auction)}
              >
                <div className="auction-image">
                  {auction.imageUrls && auction.imageUrls.length > 0 ? (
                    <img src={auction.imageUrls[0]} alt={auction.productName} />
                  ) : (
                    <div className="image-placeholder">📦</div>
                  )}
                  <div className={`status-badge ${auction.status.toLowerCase()}`}>
                    {auction.status === 'PENDING' ? '⏰ Sắp bắt đầu' : '🔴 Đang diễn ra'}
                  </div>
                </div>

                <div className="auction-info">
                  <h3>{auction.productName}</h3>
                  <p className="auction-name">{auction.auctionName}</p>

                  <div className="price-info">
                    <span className="price-label">
                      {auction.status === 'ACTIVE' ? 'Giá hiện tại' : 'Giá khởi điểm'}
                    </span>
                    <span className="price-value">
                      {formatPrice(auction.currentPrice || auction.startPrice)}
                    </span>
                  </div>

                  {auction.status === 'PENDING' && (
                    <div className="countdown-info">
                      <span className="countdown-label">Bắt đầu sau:</span>
                      <Countdown 
                        date={new Date(auction.startTime)} 
                        renderer={countdownRenderer}
                      />
                    </div>
                  )}

                  {auction.status === 'ACTIVE' && (
                    <div className="bids-info">
                      <span>🔥 {auction.totalBids || 0} lượt đấu giá</span>
                    </div>
                  )}

                  <button className="btn-view-auction">
                    {auction.status === 'ACTIVE' ? 'Tham gia đấu giá' : 'Xem chi tiết'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default AuctionsList;
