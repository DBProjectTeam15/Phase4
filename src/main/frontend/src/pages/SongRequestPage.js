import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Table, Modal, Card, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient';

function SongRequestPage() {
    const navigate = useNavigate();
    
    const [requests, setRequests] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showModal, setShowModal] = useState(false);
    const [selectedRequestId, setSelectedRequestId] = useState(null); 

    // 1. 🖼️ 악곡 요청 목록 조회 (GET /song-requests) - 수정됨!
    const fetchRequests = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await apiClient.get('/song-requests'); 
            
            const fetchedRequests = response.data.data.requests.map(req => ({
                id: req.id,
                title: req.title,
                artist: req.artist,
                requesterId: req.userld,
                date: new Date(req.requestAt).toLocaleString('ko-KR', { 
                    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' 
                }),
            }));
            setRequests(fetchedRequests);
        } catch (err) {
            console.error("악곡 요청 목록 로드 오류:", err.response || err);
            setError("악곡 요청 목록을 불러오지 못했습니다. (서버 연결 또는 권한 확인)");
            setRequests([]);
        } finally {
            setIsLoading(false);
        }
    };
    
    useEffect(() => {
        fetchRequests();
    }, []);

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleShowDeleteModal = (id) => {
        setSelectedRequestId(id);
        setShowModal(true);
    };

    // 2. 🗑️ 악곡 요청 삭제 (DELETE /song-requests/{requestId}) - 수정됨!
    const handleConfirmDelete = async () => {
        if (!selectedRequestId) return;
        
        try {
            await apiClient.delete(`/song-requests/${selectedRequestId}`); 
            
            alert(`요청 ID ${selectedRequestId}가 삭제(처리)되었습니다.`);
            fetchRequests(); 

        } catch (err) {
            if (err.response && err.response.status === 404) {
                alert(`ID ${selectedRequestId}를 가진 요청을 찾을 수 없습니다.`); 
            } else {
                const msg = err.response?.data?.message || "요청 삭제 처리 중 오류가 발생했습니다.";
                alert(msg);
            }
        } finally {
            setShowModal(false);
            setSelectedRequestId(null);
        }
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedRequestId(null);
    };

    return (
        <Container style={{ maxWidth: '1000px', marginTop: '50px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>
            
            <h2 className="mb-1" style={{ fontWeight: 'bold' }}>악곡 요청 관리</h2>
            <p className="text-muted mb-4" style={{ fontSize: '0.9em' }}>사용자들이 요청한 악곡 목록을 확인하고 관리하세요</p>

            <Card className="p-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                <h4 style={{ fontWeight: 'bold' }}>악곡 요청 목록 ({isLoading ? '로딩 중' : requests.length + '건'})</h4>
                <div className="mt-3">
                    {isLoading ? (
                        <div className="text-center py-5"><Spinner animation="border" /> <p className="mt-2">데이터 로딩 중...</p></div>
                    ) : error ? (
                        <div className="text-center py-5 text-danger">{error}</div>
                    ) : (
                        <Table borderless responsive>
                            <thead style={{ color: '#555' }}>
                                <tr>
                                    <th className="p-0 pb-2 border-bottom">요청 ID</th>
                                    <th className="p-0 pb-2 border-bottom">곡명</th>
                                    <th className="p-0 pb-2 border-bottom">아티스트</th>
                                    <th className="p-0 pb-2 border-bottom">신청자 ID</th>
                                    <th className="p-0 pb-2 border-bottom">요청 일시</th>
                                    <th className="p-0 pb-2 border-bottom">작업</th>
                                </tr>
                            </thead>
                            <tbody>
                                {requests.map((req) => (
                                    <tr key={req.id}>
                                        <td className="p-0 py-2">{req.id}</td>
                                        <td className="p-0 py-2">{req.title}</td>
                                        <td className="p-0 py-2">{req.artist}</td>
                                        <td className="p-0 py-2">{req.requesterId}</td>
                                        <td className="p-0 py-2">{req.date}</td>
                                        <td className="p-0 py-2">
                                            <Button 
                                                variant="link" 
                                                onClick={() => handleShowDeleteModal(req.id)}
                                                style={{ color: '#dc3545', padding: '0' }}
                                            >
                                                🗑️ 완료 처리
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    )}
                </div>
            </Card>

            <Modal show={showModal} onHide={handleCloseModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>요청 삭제(처리) 확인</Modal.Title>
                </Modal.Header>
                <Modal.Body>요청 ID **{selectedRequestId}**에 대한 처리를 완료하고 목록에서 삭제하시겠습니까?</Modal.Body>
                <Modal.Footer>
                    <Button variant="danger" onClick={handleConfirmDelete}>
                        예, 삭제합니다
                    </Button>
                    <Button variant="secondary" onClick={handleCloseModal}>
                        아니오
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}

export default SongRequestPage;