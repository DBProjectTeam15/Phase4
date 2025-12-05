import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainLayout from './components/MainLayout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import MyPage from './pages/MyPage.jsx';
import PlaylistPage from './pages/PlaylistPage.jsx';
import PlaylistDetailPage from './pages/PlaylistDetailPage.jsx';
import SearchPage from './pages/SearchPage.jsx';
import PlaylistSearchPage from './pages/PlaylistSearchPage.jsx';
import SongSearchPage from './pages/SongSearchPage.jsx';
import ArtistSearchPage from './pages/ArtistSearchPage.jsx';
import AdminPage from './pages/AdminPage.jsx';
import SongRequestPage from './pages/SongRequestPage.jsx';
import ProviderManagePage from './pages/ProviderManagePage.jsx';
import ArtistManagePage from './pages/ArtistManagePage.jsx';
import ConnectionTestPage from './pages/ConnectionTestPage.jsx';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<LoginPage />} />
          <Route path="mypage" element={<MyPage />} />
          <Route path="playlists" element={<PlaylistPage />} />
          <Route path="playlists/:rank" element={<PlaylistDetailPage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="search/playlist" element={<PlaylistSearchPage />} />
          <Route path="search/song" element={<SongSearchPage />} />
          <Route path="search/artist" element={<ArtistSearchPage />} />
          <Route path="admin" element={<AdminPage />} />
          <Route path="admin/requests" element={<SongRequestPage />} />
          <Route path="admin/providers" element={<ProviderManagePage />} />
          <Route path="admin/artists" element={<ArtistManagePage />} />
          <Route path="test-connection" element={<ConnectionTestPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;