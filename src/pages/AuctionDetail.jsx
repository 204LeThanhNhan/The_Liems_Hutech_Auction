import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Countdown from 'react-countdown';
import Navbar from '../components/Navbar';
import '../styles/AuctionDetail.css';

function AuctionDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [auction, setAuction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    fetchAuctionDetail();
  }, [id]);

  const fetchAuctionDetail = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/auctions/${id}`);
      setAuction(response.data);
    } catch (error) {
      console.error('Error fetching auction detail:', error);
    } finally {
      setLoading(false);
    }
  };

  const countdownRenderer = ({ days, hours, minutes, seconds, completed }) => {
    if (completed) {
      return <div className="countdown-completed">Phiên đấu giá đã bắt đầu!</div>;
    }
    
    return (
      <div className="countdown-big-display">
        {days > 0 && (
          <div className="countdown-big-item">
            <div className="countdown-big-number">{days}</div>
            <div className="countdown-big-label">Ngày</div>
          </div>
        )}
        <div className="countdown-big-item">
          <div className="countdown-big-number">{hours}</div>
          <div className="countdown-big-label">Giờ</div>
        </div>
        <div className="countdown-big-item">
          <div className="countdown-big-number">{minutes}</div>
          <div className="countdown-big-label">Phút</div>
        </div>
        <div className="countdown-big-item">
          <div className="countdown-big-number">{seconds}</div>
          <div className="countdown-big-label">Giây</div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="loading-container">
          <p>Đang tải...</p>
        </div>
      </div>
    );
  }

  if (!auction) {
    return (
      <div>
        <Navbar />
        <div className="error-container">
          <p>Không tìm thấy phiên đấu giá</p>
          <button onClick={() => navigate('/')}>Quay lại trang chủ</button>
        </div>
      </div>
    );
  }

  return (
    <div className="auction-detail-page">
      <Navbar />
      
      <div className="auction-detail-container">
        <button className="btn-back" onClick={() => navigate('/')}>
          ← Quay lại
        </button>

        <div className="auction-detail-content">
          {/* Image Gallery */}
          <div className="auction-gallery">
            <div className="main-image">
              {auction.imageUrls && auction.imageUrls.length > 0 ? (
                <>
                  {auction.imageUrls[currentImageIndex].includes('/video/') || 
                   auction.imageUrls[currentImageIndex].endsWith('.mp4') ||
                   auction.imageUrls[currentImageIndex].includes('.mp4') ? (
                    <video 
                      src={auction.imageUrls[currentImageIndex]} 
                      controls
                      className="gallery-media"
                      playsInline
                    />
                  ) : (
                    <img 
                      src={auction.imageUrls[currentImageIndex]} 
                      alt={auction.productName}
                      className="gallery-media"
                    />
                  )}
                </>
              ) : (
                <div className="gallery-placeholder">
                  <span style={{fontSize: '5rem'}}>📦</span>
                </div>
              )}
            </div>
            
            {auction.imageUrls && auction.imageUrls.length > 1 && (
              <div className="thumbnail-list">
                {auction.imageUrls.map((url, index) => (
                  <div 
                    key={index}
                    className={`thumbnail ${index === currentImageIndex ? 'active' : ''}`}
                    onClick={() => setCurrentImageIndex(index)}
                  >
                    {url.includes('/video/') || url.endsWith('.mp4') || url.includes('.mp4') ? (
                      <video src={url} className="thumbnail-media" />
                    ) : (
                      <img src={url} alt={`Thumbnail ${index + 1}`} className="thumbnail-media" />
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Auction Info */}
          <div className="auction-info-section">
            <h1 className="auction-detail-title">{auction.productName}</h1>
            <p className="auction-detail-name">{auction.auctionName}</p>
            
            {auction.description && (
              <div className="auction-description">
                <h3>Mô tả</h3>
                <p>{auction.description}</p>
              </div>
            )}

            <div className="auction-price-info">
              <div className="price-item">
                <span className="price-label">Giá khởi điểm</span>
                <span className="price-value">{auction.startPrice?.toLocaleString('vi-VN')} ₫</span>
              </div>
              {auction.status === 'ACTIVE' && auction.currentPrice && (
                <div className="price-item">
                  <span className="price-label">Giá hiện tại</span>
                  <span className="price-value current">{auction.currentPrice?.toLocaleString('vi-VN')} ₫</span>
                </div>
              )}
            </div>

            {/* Countdown for PENDING auctions */}
            {auction.status === 'PENDING' && (
              <div className="countdown-section">
                <h3>Phiên đấu giá bắt đầu sau</h3>
                <Countdown 
                  date={new Date(auction.startTime)} 
                  renderer={countdownRenderer}
                />
              </div>
            )}

            {/* Auction Details */}
            <div className="auction-details">
              <h3>Thông tin phiên đấu giá</h3>
              <div className="detail-row">
                <span className="detail-label">Thời gian bắt đầu:</span>
                <span className="detail-value">{new Date(auction.startTime).toLocaleString('vi-VN')}</span>
              </div>
              {auction.endTime && (
                <div className="detail-row">
                  <span className="detail-label">Thời gian kết thúc:</span>
                  <span className="detail-value">{new Date(auction.endTime).toLocaleString('vi-VN')}</span>
                </div>
              )}
              <div className="detail-row">
                <span className="detail-label">Trạng thái:</span>
                <span className={`status-badge ${auction.status.toLowerCase()}`}>
                  {auction.status === 'PENDING' ? 'Sắp bắt đầu' : 
                   auction.status === 'ACTIVE' ? 'Đang diễn ra' : 'Đã kết thúc'}
                </span>
              </div>
              {auction.status === 'ACTIVE' && (
                <div className="detail-row">
                  <span className="detail-label">Số lượt đấu giá:</span>
                  <span className="detail-value">{auction.totalBids || 0} lượt</span>
                </div>
              )}
            </div>

            {/* User Info */}
            {auction.user && (
              <div className="user-info-section">
                <h3>Người tổ chức</h3>
                <div className="user-card">
                  {auction.user.avatarURL && (
                    <img src={auction.user.avatarURL} alt={auction.user.username} className="user-avatar" />
                  )}
                  <div className="user-details">
                    <p className="user-name">{auction.user.username}</p>
                    <p className="user-email">{auction.user.email}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Action Button */}
            <div className="action-section">
              {auction.status === 'PENDING' && (
                <button className="btn-notify">
                  🔔 Nhận thông báo khi bắt đầu
                </button>
              )}
              {auction.status === 'ACTIVE' && (
                <button 
                  className="btn-join-bid"
                  onClick={() => navigate(`/bidding/${auction.auctionId}`)}
                >
                  Tham gia đấu giá ngay
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuctionDetail;
