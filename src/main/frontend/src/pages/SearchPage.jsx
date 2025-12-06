import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Row, Col, Card } from 'react-bootstrap';

function SearchCard({ icon, color, title, description, onClick }) {
  return (
    <Col md={4} className="mb-4">
      <Card 
        onClick={onClick}
        style={{ border: 'none', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)', cursor: 'pointer' }}
      >
        <Card.Body className="text-center p-4">
          <div 
            style={{ 
              fontSize: '40px', 
              color: color, 
              marginBottom: '15px',
              lineHeight: '1', 
            }}
          >
            {icon}
          </div>
          <Card.Title style={{ fontWeight: 'bold', fontSize: '1.1em' }}>{title}</Card.Title>
          <Card.Text className="text-muted" style={{ fontSize: '0.85em' }}>
            {description}
          </Card.Text>
        </Card.Body>
      </Card>
    </Col>
  );
}

function SearchPage() {
  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate(-1);
  };
  
  const handlePlaylistSearchClick = () => {
    navigate('/search/playlist');
  };

  const handleSongSearchClick = () => {
    navigate('/search/song');
  };

  const handleArtistSearchClick = () => {
    navigate('/search/artist');
  };

  return (
    <Container style={{ maxWidth: '900px' }}>
      <div className="mb-4 d-flex align-items-center">
        <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
          ← 뒤로가기
        </Button>
      </div>
      
      <div className="text-center mb-5">
        <h2 className="mb-2" style={{ fontWeight: 'bold' }}>검색하기</h2>
        <p className="text-muted" style={{ fontSize: '1em' }}>검색 유형을 선택하세요</p>
      </div>

      <Row className="justify-content-center">
        <SearchCard 
          icon="📋" 
          color="#9370DB" 
          title="플레이리스트 검색" 
          description="제목, 악곡 수, 댓글 수, 소유자 검색"
          onClick={handlePlaylistSearchClick}
        />
        <SearchCard 
          icon="🎵" 
          color="#3CB371" 
          title="악곡 검색" 
          description="곡명, 아티스트, 제공원 검색"
          onClick={handleSongSearchClick}
        />
        <SearchCard 
          icon="🧑‍🎤" 
          color="#BA55D3" 
          title="아티스트 검색" 
          description="이름, 성별, 역할 검색"
          onClick={handleArtistSearchClick}
        />
      </Row>
    </Container>
  );
}

export default SearchPage;