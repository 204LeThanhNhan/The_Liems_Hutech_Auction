import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import axios from 'axios';
import '../styles/CreateAuction.css';

function CreateAuction() {
  const [user, setUser] = useState(null);
  const [step, setStep] = useState(1);
  const [openNow, setOpenNow] = useState(false);
  const [auctionData, setAuctionData] = useState({
    auctionName: '',
    startTime: '',
    endTime: ''
  });
  const [productData, setProductData] = useState({
    productName: '',
    productDescription: '',
    startPrice: '',
    imageUrls: []
  });
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [validating, setValidating] = useState(false);
  const [validationErrors, setValidationErrors] = useState({
    description: '',
    images: ''
  });
  const [message, setMessage] = useState({ type: '', text: '' });
  const navigate = useNavigate();

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (!userData) {
      navigate('/login');
      return;
    }
    
    const parsedUser = JSON.parse(userData);
    if (parsedUser.role !== 'ADMIN') {
      navigate('/');
      return;
    }
    setUser(parsedUser);
  }, [navigate]);

  const handleAuctionChange = (e) => {
    setAuctionData({
      ...auctionData,
      [e.target.name]: e.target.value
    });
  };

  const handleProductChange = (e) => {
    setProductData({
      ...productData,
      [e.target.name]: e.target.value
    });
    
    // Clear validation errors when user types
    if (e.target.name === 'productDescription' && validationErrors.description) {
      setValidationErrors(prev => ({ ...prev, description: '' }));
    }
  };

  const handleOpenNowToggle = () => {
    setOpenNow(!openNow);
  };

  const handleFileChange = async (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;
    
    // Validate file sizes
    for (const file of files) {
      const isVideo = file.type.startsWith('video/');
      const maxSize = isVideo ? 50 * 1024 * 1024 : 10 * 1024 * 1024; // 50MB for video, 10MB for image
      
      if (file.size > maxSize) {
        showToast(
          `File "${file.name}" quá lớn! Giới hạn: ${isVideo ? '50MB' : '10MB'}`,
          'error'
        );
        e.target.value = '';
        return;
      }
    }
    
    setUploading(true);
    const uploadedUrls = [];

    try {
      for (const file of files) {
        let fileToUpload = file;
        
        // Resize image if it's an image (not video)
        if (file.type.startsWith('image/')) {
          fileToUpload = await resizeImage(file);
        }
        
        const formData = new FormData();
        formData.append('file', fileToUpload);
        
        const response = await axios.post('http://localhost:8080/api/upload/avatar', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        uploadedUrls.push(response.data.url);
      }
      
      setProductData({
        ...productData,
        imageUrls: [...productData.imageUrls, ...uploadedUrls]
      });
      
      showToast(`✓ Upload ${uploadedUrls.length} file thành công!`, 'success');
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.response?.data || 'Upload file thất bại!';
      showToast(errorMsg, 'error');
      console.error('Upload error:', error);
    } finally {
      setUploading(false);
      // Reset file input
      e.target.value = '';
    }
  };

  const resizeImage = (file) => {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          const MAX_WIDTH = 1200;
          const MAX_HEIGHT = 1200;
          let width = img.width;
          let height = img.height;

          // Calculate new dimensions
          if (width > height) {
            if (width > MAX_WIDTH) {
              height *= MAX_WIDTH / width;
              width = MAX_WIDTH;
            }
          } else {
            if (height > MAX_HEIGHT) {
              width *= MAX_HEIGHT / height;
              height = MAX_HEIGHT;
            }
          }

          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, width, height);

          // Convert to blob with compression
          canvas.toBlob((blob) => {
            const resizedFile = new File([blob], file.name, {
              type: 'image/jpeg',
              lastModified: Date.now()
            });
            resolve(resizedFile);
          }, 'image/jpeg', 0.8); // 80% quality
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(file);
    });
  };

  const handleRemoveImage = (indexToRemove) => {
    setProductData({
      ...productData,
      imageUrls: productData.imageUrls.filter((_, index) => index !== indexToRemove)
    });
    
    // Clear image validation errors when user removes images
    if (validationErrors.images) {
      setValidationErrors(prev => ({ ...prev, images: '' }));
    }
  };

  const handleNextStep = () => {
    if (!auctionData.auctionName || (!openNow && !auctionData.startTime)) {
      showToast('Vui lòng điền đầy đủ thông tin!', 'error');
      return;
    }
    setStep(2);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Clear previous validation errors
    setValidationErrors({ description: '', images: '' });
    
    if (!productData.productName || !productData.startPrice || productData.imageUrls.length === 0) {
      showToast('Vui lòng điền đầy đủ thông tin sản phẩm và upload ảnh!', 'error');
      return;
    }

    // Show validation modal
    setValidating(true);

    try {
      // Step 1: Validate product content
      const validationResponse = await axios.post('http://localhost:8080/api/validate/product', {
        auctionName: auctionData.auctionName,
        productName: productData.productName,
        productDescription: productData.productDescription,
        imageUrls: productData.imageUrls
      });

      const validationResult = validationResponse.data;

      // Check if validation passed
      if (!validationResult.valid) {
        setValidating(false);
        
        // Set error messages
        const errors = { description: '', images: '' };
        
        if (!validationResult.textValid) {
          errors.description = '⚠️ Phát hiện nội dung không phù hợp trong mô tả sản phẩm. Vui lòng kiểm tra lại!';
        }
        
        if (!validationResult.imageValid) {
          errors.images = `⚠️ ${validationResult.imageMessage || 'Phát hiện vật phẩm cấm trong hình ảnh. Vui lòng kiểm tra lại!'}`;
        }
        
        setValidationErrors(errors);
        showToast('Sản phẩm không hợp lệ! Vui lòng kiểm tra lại thông tin.', 'error');
        return;
      }

      // Step 2: If validation passed, create auction
      const requestData = {
        userId: user.userId,
        auctionName: auctionData.auctionName,
        startTime: openNow ? null : auctionData.startTime,
        endTime: auctionData.endTime || null,
        productName: productData.productName,
        productDescription: productData.productDescription,
        startPrice: parseFloat(productData.startPrice),
        imageUrls: productData.imageUrls,
        openNow: openNow
      };

      const response = await axios.post('http://localhost:8080/api/auctions', requestData);
      setValidating(false);
      showToast('Tạo phiên đấu giá thành công!', 'success');
      
      if (openNow) {
        // Show countdown modal
        showCountdownModal(response.data);
      } else {
        setTimeout(() => {
          navigate('/');
        }, 2000);
      }
    } catch (error) {
      setValidating(false);
      showToast(error.response?.data || 'Tạo phiên đấu giá thất bại!', 'error');
    }
  };

  const showCountdownModal = (auctionData) => {
    let countdown = 30;
    const modal = document.createElement('div');
    modal.className = 'countdown-modal';
    modal.innerHTML = `
      <div class="countdown-modal-content">
        <h2>🎉 Phiên đấu giá đã được tạo!</h2>
        <p>Phiên sẽ bắt đầu sau:</p>
        <div class="countdown-timer-large">
          <span id="countdown-value">${countdown}</span>
          <span class="countdown-label">giây</span>
        </div>
        <p class="countdown-info">Đang chuẩn bị phiên đấu giá...</p>
        <button id="close-countdown-modal" class="btn-close-countdown">
          Đóng và về trang chủ
        </button>
      </div>
    `;
    document.body.appendChild(modal);
    
    const countdownInterval = setInterval(() => {
      countdown--;
      const countdownElement = document.getElementById('countdown-value');
      if (countdownElement) {
        countdownElement.textContent = countdown;
      }
      
      if (countdown <= 0) {
        clearInterval(countdownInterval);
        if (document.body.contains(modal)) {
          document.body.removeChild(modal);
        }
        navigate('/');
      }
    }, 1000);
    
    // Add close button handler
    setTimeout(() => {
      const closeBtn = document.getElementById('close-countdown-modal');
      if (closeBtn) {
        closeBtn.onclick = () => {
          clearInterval(countdownInterval);
          if (document.body.contains(modal)) {
            document.body.removeChild(modal);
          }
          navigate('/');
        };
      }
    }, 100);
  };

  const showToast = (text, type) => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = text;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 100);
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
  };

  if (!user) {
    return <div>Loading...</div>;
  }

  return (
    <div className="create-auction-page">
      <Navbar />
      
      <div className="create-auction-container">
        <h2>Mở Phiên Đấu Giá Mới</h2>
        
        <div className="steps-indicator">
          <div className={`step-item ${step >= 1 ? 'active' : ''}`}>
            <div className="step-number">1</div>
            <span>Thông tin phiên</span>
          </div>
          <div className="step-line"></div>
          <div className={`step-item ${step >= 2 ? 'active' : ''}`}>
            <div className="step-number">2</div>
            <span>Thông tin sản phẩm</span>
          </div>
        </div>

        {message.text && (
          <div className={`alert alert-${message.type}`}>
            {message.text}
          </div>
        )}

        {step === 1 && (
          <div className="form-card">
            <h3>Bước 1: Thông tin phiên đấu giá</h3>
            
            <div className="form-group">
              <label>Tên phiên đấu giá *</label>
              <input
                type="text"
                name="auctionName"
                value={auctionData.auctionName}
                onChange={handleAuctionChange}
                placeholder="VD: Đấu giá iPhone 15 Pro Max"
                required
              />
            </div>

            <div className="open-now-section">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={openNow}
                  onChange={handleOpenNowToggle}
                />
                <span>Mở ngay (phiên sẽ bắt đầu sau 30 giây kể từ khi tạo)</span>
              </label>
            </div>

            {!openNow && (
              <div className="form-group">
                <label>Thời gian bắt đầu *</label>
                <input
                  type="datetime-local"
                  name="startTime"
                  value={auctionData.startTime}
                  onChange={handleAuctionChange}
                  required
                />
              </div>
            )}

            <div className="form-group">
              <label>Thời gian kết thúc</label>
              <input
                type="datetime-local"
                name="endTime"
                value={auctionData.endTime}
                onChange={handleAuctionChange}
              />
              <small style={{color: '#999', fontSize: '0.9rem'}}>Không bắt buộc - Để trống nếu không có thời gian kết thúc cố định</small>
            </div>

            <button onClick={handleNextStep} className="btn-next">
              Tiếp theo →
            </button>
          </div>
        )}

        {step === 2 && (
          <div className="form-card">
            <h3>Bước 2: Thông tin sản phẩm</h3>
            
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Tên sản phẩm *</label>
                <input
                  type="text"
                  name="productName"
                  value={productData.productName}
                  onChange={handleProductChange}
                  placeholder="VD: iPhone 15 Pro Max 256GB"
                  required
                />
              </div>

              <div className="form-group">
                <label>Mô tả sản phẩm *</label>
                <textarea
                  name="productDescription"
                  value={productData.productDescription}
                  onChange={handleProductChange}
                  placeholder="Mô tả chi tiết về sản phẩm..."
                  rows="5"
                  required
                />
                {validationErrors.description && (
                  <div className="validation-error">
                    {validationErrors.description}
                  </div>
                )}
              </div>

              <div className="form-group">
                <label>Giá khởi điểm (VND) *</label>
                <input
                  type="number"
                  name="startPrice"
                  value={productData.startPrice}
                  onChange={handleProductChange}
                  placeholder="VD: 25000000"
                  required
                  min="0"
                />
              </div>

              <div className="form-group">
                <label>Hình ảnh/Video sản phẩm <span className='text-destructive'>Bắt buộc</span> </label>
                <div className="image-upload-section">
                  <label htmlFor="product-images" className="file-upload-label">
                    {uploading ? (
                      <span>⏳ Đang tải file...</span>
                    ) : (
                      <>
                        <span>📁 Chọn ảnh/video để upload</span><br />
                        <span className="upload-hint">
                          JPG, PNG (tối đa 10MB) | MP4 (tối đa 50MB)<br />
                          Có thể chọn nhiều file cùng lúc
                        </span>
                      </>
                    )}
                  </label>
                  <input
                    id="product-images"
                    type="file"
                    accept="image/jpeg,image/jpg,image/png,video/mp4"
                    multiple
                    onChange={handleFileChange}
                    className="file-input-hidden"
                    disabled={uploading}
                  />
                </div>
                
                {productData.imageUrls.length > 0 && (
                  <div className="uploaded-images">
                    <p className="uploaded-count">✓ Đã upload {productData.imageUrls.length} file</p>
                    <div className="image-preview-grid">
                      {productData.imageUrls.map((url, index) => {
                        const isVideo = url.includes('/video/') || url.endsWith('.mp4') || url.includes('.mp4');
                        return (
                          <div key={index} className="image-preview-item">
                            {isVideo ? (
                              <video src={url} controls className="preview-media" />
                            ) : (
                              <img src={url} alt={`Product ${index + 1}`} className="preview-media" />
                            )}
                            <button
                              type="button"
                              className="btn-remove-image"
                              onClick={() => handleRemoveImage(index)}
                              title="Xóa"
                            >
                              ✕
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
                
                {validationErrors.images && (
                  <div className="validation-error">
                    {validationErrors.images}
                  </div>
                )}
              </div>

              <div className="form-actions">
                <button type="button" onClick={() => setStep(1)} className="btn-back-auction">
                  Quay lại
                </button>
                <button type="submit" className="btn-submit-auction" disabled={validating}>
                  {validating ? (
                    <>
                      <span className="spinner"></span>
                      Đang kiểm tra...
                    </>
                  ) : (
                    'Tạo phiên đấu giá'
                  )}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
      
      {/* Validation Modal */}
      {validating && (
        <div className="validation-modal">
          <div className="validation-modal-content">
            <div className="validation-spinner"></div>
            <h3>Đang kiểm tra thông tin sản phẩm</h3>
            <p>Vui lòng chờ trong giây lát...</p>
            <div className="validation-steps">
              <div className="validation-step">
                <span>Kiểm tra nội dung mô tả...</span>
              </div>
              <div className="validation-step">
                <span>Phân tích hình ảnh sản phẩm...</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CreateAuction;
