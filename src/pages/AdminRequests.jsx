import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import axios from 'axios';
import '../styles/AdminRequests.css';

function AdminRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (!userData) {
      navigate('/login');
      return;
    }
    
    const user = JSON.parse(userData);
    if (user.role !== 'ADMIN') {
      navigate('/');
      return;
    }
    
    fetchRequests();
  }, [navigate]);

  const fetchRequests = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin-requests/pending');
      setRequests(response.data);
    } catch (error) {
      console.error('Error fetching requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (requestId) => {
    if (!window.confirm('Bạn có chắc muốn duyệt yêu cầu này?')) return;
    
    try {
      await axios.post(`http://localhost:8080/api/admin-requests/${requestId}/approve`);
      showToast('Đã duyệt yêu cầu thành công!', 'success');
      fetchRequests();
    } catch (error) {
      showToast('Duyệt yêu cầu thất bại!', 'error');
    }
  };

  const handleReject = async (requestId) => {
    if (!window.confirm('Bạn có chắc muốn từ chối yêu cầu này?')) return;
    
    try {
      await axios.post(`http://localhost:8080/api/admin-requests/${requestId}/reject`);
      showToast('Đã từ chối yêu cầu!', 'success');
      fetchRequests();
    } catch (error) {
      showToast('Từ chối yêu cầu thất bại!', 'error');
    }
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

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="admin-requests-page">
      <Navbar />
      
      <div className="admin-requests-container">
        <h2>Duyệt Yêu Cầu Thăng Cấp ADMIN</h2>
        
        {requests.length === 0 ? (
          <div className="no-requests">
            <p>Không có yêu cầu nào đang chờ duyệt</p>
          </div>
        ) : (
          <div className="requests-list">
            {requests.map((request) => (
              <div key={request.requestId} className="request-card">
                <div className="request-header">
                  <div className="user-info-request">
                    <h3>{request.displayName || request.username}</h3>
                    <p className="username">@{request.username}</p>
                    <p className="email">{request.email}</p>
                  </div>
                  <div className="request-date">
                    {new Date(request.requestDate).toLocaleString('vi-VN')}
                  </div>
                </div>
                
                {request.content && (
                  <div className="request-content">
                    <strong>Nội dung yêu cầu:</strong>
                    <p>{request.content}</p>
                  </div>
                )}
                
                <div className="request-actions">
                  <button 
                    className="btn-approve"
                    onClick={() => handleApprove(request.requestId)}
                  >
                    ✓ Duyệt
                  </button>
                  <button 
                    className="btn-reject"
                    onClick={() => handleReject(request.requestId)}
                  >
                    ✗ Từ chối
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

export default AdminRequests;
