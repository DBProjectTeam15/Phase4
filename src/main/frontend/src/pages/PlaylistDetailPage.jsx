import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Button, Table, Spinner, Alert } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

const INITIAL_DETAIL = { id: null, title: '로딩 중...', ownerNickname: '', songs: 0, isCollaborative: "False" };

function PlaylistDetailPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [playlistDetail, setPlaylistDetail] = useState(INITIAL_DETAIL);
    const [songs, setSongs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchPlaylistDetails = async () => {
        setIsLoading(true);
        setError(null);

        const playlistId = parseInt(id, 10);

        // if (isNaN(playlistId)) {
        //     setError("유효하지 않은 플레이리스트 ID입니다.");
        //     setIsLoading(false);
        //     return;
        // }

        try {
            const [detailResponse, songsResponse] = await Promise.all([
                apiClient.get(`/api/playlists/${playlistId}`),
                apiClient.get(`/api/playlists/${playlistId}/songs`),
            ]);

            if (detailResponse.data) {
                const detail = detailResponse.data;
                setPlaylistDetail({
                    ...detail,
                    ownerNickname: `User ${detail.ownerId}`,
                });
            }
            else {
                setError("유효하지 않은 플레이리스트 ID입니다.");
                setIsLoading(false);
                return;
            }

            if (songsResponse.data) {
                const fetchedSongs = songsResponse.data;
                setSongs(fetchedSongs);
                setPlaylistDetail(prev => ({
                    ...prev,
                    songs: songsResponse.data.length
                }));
            }
            else {
                setError("유효하지 않은 플레이리스트 ID입니다.");
                setIsLoading(false);
                return;
            }

        } catch (err) {
            console.error("플레이리스트 상세 로드 오류:", err.response || err);
            if (err.response && err.response.status === 404) {
                setError("요청하신 플레이리스트를 찾을 수 없습니다.");
            } else {
                setError("데이터를 불러오는 중 오류가 발생했습니다. 서버 연결 상태를 확인해주세요.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchPlaylistDetails();
    }, [id]);

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleLinkClick = (playLink) => {
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
                    소유자: {data.ownerNickname}
                    {data.isCollaborative == "True" ? ' | (협업 가능)' : ''}
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
                                onClick={() => handleLinkClick(song.playLink)}
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