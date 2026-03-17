import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import AdminRequests from './pages/AdminRequests';
import CreateAuction from './pages/CreateAuction';
import AuctionDetail from './pages/AuctionDetail';
import BiddingRoom from './pages/BiddingRoom';
import AuctionsList from './pages/AuctionsList';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/admin-requests" element={<AdminRequests />} />
        <Route path="/create-auction" element={<CreateAuction />} />
        <Route path="/auction/:id" element={<AuctionDetail />} />
        <Route path="/bidding/:id" element={<BiddingRoom />} />
        <Route path="/auctions" element={<AuctionsList />} />
      </Routes>
    </Router>
  );
}

export default App;
