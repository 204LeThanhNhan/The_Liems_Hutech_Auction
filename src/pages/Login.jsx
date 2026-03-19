import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import axios from 'axios';
import '../styles/Auth.css';

function Login() {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', formData);
      
      localStorage.setItem('user', JSON.stringify(response.data));
      setSuccess('Đăng nhập thành công! Đang chuyển hướng...');
      
      setTimeout(() => {
        navigate('/');
        window.location.reload();
      }, 1500);
    } catch (err) {
      setError(err.response?.data || 'Đăng nhập thất bại!');
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <Navbar />
      
      <div className="auth-container">
        <div className="auth-card">
          <h2>Đăng Nhập</h2>
          
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}
          
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Tên đăng nhập</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label>Mật khẩu</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>
            
            <button type="submit" className="btn-submit" disabled={loading}>
              {loading ? (
                <>
                  <span className="spinner"></span>
                  Đang đăng nhập...
                </>
              ) : (
                'Đăng nhập'
              )}
            </button>
          </form>
          
          <p className="auth-link">
            Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;
