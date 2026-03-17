import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import axios from 'axios';
import '../styles/Profile.css';

function Profile() {
  const [user, setUser] = useState(null);
  const [formData, setFormData] = useState({
    displayName: '',
    email: '',
    avatarUrl: ''
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [showRequestModal, setShowRequestModal] = useState(false);
  const [requestContent, setRequestContent] = useState('');
  const [hasPendingRequest, setHasPendingRequest] = useState(false);
  const navigate = useNavigate();

  // Toast notification function
  const showToast = (text, type) => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = text;
    document.body.appendChild(toast);
    
    setTimeout(() => {
      toast.classList.add('show');
    }, 100);
    
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => {
        document.body.removeChild(toast);
      }, 300);
    }, 3000);
  };

  const checkPendingRequest = async (userId) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/admin-requests/check/${userId}`);
      setHasPendingRequest(response.data);
    } catch (error) {
      console.error('Error checking pending request:', error);
    }
  };

  const handleRequestAdmin = async () => {
    try {
      await axios.post('http://localhost:8080/api/admin-requests', {
        userId: user.userId,
        content: requestContent
      });
      
      showToast('Gửi yêu cầu thành công!', 'success');
      setShowRequestModal(false);
      setRequestContent('');
      setHasPendingRequest(true);
    } catch (error) {
      showToast(error.response?.data || 'Gửi yêu cầu thất bại!', 'error');
    }
  };

  useEffect(() => {
    const userData = localStorage.getItem('user');
    console.log('Profile - User data from localStorage:', userData);
    
    if (!userData) {
      navigate('/login');
      return;
    }
    
    const parsedUser = JSON.parse(userData);
    console.log('Profile - Parsed user:', parsedUser);
    
    setUser(parsedUser);
    setFormData({
      displayName: parsedUser.displayName || '',
      email: parsedUser.email || '',
      avatarUrl: parsedUser.avatarUrl || ''
    });
    setPreviewUrl(parsedUser.avatarUrl || '');
    
    // Check if user has pending request
    checkPendingRequest(parsedUser.userId);
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Resize image to 150x150 before upload
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          canvas.width = 150;
          canvas.height = 150;
          const ctx = canvas.getContext('2d');
          
          // Draw image with cover fit
          const scale = Math.max(150 / img.width, 150 / img.height);
          const x = (150 / 2) - (img.width / 2) * scale;
          const y = (150 / 2) - (img.height / 2) * scale;
          ctx.drawImage(img, x, y, img.width * scale, img.height * scale);
          
          // Convert canvas to blob
          canvas.toBlob((blob) => {
            const resizedFile = new File([blob], file.name, {
              type: 'image/jpeg',
              lastModified: Date.now()
            });
            setSelectedFile(resizedFile);
            setPreviewUrl(URL.createObjectURL(resizedFile));
          }, 'image/jpeg', 0.9);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUploadAvatar = async () => {
    if (!selectedFile) return;

    setUploading(true);
    const uploadFormData = new FormData();
    uploadFormData.append('file', selectedFile);

    try {
      // Upload to Cloudinary
      const uploadResponse = await axios.post('http://localhost:8080/api/upload/avatar', uploadFormData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      const avatarUrl = uploadResponse.data.url;
      
      // Update user in database
      const updateResponse = await axios.put(
        `http://localhost:8080/api/users/${user.userId}`,
        { avatarUrl: avatarUrl }
      );
      
      // Update localStorage and state
      const updatedUser = {
        ...user,
        avatarUrl: updateResponse.data.avatarUrl
      };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
      
      setFormData({
        ...formData,
        avatarUrl: updateResponse.data.avatarUrl
      });
      
      setSelectedFile(null);
      showToast('Upload và cập nhật avatar thành công!', 'success');
      setMessage({ type: 'success', text: 'Upload và cập nhật avatar thành công!' });
    } catch (error) {
      showToast('Upload avatar thất bại!', 'error');
      setMessage({ type: 'error', text: 'Upload avatar thất bại!' });
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });

    try {
      const response = await axios.put(
        `http://localhost:8080/api/users/${user.userId}`,
        formData
      );
      
      // Update localStorage
      const updatedUser = {
        ...user,
        displayName: response.data.displayName,
        email: response.data.email,
        avatarUrl: response.data.avatarUrl
      };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
      
      showToast('Cập nhật thông tin thành công!', 'success');
      setMessage({ type: 'success', text: 'Cập nhật thông tin thành công!' });
      
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } catch (error) {
      showToast(error.response?.data || 'Cập nhật thất bại!', 'error');
      setMessage({ type: 'error', text: error.response?.data || 'Cập nhật thất bại!' });
    }
  };

  if (!user) {
    console.log('Profile - User is null, showing loading...');
    return (
      <div>
        <Navbar />
        <div style={{padding: '2rem', textAlign: 'center'}}>
          <p>Đang tải thông tin người dùng...</p>
          <p style={{fontSize: '0.9rem', color: '#666', marginTop: '1rem'}}>
            Nếu trang không tải, vui lòng <a href="/login" style={{color: '#2196f3'}}>đăng nhập lại</a>
          </p>
        </div>
      </div>
    );
  }

  console.log('Profile - Rendering profile page for user:', user.username);

  return (
    <div className="profile-page">
      <Navbar />
      
      <div className="profile-container">
        <div className="profile-card">
          <h2>Thông Tin Cá Nhân</h2>
          
          {message.text && (
            <div className={`alert alert-${message.type}`}>
              {message.text}
            </div>
          )}

          <div className="avatar-section">
            <div className="avatar-preview">
              {previewUrl ? (
                <img src={previewUrl} alt="Avatar" />
              ) : (
                <div className="avatar-placeholder">
                  {user.displayName?.charAt(0) || user.username?.charAt(0)}
                </div>
              )}
            </div>
            <div className="avatar-upload">
              <input
                type="file"
                id="avatar-input"
                accept="image/*"
                onChange={handleFileChange}
                style={{ display: 'none' }}
              />
              <label htmlFor="avatar-input" className="btn-choose-file">
                Chọn ảnh
              </label>
              {selectedFile && (
                <button
                  onClick={handleUploadAvatar}
                  disabled={uploading}
                  className="btn-upload"
                >
                  {uploading ? 'Đang tải...' : 'Upload & Lưu'}
                </button>
              )}
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Tên đăng nhập</label>
              <input
                type="text"
                value={user.username}
                disabled
                className="input-disabled"
              />
              <small>Không thể thay đổi tên đăng nhập</small>
            </div>

            <div className="form-group">
              <label>Tên hiển thị</label>
              <input
                type="text"
                name="displayName"
                value={formData.displayName}
                onChange={handleChange}
              />
            </div>

            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Vai trò</label>
              <div style={{display: 'flex', gap: '1rem', alignItems: 'center'}}>
                <input
                  type="text"
                  value={user.role}
                  disabled
                  className="input-disabled"
                  style={{flex: 1}}
                />
                {user.role !== 'ADMIN' && (
                  <div className="admin-request-wrapper">
                    <button
                      type="button"
                      onClick={() => setShowRequestModal(true)}
                      className={`btn-request-admin ${hasPendingRequest ? 'pending' : ''}`}
                      disabled={hasPendingRequest}
                    >
                      {hasPendingRequest ? '⏳ Chờ duyệt' : 'Nâng cấp thành Admin'}
                    </button>
                    <div className="info-icon-wrapper">
                      <span className="info-icon">ℹ️</span>
                      <div className="info-tooltip">
                        <strong>Quyền lợi của Admin:</strong>
                        <ul>
                          <li>Mở phiên đấu giá mới</li>
                          <li>Quản lý sản phẩm đấu giá</li>
                          <li>Theo dõi và kiểm soát các phiên</li>
                          <li>Xem báo cáo thống kê</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>

            <button type="submit" className="btn-submit-profile">
              Cập nhật thông tin
            </button>
          </form>
        </div>
      </div>

      {/* Request Admin Modal */}
      {showRequestModal && (
        <div className="modal-overlay" onClick={() => setShowRequestModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Yêu Cầu Thăng Cấp ADMIN</h3>
            <p className="modal-description">
              Vui lòng cho chúng tôi biết lý do bạn muốn trở thành ADMIN
            </p>
            
            {hasPendingRequest ? (
              <div className="alert alert-info">
                Bạn đã có yêu cầu đang chờ duyệt. Vui lòng đợi admin xét duyệt.
              </div>
            ) : (
              <>
                <textarea
                  className="modal-textarea"
                  placeholder="Nhập lý do của bạn (không bắt buộc)..."
                  value={requestContent}
                  onChange={(e) => setRequestContent(e.target.value)}
                  rows="5"
                />
                
                <div className="modal-actions">
                  <button
                    className="btn-modal-cancel"
                    onClick={() => setShowRequestModal(false)}
                  >
                    Hủy
                  </button>
                  <button
                    className="btn-modal-submit"
                    onClick={handleRequestAdmin}
                  >
                    Gửi yêu cầu
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

export default Profile;
