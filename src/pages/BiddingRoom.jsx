import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';
import Countdown from 'react-countdown';
import Navbar from '../components/Navbar';
import AuctionCountdown from '../components/AuctionCountdown';
import GridLayout from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import '../styles/BiddingRoom.css';

function BiddingRoom() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [auction, setAuction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const [customBidAmount, setCustomBidAmount] = useState('');
  const [bidHistory, setBidHistory] = useState([]);
  const [showCustomBid, setShowCustomBid] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  
  // Floating bidding panel state
  const [biddingPanelPos, setBiddingPanelPos] = useState(() => {
    const saved = localStorage.getItem('biddingPanelPos');
    return saved ? JSON.parse(saved) : { x: 50, y: 100 };
  });
  const [biddingPanelSize, setBiddingPanelSize] = useState(() => {
    const saved = localStorage.getItem('biddingPanelSize');
    return saved ? JSON.parse(saved) : { width: 400, height: 400 };
  });
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [chatMessage, setChatMessage] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [isChatCollapsed, setIsChatCollapsed] = useState(false);
  const [isHistoryCollapsed, setIsHistoryCollapsed] = useState(false);
  const [isBiddingPanelCollapsed, setIsBiddingPanelCollapsed] = useState(false);
  
  const stompClientRef = useRef(null);
  const bidHistoryRef = useRef(null);
  const carouselIntervalRef = useRef(null);
  const currentPriceRef = useRef(null); // Track current price in ref for immediate access
  const chatMessagesRef = useRef(null);

  // Default layout configuration - Fixed height 600px, horizontal resize only
  const defaultLayout = [
    { i: 'chat', x: 0, y: 0, w: 3, h: 20, minW: 1, maxW: 8, static: false },
    { i: 'product', x: 3, y: 0, w: 6, h: 20, minW: 3, maxW: 10, static: false },
    { i: 'history', x: 9, y: 0, w: 3, h: 20, minW: 1, maxW: 8, static: false }
  ];



  const [layout, setLayout] = useState(() => {
    const savedLayout = localStorage.getItem('biddingRoomLayout');
    return savedLayout ? JSON.parse(savedLayout) : defaultLayout;
  });

  const onLayoutChange = (newLayout) => {
    setLayout(newLayout);
    localStorage.setItem('biddingRoomLayout', JSON.stringify(newLayout));
    
    // Auto-collapse items that are too small (width < 100px)
    newLayout.forEach(item => {
      const widthInPx = (item.w / 12) * 1400; // Calculate actual width
      
      if (item.i === 'chat' && widthInPx < 100 && !isChatCollapsed) {
        setIsChatCollapsed(true);
      } else if (item.i === 'chat' && widthInPx >= 100 && isChatCollapsed) {
        setIsChatCollapsed(false);
      }
      
      if (item.i === 'history' && widthInPx < 100 && !isHistoryCollapsed) {
        setIsHistoryCollapsed(true);
      } else if (item.i === 'history' && widthInPx >= 100 && isHistoryCollapsed) {
        setIsHistoryCollapsed(false);
      }
    });
  };

  const resetLayout = () => {
    setLayout(defaultLayout);
    localStorage.removeItem('biddingRoomLayout');
    setBiddingPanelPos({ x: 50, y: 100 });
    setBiddingPanelSize({ width: 400, height: 400 });
    localStorage.removeItem('biddingPanelPos');
    localStorage.removeItem('biddingPanelSize');
    setIsChatCollapsed(false);
    setIsHistoryCollapsed(false);
  };

  const toggleChat = () => {
    setIsChatCollapsed(!isChatCollapsed);
    updateLayoutOnToggle(!isChatCollapsed, isHistoryCollapsed);
  };

  const toggleHistory = () => {
    setIsHistoryCollapsed(!isHistoryCollapsed);
    updateLayoutOnToggle(isChatCollapsed, !isHistoryCollapsed);
  };

  const updateLayoutOnToggle = (chatCollapsed, historyCollapsed) => {
    if (chatCollapsed && historyCollapsed) {
      // Both collapsed - product full width
      setLayout([
        { i: 'product', x: 0, y: 0, w: 12, h: 17, minW: 4, minH: 17, maxH: 17 }
      ]);
    } else if (chatCollapsed && !historyCollapsed) {
      // Only chat collapsed - product + history
      setLayout([
        { i: 'product', x: 0, y: 0, w: 9, h: 17, minW: 4, minH: 17, maxH: 17 },
        { i: 'history', x: 9, y: 0, w: 3, h: 17, minW: 2, minH: 17, maxH: 17 }
      ]);
    } else if (!chatCollapsed && historyCollapsed) {
      // Only history collapsed - chat + product
      setLayout([
        { i: 'chat', x: 0, y: 0, w: 3, h: 17, minW: 2, minH: 17, maxH: 17 },
        { i: 'product', x: 3, y: 0, w: 9, h: 17, minW: 4, minH: 17, maxH: 17 }
      ]);
    } else {
      // Both expanded - default layout
      setLayout(defaultLayout);
    }
  };

  // Dragging handlers for floating panel
  const handleMouseDown = (e) => {
    if (e.target.closest('.bidding-panel-header')) {
      setIsDragging(true);
      setDragOffset({
        x: e.clientX - biddingPanelPos.x,
        y: e.clientY - biddingPanelPos.y
      });
    }
  };

  const handleMouseMove = (e) => {
    if (isDragging) {
      const newPos = {
        x: e.clientX - dragOffset.x,
        y: e.clientY - dragOffset.y
      };
      setBiddingPanelPos(newPos);
      localStorage.setItem('biddingPanelPos', JSON.stringify(newPos));
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
      return () => {
        window.removeEventListener('mousemove', handleMouseMove);
        window.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDragging, dragOffset]);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (!userData) {
      navigate('/login');
      return;
    }
    setUser(JSON.parse(userData));
    
    fetchAuctionDetails();
    fetchBidHistory();
    fetchRecentChatMessages();
    connectWebSocket();
    
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      if (carouselIntervalRef.current) {
        clearInterval(carouselIntervalRef.current);
      }
    };
  }, [id]);

  // Auto carousel effect
  useEffect(() => {
    if (auction?.imageUrls && auction.imageUrls.length > 1) {
      carouselIntervalRef.current = setInterval(() => {
        setCurrentImageIndex(prev => (prev + 1) % auction.imageUrls.length);
      }, 5000);
      
      return () => {
        if (carouselIntervalRef.current) {
          clearInterval(carouselIntervalRef.current);
        }
      };
    }
  }, [auction?.imageUrls]);

  const nextImage = () => {
    if (auction?.imageUrls) {
      setCurrentImageIndex(prev => (prev + 1) % auction.imageUrls.length);
      resetCarouselTimer();
    }
  };

  const prevImage = () => {
    if (auction?.imageUrls) {
      setCurrentImageIndex(prev => (prev - 1 + auction.imageUrls.length) % auction.imageUrls.length);
      resetCarouselTimer();
    }
  };

  const resetCarouselTimer = () => {
    if (carouselIntervalRef.current) {
      clearInterval(carouselIntervalRef.current);
      carouselIntervalRef.current = setInterval(() => {
        setCurrentImageIndex(prev => (prev + 1) % auction.imageUrls.length);
      }, 5000);
    }
  };

  const fetchAuctionDetails = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/auctions/${id}`);
      console.log('auction details:', response.data);
      setAuction(response.data);
      // Initialize currentPriceRef
      const initialPrice = response.data.currentPrice || response.data.startPrice;
      currentPriceRef.current = initialPrice;
    } catch (error) {
      console.error('Error fetching auction:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchBidHistory = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/auctions/${id}/bids`);

      setBidHistory(response.data.slice(0, 20));
    } catch (error) {
      console.error('Error fetching bid history:', error);
      console.error('Error details:', error.response?.data);
    }
  };

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Connected to WebSocket');
        setConnected(true);
        
        // Subscribe to bid updates
        client.subscribe(`/topic/auction/${id}`, (message) => {
          const response = JSON.parse(message.body);
          handleBidUpdate(response);
        });
        
        // Subscribe to chat messages
        client.subscribe(`/topic/auction/${id}/chat`, (message) => {
          const chatResponse = JSON.parse(message.body);
          handleChatMessage(chatResponse);
        });
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });
    
    client.activate();
    stompClientRef.current = client;
  };

  const handleBidUpdate = (response) => {
    console.log('=== BID UPDATE START ===');
    console.log('Bid response received:', response);
    console.log('Response data type:', typeof response.data?.currentPrice);
    console.log('Response data value:', response.data?.currentPrice);
    
    if (response.success) {
      // Parse currentPrice - might be BigDecimal object from Java
      let newPrice = response.data.currentPrice;
      
      // If it's an object with value property (BigDecimal), extract the value
      if (typeof newPrice === 'object' && newPrice !== null) {
        console.log('currentPrice is object:', newPrice);
        newPrice = parseFloat(newPrice) || newPrice.value || newPrice;
      }
      
      // Ensure it's a number
      newPrice = typeof newPrice === 'number' ? newPrice : parseFloat(newPrice);
      
      console.log('Old price (state):', auction?.currentPrice);
      console.log('Old price (ref):', currentPriceRef.current);
      console.log('New price (parsed):', newPrice);
      
      // Update ref immediately for next bid calculation
      currentPriceRef.current = newPrice;
      console.log('Updated ref to:', currentPriceRef.current);
      
      // Update auction data with new current price
      setAuction(prev => {
        const updated = {
          ...prev,
          currentPrice: newPrice,
          totalBids: response.data.totalBids,
          highestBidderId: response.data.userId
        };
        console.log('Updated auction state:', updated);
        return updated;
      });
      
      // Add to bid history
      setBidHistory(prev => [response.data, ...prev].slice(0, 20));
      
      // Show notification
      if (response.data.userId !== user?.userId) {
        showNotification(`${response.data.username} đã đấu giá ${formatPrice(response.data.bidAmount)}`);
      } else {
        showNotification('Đấu giá thành công!', 'success');
      }
      
      // Scroll bid history to top
      if (bidHistoryRef.current) {
        bidHistoryRef.current.scrollTop = 0;
      }
      
      console.log('=== BID UPDATE END ===');
    } else {
      const errorMsg = response.message || 'Đấu giá thất bại. Vui lòng thử lại.';
      console.error('Bid failed:', errorMsg);
      showNotification(errorMsg, 'error');
    }
  };

  const placeBid = (amount, type) => {
    console.log('Attempting to place bid:', { amount, type, connected, userId: user?.userId });
    
    if (!connected || !stompClientRef.current) {
      showNotification('Chưa kết nối đến server', 'error');
      return;
    }
    
    if (!user) {
      showNotification('Vui lòng đăng nhập', 'error');
      return;
    }
    
    const bidRequest = {
      auctionId: parseInt(id),
      userId: user.userId,
      amount: amount,
      type: type
    };
    
    console.log('Sending bid request:', bidRequest);
    
    stompClientRef.current.publish({
      destination: `/app/auction/${id}/bid`,
      body: JSON.stringify(bidRequest)
    });
    
    setShowCustomBid(false);
    setCustomBidAmount('');
  };

  const handleQuickBid = (increment) => {
    // Use ref for immediate access to latest price (not affected by React state delay)
    const priceFromRef = currentPriceRef.current;
    const priceFromState = auction?.currentPrice || auction?.startPrice || 0;
    
    console.log('=== QUICK BID START ===');
    console.log('Increment:', increment);
    console.log('Price from ref:', priceFromRef);
    console.log('Price from state:', priceFromState);
    console.log('Auction object:', auction);
    
    // Use the higher value to be safe
    const currentPrice = Math.max(priceFromRef || 0, priceFromState);
    const bidAmount = currentPrice + increment;
    
    console.log('Final current price used:', currentPrice);
    console.log('Bid amount to send:', bidAmount);
    console.log('=== QUICK BID END ===');
    
    placeBid(bidAmount, 'quick');
  };

  const handleSendMessage = () => {
    if (!chatMessage.trim()) return;
    
    if (chatMessage.length > 100) {
      showNotification('Tin nhắn không được quá 100 ký tự', 'error');
      return;
    }
    
    if (!connected || !stompClientRef.current) {
      showNotification('Chưa kết nối đến server', 'error');
      return;
    }
    
    if (!user) {
      showNotification('Vui lòng đăng nhập', 'error');
      return;
    }
    
    const chatRequest = {
      auctionId: parseInt(id),
      userId: user.userId,
      message: chatMessage.trim()
    };
    
    console.log('Sending chat message:', chatRequest);
    
    stompClientRef.current.publish({
      destination: `/app/auction/${id}/chat`,
      body: JSON.stringify(chatRequest)
    });
    
    setChatMessage('');
  };

  const handleChatMessage = (chatResponse) => {
    console.log('Chat message received:', chatResponse);
    
    setChatMessages(prev => [...prev, chatResponse].slice(-100)); // Keep last 100 messages
    
    // Auto scroll to bottom
    setTimeout(() => {
      if (chatMessagesRef.current) {
        chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
      }
    }, 100);
  };

  const fetchRecentChatMessages = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/auctions/${id}/chat/recent?limit=50`);
      setChatMessages(response.data);
      
      // Scroll to bottom after loading
      setTimeout(() => {
        if (chatMessagesRef.current) {
          chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
        }
      }, 100);
    } catch (error) {
      console.error('Error fetching chat messages:', error);
    }
  };

  const handleCustomBid = () => {
    const amount = parseFloat(customBidAmount);
    const currentPrice = auction.currentPrice || auction.startPrice;
    
    if (isNaN(amount) || amount <= currentPrice) {
      showNotification(`Giá đặt phải lớn hơn ${formatPrice(currentPrice)}`, 'error');
      return;
    }
    
    placeBid(amount, 'custom');
  };

  const showNotification = (message, type = 'info') => {
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 100);
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  const countdownRenderer = ({ days, hours, minutes, seconds, completed }) => {
    if (completed) {
      return <span className="countdown-ended">Đã kết thúc</span>;
    }
    return (
      <span className="countdown-time">
        {days > 0 && `${days}d `}
        {hours}:{minutes.toString().padStart(2, '0')}:{seconds.toString().padStart(2, '0')}
      </span>
    );
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="loading-container">Đang tải...</div>
      </div>
    );
  }

  if (!auction) {
    return (
      <div>
        <Navbar />
        <div className="error-container">Không tìm thấy phiên đấu giá</div>
      </div>
    );
  }

  // Calculate current price - will update when auction state changes
  const currentPrice = auction.currentPrice || auction.startPrice;
  const isHighestBidder = user && auction.highestBidderId === user.userId;
  
  console.log('Render - Current price:', currentPrice, 'Auction state:', auction.currentPrice);

  return (
    <div className="bidding-room">
      <Navbar />
      
      {/* Auction Countdown Component */}
      <AuctionCountdown 
        auctionId={auction.auctionId}
        currentPrice={currentPrice}
        connected={connected}
      />
      
      <div className="bidding-room-header">
        <button className="reset-layout-btn" onClick={resetLayout}>
          Đặt lại bố cục
        </button>
        <button 
          className={`toggle-panel-btn ${isChatCollapsed ? 'collapsed' : ''}`}
          onClick={toggleChat}
        >
          💬 {isChatCollapsed ? 'Mở Chat' : 'Đóng Chat'}
        </button>
        <button 
          className={`toggle-panel-btn ${isHistoryCollapsed ? 'collapsed' : ''}`}
          onClick={toggleHistory}
        >
          📜 {isHistoryCollapsed ? 'Mở Lịch sử' : 'Đóng Lịch sử'}
        </button>
        <button 
          className={`toggle-panel-btn ${isBiddingPanelCollapsed ? 'collapsed' : ''}`}
          onClick={() => setIsBiddingPanelCollapsed(!isBiddingPanelCollapsed)}
        >
          💰 {isBiddingPanelCollapsed ? 'Mở Đấu giá' : 'Đóng Đấu giá'}
        </button>
        <span className="layout-hint">💡 Kéo thả các khối để tùy chỉnh giao diện</span>
      </div>

      {/* Collapsed Icons */}
      {isChatCollapsed && (
        <div className="collapsed-icon collapsed-chat" onClick={toggleChat}>
          💬
        </div>
      )}
      {isHistoryCollapsed && (
        <div className="collapsed-icon collapsed-history" onClick={toggleHistory}>
          📜
        </div>
      )}

      <GridLayout
        className="bidding-grid-layout"
        layout={layout}
        cols={12}
        rowHeight={30}
        width={1400}
        onLayoutChange={onLayoutChange}
        draggableHandle=".drag-handle"
        compactType="horizontal"
        verticalCompact={false}
        preventCollision={true}
        allowOverlap={false}
        isResizable={true}
        resizeHandles={['e', 'w']}
        containerPadding={[0, 0]}
        margin={[5, 0]}
        maxRows={20}
      >
        {/* Chat Box */}
        {!isChatCollapsed && (
          <div key="chat" className="grid-item chat-box">
            <div className="drag-handle">
              <span className="drag-icon">⋮⋮</span>
              <h3>💬 Chat phòng đấu giá</h3>
            </div>
            <div className="chat-messages" ref={chatMessagesRef}>
              {chatMessages.length === 0 ? (
                <div className="chat-placeholder">
                  Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!
                </div>
              ) : (
                chatMessages.map((msg, index) => {
                  const isMyMessage = user && msg.userId === user.userId;
                  const messageDate = new Date(msg.messageTime);
                  const today = new Date();
                  const isToday = messageDate.toDateString() === today.toDateString();
                  
                  const timeDisplay = isToday 
                    ? messageDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
                    : messageDate.toLocaleString('vi-VN', { 
                        day: '2-digit', 
                        month: '2-digit', 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      });
                  
                  // Assign color based on userId (4 colors cycling)
                  const userColorClass = `user-color-${msg.userId % 4}`;
                  
                  return (
                    <div 
                      key={msg.messageId || index} 
                      className={`chat-message ${isMyMessage ? 'chat-message-mine' : userColorClass}`}
                    >
                      {!isMyMessage && (
                        msg.avatarURL ? (
                          <img src={msg.avatarURL} alt={msg.displayName} className="chat-avatar" />
                        ) : (
                          <div className="chat-avatar" style={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'white',
                            fontSize: '0.8rem',
                            fontWeight: '600'
                          }}>
                            {(msg.displayName || msg.username).charAt(0).toUpperCase()}
                          </div>
                        )
                      )}
                      <div style={{flex: 1, maxWidth: '70%'}}>
                        <div className="chat-message-header">
                          {!isMyMessage && (
                            <span className="chat-username">{msg.displayName || msg.username}</span>
                          )}
                          <span className="chat-time">{timeDisplay}</span>
                        </div>
                        <div className="chat-message-content">{msg.message}</div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
            <div className="chat-input-container">
              <input
                type="text"
                value={chatMessage}
                onChange={(e) => setChatMessage(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                placeholder="Nhập tin nhắn (tối đa 100 ký tự)..."
                className="chat-input"
                maxLength={100}
              />
              <span className={`chat-char-count ${chatMessage.length > 90 ? 'warning' : ''}`}>
                {chatMessage.length}/100
              </span>
              <button onClick={handleSendMessage} className="chat-send-btn">
                Gửi
              </button>
            </div>
          </div>
        )}

        {/* Product Info */}
        <div key="product" className="grid-item product-section">
          <div className="drag-handle">
            <span className="drag-icon">⋮⋮</span>
            <h3>📦 Thông tin sản phẩm</h3>
          </div>
          <div className="product-content">
            <div className="product-image-carousel">
              {auction.imageUrls && auction.imageUrls.length > 0 ? (
                <>
                  {auction.imageUrls[currentImageIndex].includes('/video/') || 
                   auction.imageUrls[currentImageIndex].endsWith('.mp4') ||
                   auction.imageUrls[currentImageIndex].includes('.mp4') ? (
                    <video 
                      src={auction.imageUrls[currentImageIndex]} 
                      className="carousel-media"
                      controls
                      muted
                      loop
                      playsInline
                    />
                  ) : (
                    <img 
                      src={auction.imageUrls[currentImageIndex]} 
                      alt={auction.productName}
                      className="carousel-media"
                    />
                  )}
                  
                  {auction.imageUrls.length > 1 && (
                    <>
                      <button className="carousel-btn-left" onClick={prevImage}>
                        ‹
                      </button>
                      <button className="carousel-btn-right" onClick={nextImage}>
                        ›
                      </button>
                      <div className="carousel-dots">
                        {auction.imageUrls.map((_, index) => (
                          <span 
                            key={index}
                            className={`dot ${index === currentImageIndex ? 'active' : ''}`}
                            onClick={() => {
                              setCurrentImageIndex(index);
                              resetCarouselTimer();
                            }}
                          />
                        ))}
                      </div>
                    </>
                  )}
                </>
              ) : (
                <div className="no-image">📦 Không có hình ảnh</div>
              )}
            </div>
            <div className="product-info">
              <h1 className="product-name">{auction.productName}</h1>
              <p className="auction-name-text">{auction.auctionName}</p>
              {auction.productDescription && (
                <p className="product-description-text">{auction.productDescription}</p>
              )}
            </div>
          </div>
        </div>

        {/* Bid History */}
        {!isHistoryCollapsed && (
          <div key="history" className="grid-item bid-history-sidebar">
            <div className="drag-handle">
              <span className="drag-icon">⋮⋮</span>
              <h3>📜 Lịch sử đấu giá</h3>
            </div>
          <div className="bid-history-list" ref={bidHistoryRef}>
            {bidHistory.length === 0 ? (
              <div className="no-bids">Chưa có lượt đấu giá nào</div>
            ) : (
              bidHistory.map((bid, index) => (
                <div key={bid.bidId || index} className="bid-card">
                  <div className="bid-card-top">
                    <div className="bid-user-info">
                      {bid.avatarURL ? (
                        <img src={bid.avatarURL} alt={bid.displayName} className="bid-avatar-small" />
                      ) : (
                        <div className="bid-avatar-placeholder">👤</div>
                      )}
                      <span className="bid-username-small">{bid.displayName || bid.username}</span>
                    </div>
                    <div className="bid-amount-highlight">
                      {formatPrice(bid.bidAmount)}
                    </div>
                  </div>
                  <div className="bid-card-bottom">
                    <span className="bid-time-small">
                      {new Date(bid.bidTime).toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                      })}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
        )}
      </GridLayout>

      {/* Floating Bidding Panel - Hide when collapsed */}
      {!isBiddingPanelCollapsed && (
        <div 
          className="floating-bidding-panel"
          style={{
            left: `${biddingPanelPos.x}px`,
            top: `${biddingPanelPos.y}px`,
            width: `${biddingPanelSize.width}px`,
            height: `${biddingPanelSize.height}px`
          }}
          onMouseDown={handleMouseDown}
        >
          <div className="bidding-panel-header">
            <span className="drag-icon">⋮⋮</span>
            <h3>💰 Đấu giá</h3>
            <span className={`status-dot-mini ${connected ? 'connected' : 'disconnected'}`}></span>
          </div>
          <div className="bidding-panel-content" style={{ display: 'flex' }}>
          {/* Compact Info - Move to top */}
          <div className="panel-info-compact">
            <span className="info-compact">
              <strong>{auction.totalBids || 0}</strong> lượt
            </span>
            {auction.endTime && (
              <>
                <span className="info-separator">•</span>
                <span className="info-compact">
                  <Countdown date={new Date(auction.endTime)} renderer={countdownRenderer} />
                </span>
              </>
            )}
          </div>

          {/* Current Price Box */}
          <fieldset className="price-fieldset">
            <legend>Giá hiện tại</legend>
            <div className="current-price-display">
              {formatPrice(currentPrice)}
            </div>
            {auction.highestBidderId && (
              <div className="bidder-status">
                {isHighestBidder ? (
                  <span className="status-leading">🏆 Bạn đang dẫn đầu</span>
                ) : (
                  <span className="status-normal">Người khác đang dẫn đầu</span>
                )}
              </div>
            )}
          </fieldset>

          {/* Quick Bid Buttons - Always show unless auction ended */}
          {auction.status !== 'COMPLETED' && (
            <>
              <div className="quick-bids-row">
                <button 
                  className="quick-bid-btn-new quick-bid-green"
                  onClick={() => handleQuickBid(500000)}
                >
                  +500K
                </button>
                <button 
                  className="quick-bid-btn-new quick-bid-yellow"
                  onClick={() => handleQuickBid(1000000)}
                >
                  +1TR
                </button>
                <button 
                  className="quick-bid-btn-new quick-bid-red"
                  onClick={() => handleQuickBid(2000000)}
                >
                  +2TR
                </button>
              </div>

              {/* Custom Bid Input */}
              <div className="custom-bid-row">
                <input
                  type="number"
                  value={customBidAmount}
                  onChange={(e) => setCustomBidAmount(e.target.value)}
                  placeholder="Nhập giá đấu..."
                  className="custom-bid-input-new"
                />
                <button 
                  onClick={handleCustomBid} 
                  className="bid-submit-btn"
                >
                  Đấu giá
                </button>
              </div>
            </>
          )}
        </div>
      </div>
      )}
    </div>
  );
}

export default BiddingRoom;
