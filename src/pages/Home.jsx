import Navbar from '../components/Navbar';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import axios from 'axios';
import Countdown from 'react-countdown';
import '../styles/HomeNew.css';

function Home() {
  const navigate = useNavigate();
  const [auctions, setAuctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentImageIndex, setCurrentImageIndex] = useState({});
  const [stats, setStats] = useState({
    totalAuctions: 0,
    activeAuctions: 0,
    totalUsers: 0,
    totalBids: 0
  });

  useEffect(() => {
    fetchAuctions();
    calculateStats();
  }, []);

  const fetchAuctions = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/auctions');
      const latestAuctions = response.data
        .sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
        .slice(0, 8);
      setAuctions(latestAuctions);
      
      const initialIndex = {};
      latestAuctions.forEach(auction => {
        initialIndex[auction.auctionId] = 0;
      });
      setCurrentImageIndex(initialIndex);
    } catch (error) {
      console.error('Error fetching auctions:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/auctions');
      const activeCount = response.data.filter(a => a.status === 'ACTIVE').length;
      const totalBids = response.data.reduce((sum, a) => sum + (a.totalBids || 0), 0);
      
      setStats({
        totalAuctions: response.data.length,
        activeAuctions: activeCount,
        totalUsers: 1250,
        totalBids: totalBids
      });
    } catch (error) {
      console.error('Error calculating stats:', error);
    }
  };

  const nextImage = (auctionId, totalImages) => {
    setCurrentImageIndex(prev => ({
      ...prev,
      [auctionId]: (prev[auctionId] + 1) % totalImages
    }));
  };

  const prevImage = (auctionId, totalImages) => {
    setCurrentImageIndex(prev => ({
      ...prev,
      [auctionId]: (prev[auctionId] - 1 + totalImages) % totalImages
    }));
  };

  const countdownRenderer = ({ days, hours, minutes, seconds, completed }) => {
    if (completed) {
      return <span className="time">Đã bắt đầu</span>;
    }
    return (
      <div className="countdown-display">
        {days > 0 && (
          <div className="countdown-item">
            <span className="countdown-number">{days}</span>
            <span className="countdown-label">ngày</span>
          </div>
        )}
        {hours > 0 && (
          <div className="countdown-item">
            <span className="countdown-number">{hours}</span>
            <span className="countdown-label">giờ</span>
          </div>
        )}
        {minutes > 0 && (
          <div className="countdown-item">
            <span className="countdown-number">{minutes}</span>
            <span className="countdown-label">phút</span>
          </div>
        )}
        {(seconds > 0 || (days === 0 && hours === 0 && minutes === 0)) && (
          <div className="countdown-item">
            <span className="countdown-number">{seconds}</span>
            <span className="countdown-label">giây</span>
          </div>
        )}
      </div>
    );
  };

  const handleCardClick = (auction) => {
    const now = new Date();
    const startTime = new Date(auction.startTime);
    
    // Nếu phiên đã bắt đầu (startTime < now), dẫn vào BiddingRoom
    if (startTime < now) {
      navigate(`/bidding/${auction.auctionId}`);
    } else {
      // Nếu chưa bắt đầu (startTime > now), dẫn vào AuctionDetail
      navigate(`/auction/${auction.auctionId}`);
    }
  };


  return (
    <div className="home">
      <Navbar />
      
      {/* Hero Banner */}
      <section className="hero-banner">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">
              Khám phá <span className="highlight">Đấu giá</span><br />
              Trực tuyến Hàng đầu
            </h1>
            <p className="hero-subtitle">
              Tham gia hàng nghìn phiên đấu giá với sản phẩm chất lượng cao.<br />
              Đấu giá công bằng, minh bạch và an toàn.
            </p>
            <div className="hero-buttons">
              <button 
                className="btn-primary-hero" 
                onClick={() => {
                  const user = localStorage.getItem('user');
                  navigate(user ? '/auctions' : '/register');
                }}
              >
                🔥 Đấu giá ngay
              </button>
            </div>
          </div>
          <div className="hero-image">
            <div className="floating-card card-1">
              <span className="card-icon">🏆</span>
              <div className="card-text">
                <strong>1,250+</strong>
                <span>Người dùng</span>
              </div>
            </div>
            <div className="floating-card card-2">
              <span className="card-icon">⚡</span>
              <div className="card-text">
                <strong>{stats.activeAuctions}</strong>
                <span>Đang diễn ra</span>
              </div>
            </div>
            <div className="floating-card card-3">
              <span className="card-icon">💰</span>
              <div className="card-text">
                <strong>{stats.totalBids}+</strong>
                <span>Lượt đấu giá</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="stats-section">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-item">
              <div className="stat-icon">📊</div>
              <div className="stat-number">{stats.totalAuctions}+</div>
              <div className="stat-label">Phiên đấu giá</div>
            </div>
            <div className="stat-item">
              <div className="stat-icon">👥</div>
              <div className="stat-number">1,250+</div>
              <div className="stat-label">Người dùng</div>
            </div>
            <div className="stat-item">
              <div className="stat-icon">🔴</div>
              <div className="stat-number">{stats.activeAuctions}</div>
              <div className="stat-label">Đang diễn ra</div>
            </div>
            <div className="stat-item">
              <div className="stat-icon">⚡</div>
              <div className="stat-number">{stats.totalBids}+</div>
              <div className="stat-label">Lượt đấu giá</div>
            </div>
          </div>
        </div>
      </section>


{/* Latest Auctions Section */}
      <section className="latest-auctions-section">
        <div className="container">
          <div className="section-header">
            <h2>Phiên đấu giá mới nhất</h2>
            <p>Tham gia ngay để không bỏ lỡ cơ hội</p>
          </div>
          
          {loading ? (
            <div className="loading-state">
              <div className="spinner"></div>
              <p>Đang tải...</p>
            </div>
          ) : auctions.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">📦</span>
              <p>Chưa có phiên đấu giá nào</p>
            </div>
          ) : (
            <div className="auctions-grid-main">
              {auctions.map((auction) => (
                <div 
                  key={auction.auctionId} 
                  className="auction-card-main"
                  onClick={() => handleCardClick(auction)}
                >
                  <div className="auction-image-main">
                    {auction.imageUrls && auction.imageUrls.length > 0 ? (
                      <>
                        {auction.imageUrls[currentImageIndex[auction.auctionId] || 0].includes('/video/') || 
                         auction.imageUrls[currentImageIndex[auction.auctionId] || 0].endsWith('.mp4') ||
                         auction.imageUrls[currentImageIndex[auction.auctionId] || 0].includes('.mp4') ? (
                          <video 
                            src={auction.imageUrls[currentImageIndex[auction.auctionId] || 0]} 
                            className="auction-media"
                            controls
                            muted
                            loop
                            playsInline
                            onClick={(e) => e.stopPropagation()}
                          />
                        ) : (
                          <img 
                            src={auction.imageUrls[currentImageIndex[auction.auctionId] || 0]} 
                            alt={auction.productName}
                            className="auction-media"
                          />
                        )}
                        
                        {auction.imageUrls.length > 1 && (
                          <>
                            <button 
                              className="carousel-btn carousel-prev"
                              onClick={(e) => {
                                e.stopPropagation();
                                prevImage(auction.auctionId, auction.imageUrls.length);
                              }}
                            >
                              ‹
                            </button>
                            <button 
                              className="carousel-btn carousel-next"
                              onClick={(e) => {
                                e.stopPropagation();
                                nextImage(auction.auctionId, auction.imageUrls.length);
                              }}
                            >
                              ›
                            </button>
                            <div className="carousel-indicators">
                              {auction.imageUrls.map((_, index) => (
                                <span 
                                  key={index}
                                  className={`indicator ${index === (currentImageIndex[auction.auctionId] || 0) ? 'active' : ''}`}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setCurrentImageIndex(prev => ({...prev, [auction.auctionId]: index}));
                                  }}
                                />
                              ))}
                            </div>
                          </>
                        )}
                      </>
                    ) : (
                      <div className="auction-media-placeholder">
                        <span style={{fontSize: '3rem'}}>📦</span>
                      </div>
                    )}
                    {auction.status === 'ACTIVE' && (
                      <div className="live-badge">🔴 LIVE</div>
                    )}
                  </div>
                  <div className="auction-content">
                    <h3 className="auction-title-main">{auction.productName}</h3>
                    <p className="auction-name">{auction.auctionName}</p>
                    <div className="auction-info">
                      <div className="info-item row">
                        <span className="label-price">
                          {auction.status === 'ACTIVE' ? 'Giá hiện tại' : 'Giá khởi điểm'}
                        </span>
                        <span className="value-price">
                          {(auction.currentPrice || auction.startPrice)?.toLocaleString('vi-VN')} ₫
                        </span>
                      </div>
                      {auction.status === 'PENDING' && (
                        <div className="info-item">
                          <span className="label-price">Bắt đầu sau</span>
                          <Countdown 
                            date={new Date(auction.startTime)} 
                            renderer={countdownRenderer}
                          />
                        </div>
                      )}
                      {auction.status === 'ACTIVE' && (
                        <>
                          <div className="info-item row">
                            <span className="label-price">Số lượt đấu giá</span>
                            <span className="bids-count-inline">{auction.totalBids || 0} lượt</span>
                          </div>
                        </>
                      )}
                    </div>
                    <div className="auction-footer">
                      <button className="btn-join-auction">
                        {auction.status === 'PENDING' ? 'Xem chi tiết' : 'Tham gia đấu giá'}
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
          
          <div className="view-all-section">
            <button className="btn-view-all" onClick={() => navigate('/auctions')}>
              Xem tất cả phiên đấu giá →
            </button>
          </div>
        </div>
      </section>

      {/* Customer Testimonials */}
      <section className="testimonials-section-new">
        <div className="container">
          <div className="section-header">
            <h2>Đánh giá từ khách hàng</h2>
            <p>Những gì khách hàng nói về chúng tôi</p>
          </div>
          
          <div className="testimonials-carousel-container">
            <button 
              className="testimonial-nav testimonial-nav-prev"
              onClick={() => {
                const carousel = document.querySelector('.testimonials-grid-new');
                carousel.scrollBy({ left: -carousel.offsetWidth, behavior: 'smooth' });
              }}
            >
              ‹
            </button>

            <div className="testimonials-grid-new">
              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Sàn đấu giá rất uy tín và chuyên nghiệp. Tôi đã mua được chiếc đồng hồ Rolex với giá tốt. 
                  Giao dịch nhanh chóng, minh bạch. Rất hài lòng với dịch vụ!"
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=12" alt="Nguyễn Văn A" className="author-avatar" />
                  <div className="author-info">
                    <h4>Nguyễn Văn A</h4>
                    <p>Khách hàng thân thiết</p>
                  </div>
                </div>
              </div>

              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Giao diện đẹp, dễ sử dụng. Hệ thống đếm ngược rất trực quan. 
                  Đã tham gia nhiều phiên đấu giá và luôn cảm thấy công bằng, minh bạch."
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=45" alt="Trần Thị B" className="author-avatar" />
                  <div className="author-info">
                    <h4>Trần Thị B</h4>
                    <p>Người mua thường xuyên</p>
                  </div>
                </div>
              </div>

              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Tôi là người bán hàng trên sàn. Quy trình tạo phiên đấu giá rất đơn giản. 
                  Hỗ trợ nhiệt tình, thanh toán nhanh. Đây là nơi tuyệt vời để kinh doanh!"
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=33" alt="Lê Minh C" className="author-avatar" />
                  <div className="author-info">
                    <h4>Lê Minh C</h4>
                    <p>Người bán chuyên nghiệp</p>
                  </div>
                </div>
              </div>

              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Sản phẩm đa dạng, từ điện tử đến đồ cổ. Giá cả hợp lý, 
                  cạnh tranh lành mạnh. Đã giới thiệu cho nhiều bạn bè sử dụng!"
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=68" alt="Phạm Thị D" className="author-avatar" />
                  <div className="author-info">
                    <h4>Phạm Thị D</h4>
                    <p>Khách hàng VIP</p>
                  </div>
                </div>
              </div>

              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Hệ thống bảo mật tốt, thông tin cá nhân được bảo vệ. 
                  Đấu giá công bằng, không gian lận. Đây là sàn đấu giá đáng tin cậy nhất!"
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=15" alt="Hoàng Văn E" className="author-avatar" />
                  <div className="author-info">
                    <h4>Hoàng Văn E</h4>
                    <p>Thành viên từ 2024</p>
                  </div>
                </div>
              </div>

              <div className="testimonial-card-new">
                <div className="testimonial-rating">
                  <span className="stars">⭐⭐⭐⭐⭐</span>
                  <span className="rating-text">5.0</span>
                </div>
                <p className="testimonial-content">
                  "Dịch vụ khách hàng xuất sắc! Mọi thắc mắc đều được giải đáp nhanh chóng. 
                  Sàn đấu giá đáng tin cậy cho cả người mua và người bán."
                </p>
                <div className="testimonial-author">
                  <img src="https://i.pravatar.cc/150?img=25" alt="Võ Thị F" className="author-avatar" />
                  <div className="author-info">
                    <h4>Võ Thị F</h4>
                    <p>Người dùng mới</p>
                  </div>
                </div>
              </div>
            </div>

            <button 
              className="testimonial-nav testimonial-nav-next"
              onClick={() => {
                const carousel = document.querySelector('.testimonials-grid-new');
                carousel.scrollBy({ left: carousel.offsetWidth, behavior: 'smooth' });
              }}
            >
              ›
            </button>
          </div>
        </div>
      </section>

      

      {/* How It Works */}
      <section className="how-it-works">
        <div className="container">
          <div className="section-header">
            <h2>Cách thức hoạt động</h2>
            <p>Đấu giá dễ dàng chỉ với 3 bước đơn giản</p>
          </div>
          <div className="steps-grid">
            <div className="step-card">
              <div className="step-number">01</div>
              <div className="step-icon">📝</div>
              <h3>Đăng ký tài khoản</h3>
              <p>Tạo tài khoản miễn phí và xác thực thông tin của bạn</p>
            </div>
            <div className="step-card">
              <div className="step-number">02</div>
              <div className="step-icon">🔍</div>
              <h3>Tìm sản phẩm</h3>
              <p>Duyệt qua hàng nghìn sản phẩm đấu giá chất lượng cao</p>
            </div>
            <div className="step-card">
              <div className="step-number">03</div>
              <div className="step-icon">🎯</div>
              <h3>Đấu giá & Thắng</h3>
              <p>Đặt giá và theo dõi phiên đấu giá real-time</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <div className="container">
          <div className="cta-content">
            <h2>Sẵn sàng bắt đầu đấu giá?</h2>
            <p>Tham gia cộng đồng đấu giá trực tuyến lớn nhất Việt Nam</p>
            {localStorage.getItem('user') ? (
              <button className="btn-cta" onClick={() => navigate('/auctions')}>
                Đấu giá ngay
              </button>
            ) : (
              <button className="btn-cta" onClick={() => navigate('/register')}>
                Đăng ký miễn phí ngay
              </button>
            )}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer-main">
        <div className="container">
          <div className="footer-content-main">
            <div className="footer-col">
              <h3 className="footer-logo">HUTECH AUCTION</h3>
              <p>Sàn đấu giá trực tuyến uy tín hàng đầu Việt Nam</p>
              <div className="footer-contact">
                <p>hutech.auction@hutech.edu.vn</p>
                <p>028 5445 7777</p>
                <p>475A Điện Biên Phủ, P.25, Q.Bình Thạnh, TP.HCM</p>
              </div>
            </div>
            <div className="footer-col">
              <h4>Danh Mục</h4>
              <ul>
                <li><a href="#">Điện tử</a></li>
                <li><a href="#">Thời trang</a></li>
                <li><a href="#">Đồng hồ</a></li>
                <li><a href="#">Phụ kiện</a></li>
              </ul>
            </div>
            <div className="footer-col">
              <h4>Chính Sách</h4>
              <ul>
                <li><a href="#">Điều khoản sử dụng</a></li>
                <li><a href="#">Chính sách bảo mật</a></li>
                <li><a href="#">Quy định đấu giá</a></li>
                <li><a href="#">Hướng dẫn thanh toán</a></li>
              </ul>
            </div>
            <div className="footer-col">
              <h4>Đăng Ký Nhận Tin</h4>
              <p className="newsletter-desc">
                Nhận thông báo về các phiên đấu giá mới nhất
              </p>
              <div className="newsletter-form">
                <input type="email" placeholder="Email của bạn" />
                <button>GỬI</button>
              </div>
            </div>
          </div>
          <div className="footer-bottom-main">
            <p>© 2026 HUTECH AUCTION - Đại học Công nghệ TP.HCM (HUTECH). All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default Home;
