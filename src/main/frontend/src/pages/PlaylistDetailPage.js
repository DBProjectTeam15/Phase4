import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Button, Table, Spinner, Alert } from 'react-bootstrap';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로 확인

// 빈 객체/배열로 초기화하여 조건부 렌더링에 대비
const INITIAL_DETAIL = { id: null, title: '로딩 중...', ownerNickname: '', songs: 0, isCollaborative: false };

function PlaylistDetailPage() {
    const navigate = useNavigate();
    // URL 경로에서 ID를 가져옵니다. (이전에는 'rank'였지만, API는 'playlistId'를 사용)
    const { id } = useParams(); 

    const [playlistDetail, setPlaylistDetail] = useState(INITIAL_DETAIL);
    const [songs, setSongs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // 1. 🖼️ 플레이리스트 상세 정보 및 곡 목록 조회
    const fetchPlaylistDetails = async () => {
        setIsLoading(true);
        setError(null);
        
        // ID가 유효한 숫자가 아닐 경우를 대비
        const playlistId = parseInt(id, 10);
        if (isNaN(playlistId)) {
            setError("유효하지 않은 플레이리스트 ID입니다.");
            setIsLoading(false);
            return;
        }

        try {
            // 두 개의 API 호출을 동시에 실행합니다.
            const [detailResponse, songsResponse] = await Promise.all([
                // API 4.3.5 플레이리스트 상세 조회
                apiClient.get(`/playlists/${playlistId}`), 
                // API 4.3.6 플레이리스트 곡 목록 조회
                apiClient.get(`/playlists/${playlistId}/songs`), 
            ]);

            // 1. 상세 정보 처리
            if (detailResponse.data.success) {
                const detail = detailResponse.data.data;
                // ownerNickname 필드가 상세 조회 응답에 포함되어 있다고 가정합니다. (API 명세 4.3.5와 4.3.1 참조)
                setPlaylistDetail({
                    ...detail,
                    ownerNickname: detail.ownerNickname || `User ${detail.userld}`, // 닉네임이 없을 경우 대비
                });
            }

            // 2. 곡 목록 처리
            if (songsResponse.data.success) {
                // 응답 구조: { success: true, data: { songs: [..], totalSongs: N } }
                const fetchedSongs = songsResponse.data.data.songs;
                setSongs(fetchedSongs);
                // 총 곡 수는 songsResponse에서 가져오는 것이 더 정확할 수 있습니다.
                setPlaylistDetail(prev => ({ 
                    ...prev, 
                    songs: songsResponse.data.data.totalSongs 
                }));
            }
        } catch (err) {
            console.error("플레이리스트 상세 로드 오류:", err.response || err);
            if (err.response && err.response.status === 404) {
                setError("요청하신 플레이리스트를 찾을 수 없습니다."); // ⬅️ API 404 실패 응답
            } else {
                setError("데이터를 불러오는 중 오류가 발생했습니다. 서버 연결 상태를 확인해주세요.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchPlaylistDetails();
    }, [id]); // id가 변경될 때마다 재호출

    const handleGoBack = () => {
        navigate(-1);
    };
    
    const handleLinkClick = (playLink) => {
        // 실제 곡의 재생 링크로 이동
        window.open(playLink, '_blank');
    };

    if (isLoading) {
        return (
            <Container style={{ maxWidth: '900px' }} className="text-center py-5">
                <Spinner animation="border" /> <p className="mt-2">플레이리스트 정보 로딩 중...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container style={{ maxWidth: '900px' }} className="py-5">
                <Alert variant="danger">{error}</Alert>
                <Button variant="secondary" onClick={handleGoBack}>목록으로 돌아가기</Button>
            </Container>
        );
    }
    
    // 정상적으로 로딩된 데이터
    const data = playlistDetail; 

    return (
        <Container style={{ maxWidth: '900px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 플레이리스트 목록으로
                </Button>
            </div>
            
            <div className="mb-5">
                <h2 className="mb-1" style={{ fontWeight: 'bold' }}>{data.title}</h2>
                <p className="text-muted" style={{ fontSize: '0.9em' }}>
                    소유자: **{data.ownerNickname}** | 총 **{data.songs}**곡 
                    {data.isCollaborative ? ' | (협업 가능)' : ''}
                </p>
            </div>

            <Table borderless className="mb-5">
                <thead style={{ color: '#555' }}>
                    <tr>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '50%' }}>곡명</th>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '30%' }}>아티스트</th>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '20%' }}>재생 링크</th>
                    </tr>
                </thead>
                <tbody>
                    {songs.map((song) => (
                        <tr key={song.id}>
                            <td className="p-0 py-2">{song.title}</td>
                            <td className="p-0 py-2">{song.artistName}</td>
                            <td className="p-0 py-2">
                                <Button 
                                    variant="link" 
                                    onClick={() => handleLinkClick(song.playLink)} // 서버에서 받은 playLink 사용
                                    className="p-0" 
                                    style={{ color: '#007bff', textDecoration: 'none', fontSize: '0.9em' }}
                                >
                                    <span style={{ fontSize: '1em' }}>{'⇗'}</span> 재생
                                </Button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
            
            {songs.length === 0 && !isLoading && (
                <Alert variant="info" className="text-center">이 플레이리스트에는 아직 곡이 없습니다.</Alert>
            )}

        </Container>
    );
}

export default PlaylistDetailPage;