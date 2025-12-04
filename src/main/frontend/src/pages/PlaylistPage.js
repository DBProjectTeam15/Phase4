import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Row, Col, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로 확인

// PlayListCard 컴포넌트는 그대로 사용합니다.
const PlaylistCard = ({ rank, id, title, ownerNickname, songCount, navigate }) => {
    const handleDetailClick = () => {
        // 실제 API에서는 rank 대신 고유 ID를 사용해야 합니다.
        navigate(`/playlists/${id}`); 
    };
    
    return (
        <Col xs={12} md={6} className="mb-4">
            <div style={{ backgroundColor: 'white', padding: '15px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                <div className="d-flex justify-content-between align-items-center mb-2">
                    <div className="d-flex align-items-center">
                        <div 
                            style={{ 
                                width: '24px', 
                                height: '24px', 
                                borderRadius: '50%', 
                                backgroundColor: '#9370DB', 
                                color: 'white', 
                                fontWeight: 'bold', 
                                display: 'flex', 
                                alignItems: 'center', 
                                justifyContent: 'center',
                                fontSize: '0.8em'
                            }}
                            className="me-2"
                        >
                            {rank}
                        </div>
                        <h5 className="mb-0" style={{ fontSize: '1em', fontWeight: 'bold' }}>{title}</h5>
                    </div>
                    <span style={{ color: '#9370DB' }}>🎵</span>
                </div>
                
                <div className="mb-3" style={{ fontSize: '0.9em', color: '#666' }}>
                    소유자: {ownerNickname}
                </div>

                <div className="d-flex justify-content-between align-items-center">
                    <span style={{ color: '#9370DB', fontWeight: 'bold', fontSize: '0.9em' }}>{songCount}곡</span>
                    <Button 
                        variant="link" 
                        className="p-0" 
                        onClick={handleDetailClick}
                        style={{ color: '#666', textDecoration: 'none', fontSize: '0.9em' }}
                    >
                        자세히 보기 →
                    </Button>
                </div>
            </div>
        </Col>
    );
};

// ====================================================================
// 💻 PlaylistPage 컴포넌트 시작
// ====================================================================

function PlaylistPage() {
    const navigate = useNavigate();
    // MOCK 데이터 제거
    const [playlists, setPlaylists] = useState([]); 
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // 1. 🖼️ 인기 플레이리스트 목록 조회 (GET /playlists/top)
    const fetchTopPlaylists = async () => {
        setIsLoading(true);
        setError(null);
        try {
            // API 4.3.1 인기 플레이리스트 조회 (인증 필요 없음)
            const response = await apiClient.get('/playlists/top'); 
            
            // 응답 구조: { success: true, data: { playlists: [..], totalCount: N } }
            // API 응답 필드: id, title, userld, ownerNickname, songCount 등
            
            // rank 필드를 추가하여 렌더링에 사용
            const fetchedPlaylists = response.data.data.playlists.map((playlist, index) => ({
                ...playlist,
                rank: index + 1, // 순위를 배열 인덱스로 부여
            }));
            
            setPlaylists(fetchedPlaylists);
        } catch (err) {
            console.error("인기 플레이리스트 로드 오류:", err.response || err);
            setError("인기 플레이리스트 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요.");
            setPlaylists([]);
        } finally {
            setIsLoading(false);
        }
    };
    
    useEffect(() => {
        fetchTopPlaylists();
    }, []);

    const handleGoBack = () => {
        navigate(-1);
    };

    return (
        <Container style={{ maxWidth: '900px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>
            
            <h2 className="mb-1" style={{ fontWeight: 'bold' }}>플레이리스트 찾아보기</h2>
            <p className="text-muted mb-4" style={{ fontSize: '0.9em' }}>음원 수가 많은 상위 {playlists.length}개 플레이리스트</p>

            <Row>
                {isLoading ? (
                    <div className="text-center py-5"><Spinner animation="border" /> <p className="mt-2">데이터 로딩 중...</p></div>
                ) : error ? (
                    <div className="text-center py-5 text-danger">{error}</div>
                ) : (
                    playlists.map(playlist => (
                        <PlaylistCard key={playlist.id} {...playlist} navigate={navigate} />
                    ))
                )}
            </Row>
        </Container>
    );
}

export default PlaylistPage;