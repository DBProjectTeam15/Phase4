import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Button, Table, Spinner, Alert, Form, Row, Col, Card } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

const INITIAL_DETAIL = { id: null, title: '로딩 중...', ownerNickname: '', songs: 0, isCollaborative: "False" };

function PlaylistDetailPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [playlistDetail, setPlaylistDetail] = useState(INITIAL_DETAIL);
    const [songs, setSongs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // 곡 추가용 state
    const [songIdToAdd, setSongIdToAdd] = useState('');

    // 댓글용 state
    const [comments, setComments] = useState([]);
    const [commentContent, setCommentContent] = useState('');

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
        fetchComments();
    }, [id]);

    // 댓글 목록 가져오기
    const fetchComments = async () => {
        try {
            const response = await apiClient.get(`/api/comments/playlists/${id}`);
            setComments(response.data || []);
        } catch (err) {
            console.error("댓글 로드 오류:", err);
        }
    };

    // 곡 추가
    const handleAddSong = async (e) => {
        e.preventDefault();
        if (!songIdToAdd) {
            alert('곡 ID를 입력해주세요.');
            return;
        }

        try {
            await apiClient.post(`/api/playlists/${id}/songs/${songIdToAdd}`);
            alert('곡이 추가되었습니다!');
            setSongIdToAdd('');
            fetchPlaylistDetails(); // 곡 목록 새로고침
        } catch (err) {
            if (err.response?.status === 409) {
                alert('이미 플레이리스트에 추가된 곡입니다. (동시성 제어 작동!)');
            } else {
                alert('곡 추가 실패: ' + (err.response?.data || err.message));
            }
        }
    };

    // 댓글 추가
    const handleAddComment = async (e) => {
        e.preventDefault();
        if (!commentContent.trim()) {
            alert('댓글 내용을 입력해주세요.');
            return;
        }

        try {
            await apiClient.post(`/api/comments/playlists/${id}`, {
                content: commentContent
            });
            setCommentContent('');
            fetchComments(); // 댓글 목록 새로고침
            alert('댓글이 추가되었습니다!');
        } catch (err) {
            alert('댓글 추가 실패: ' + (err.response?.data || err.message));
        }
    };

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

            {/* 곡 추가 폼 (동시성 테스트용) */}
            <Card className="p-3 mb-4" style={{ backgroundColor: '#f8f9fa' }}>
                <Form onSubmit={handleAddSong}>
                    <Form.Group>
                        <Form.Label style={{ fontWeight: 'bold' }}>곡 추가 (동시성 테스트용)</Form.Label>
                        <Row className="g-2">
                            <Col md={8}>
                                <Form.Control
                                    type="number"
                                    placeholder="곡 ID 입력 (예: 1, 2, 3...)"
                                    value={songIdToAdd}
                                    onChange={(e) => setSongIdToAdd(e.target.value)}
                                />
                            </Col>
                            <Col md={4}>
                                <Button type="submit" variant="primary" className="w-100">
                                    추가
                                </Button>
                            </Col>
                        </Row>
                    </Form.Group>
                </Form>
                <Alert variant="info" className="mt-3 mb-0" style={{ fontSize: '0.9em' }}>
                    <strong>동시성 테스트 방법:</strong>
                    <ol className="mb-0 mt-2">
                        <li>두 개의 브라우저 탭에서 같은 플레이리스트를 엽니다</li>
                        <li>두 탭에서 동시에 같은 곡 ID를 입력하고 추가 버튼을 클릭</li>
                        <li>하나는 성공, 다른 하나는 "이미 추가된 곡" 메시지 확인</li>
                    </ol>
                </Alert>
            </Card>

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

            {/* 댓글 섹션 */}
            <div className="mt-5">
                <h4 style={{ fontWeight: 'bold' }}>댓글</h4>

                {/* 댓글 추가 폼 */}
                <Card className="p-3 mb-3">
                    <Form onSubmit={handleAddComment}>
                        <Form.Group>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                placeholder="댓글을 입력하세요..."
                                value={commentContent}
                                onChange={(e) => setCommentContent(e.target.value)}
                            />
                        </Form.Group>
                        <Button type="submit" variant="dark" className="mt-2">
                            댓글 작성
                        </Button>
                    </Form>
                </Card>

                {/* 댓글 목록 */}
                <div className="mt-3">
                    {comments.length === 0 ? (
                        <Alert variant="info">첫 댓글을 작성해보세요!</Alert>
                    ) : (
                        comments.map((comment, index) => (
                            <Card key={index} className="mb-2">
                                <Card.Body>
                                    <Card.Text>{comment.content}</Card.Text>
                                    <Card.Subtitle className="text-muted" style={{ fontSize: '0.85em' }}>
                                        User {comment.userId} - {new Date(comment.commentedAt).toLocaleString('ko-KR')}
                                    </Card.Subtitle>
                                </Card.Body>
                            </Card>
                        ))
                    )}
                </div>
            </div>

        </Container>
    );
}

export default PlaylistDetailPage;