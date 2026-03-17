import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import '../styles/Navbar.css';

function Navbar() {
  const [user, setUser] = useState(() => {
    const userData = localStorage.getItem('user');
    return userData ? JSON.parse(userData) : null;
  });
  const navigate = useNavigate();

  useEffect(() => {
    // Listen for storage changes from other tabs/windows
    const handleStorageChange = () => {
      const userData = localStorage.getItem('user');
      setUser(userData ? JSON.parse(userData) : null);
    };
    
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <Link to="/" className="navbar-brand">
          <img src="/logo.png" alt="HUTECH AUCTION" />
          HUTECH AUCTION
        </Link>
        
        <ul className="navbar-menu">
          
          {user && user.role === 'ADMIN' && (
            <>
              <li><Link to="/create-auction">Mở phiên đấu giá</Link></li>
              <li><Link to="/admin-requests">Duyệt yêu cầu ADMIN</Link></li>
            </>
          )}
          
          {!user ? (
            <>
              <li><Link to="/login">Đăng nhập</Link></li>
              <li><Link to="/register" className="btn-register">Đăng ký</Link></li>
            </>
          ) : (
            <>
              <li className="user-info">
                <Link to="/profile" className="user-profile-link">
                  <div className="user-avatar">
                    {user.avatarUrl ? (
                      <img src={user.avatarUrl} alt={user.displayName} />
                    ) : (
                      <span>{user.displayName?.charAt(0) || user.username?.charAt(0)}</span>
                    )}
                  </div>
                  <span className="user-name">Xin chào, {user.displayName}</span>
                </Link>
              </li>
              <li><button onClick={handleLogout} className="btn-logout">Đăng xuất</button></li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
}

export default Navbar;
