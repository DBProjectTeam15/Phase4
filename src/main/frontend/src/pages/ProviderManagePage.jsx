import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Table, Modal, Card, Form, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

function ProviderManagePage() {
    const navigate = useNavigate();

    const [providers, setProviders] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showAddModal, setShowAddModal] = useState(false);
    const [selectedProviderId, setSelectedProviderId] = useState(null);
    const [newProviderName, setNewProviderName] = useState('');
    const [newProviderLink, setNewProviderLink] = useState('');

    const fetchProviders = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await apiClient.get('/api/providers');

            const fetchedProviders = response.data;
            setProviders(fetchedProviders);
        } catch (err) {
            console.error("제공원 목록 로드 오류:", err.response || err);
            setError("제공원 목록을 불러오지 못했습니다. (서버 연결 또는 권한 확인)");
            setProviders([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchProviders();
    }, []);

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleShowDeleteModal = (id) => {
        setSelectedProviderId(id);
        setShowDeleteModal(true);
    };

    const handleConfirmDelete = async () => {
        if (!selectedProviderId) return;

        try {
            await apiClient.delete(`/api/providers/${selectedProviderId}`);

            alert(`제공원 ID ${selectedProviderId}가 삭제되었습니다.`);
            fetchProviders();

        } catch (err) {
            if (err.response && err.response.status === 404) {
                alert(`ID ${selectedProviderId}를 가진 제공원을 찾을 수 없습니다.`);
            } else {
                const msg = err.response?.data?.message || "제공원 삭제 중 오류가 발생했습니다.";
                alert(msg);
            }
        } finally {
            setShowDeleteModal(false);
            setSelectedProviderId(null);
        }
    };

    const handleCloseDeleteModal = () => {
        setShowDeleteModal(false);
        setSelectedProviderId(null);
    };

    const handleShowAddModal = () => {
        setShowAddModal(true);
        setNewProviderName('');
        setNewProviderLink('');
    };

    const handleAddProvider = async (e) => {
        e.preventDefault();
        if (!newProviderName || !newProviderLink) {
            alert("이름과 링크를 모두 입력해주세요.");
            return;
        }

        try {
            const response = await apiClient.post('/api/providers', {
                name: newProviderName,
                link: newProviderLink,
            });

            alert(response.data.message || `${response.data.data.name} 제공원이 추가되었습니다.`);
            fetchProviders();

        } catch (err) {
            console.error("제공원 추가 오류:", err.response || err);
            const msg = err.response?.data?.message || "제공원 추가에 실패했습니다. (중복 또는 입력 오류)";
            alert(msg);
        } finally {
            setShowAddModal(false);
        }
    };

    const handleCloseAddModal = () => {
        setShowAddModal(false);
    };

    const handleLinkClick = (link) => {
        window.open(link, '_blank');
    };


    return (
        <Container style={{ maxWidth: '1000px', marginTop: '50px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>

            <h2 className="mb-1" style={{ fontWeight: 'bold' }}>제공원 관리</h2>
            <p className="text-muted mb-4" style={{ fontSize: '0.9em' }}>음악 제공원 목록을 확인하고 관리하세요</p>

            <Card className="p-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h4 style={{ fontWeight: 'bold' }}>제공원 목록 ({isLoading ? '로딩 중' : providers.length + '개'})</h4>
                    <Button variant="dark" onClick={handleShowAddModal}>
                        + 제공원 추가
                    </Button>
                </div>
                <div className="mt-3">
                    {isLoading ? (
                        <div className="text-center py-5"><Spinner animation="border" /> <p className="mt-2">데이터 로딩 중...</p></div>
                    ) : error ? (
                        <div className="text-center py-5 text-danger">{error}</div>
                    ) : (
                        <Table borderless responsive>
                            <thead style={{ color: '#555' }}>
                            <tr>
                                <th className="p-0 pb-2 border-bottom">제공원 ID</th>
                                <th className="p-0 pb-2 border-bottom">제공원 이름</th>
                                <th className="p-0 pb-2 border-bottom">제공원 링크</th>
                                <th className="p-0 pb-2 border-bottom">작업</th>
                            </tr>
                            </thead>
                            <tbody>
                            {providers.map((prov) => (
                                <tr key={prov.id}>
                                    <td className="p-0 py-2">{prov.id}</td>
                                    <td className="p-0 py-2">{prov.name}</td>
                                    <td className="p-0 py-2">
                                        <Button
                                            variant="link"
                                            onClick={() => handleLinkClick(prov.link)}
                                            style={{ padding: '0' }}
                                        >
                                            링크 {'⇗'}
                                        </Button>
                                    </td>
                                    <td className="p-0 py-2">
                                        <Button
                                            variant="link"
                                            onClick={() => handleShowDeleteModal(prov.id)}
                                            style={{ color: '#dc3545', padding: '0' }}
                                        >
                                            🗑️
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </Table>
                    )}
                </div>
            </Card>

            <Modal show={showDeleteModal} onHide={handleCloseDeleteModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>제공원 삭제 확인</Modal.Title>
                </Modal.Header>
                <Modal.Body>정말 제공원 ID {selectedProviderId}를 삭제하시겠습니까?</Modal.Body>
                <Modal.Footer>
                    <Button variant="danger" onClick={handleConfirmDelete}>
                        예, 삭제합니다
                    </Button>
                    <Button variant="secondary" onClick={handleCloseDeleteModal}>
                        아니오
                    </Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showAddModal} onHide={handleCloseAddModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>새 제공원 추가</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form onSubmit={handleAddProvider}>
                        <Form.Group className="mb-3">
                            <Form.Label>제공원 이름</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="예: YouTube Music"
                                value={newProviderName}
                                onChange={(e) => setNewProviderName(e.target.value)}
                                required
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>제공원 링크 (URL)</Form.Label>
                            <Form.Control
                                type="url"
                                placeholder="예: https://music.youtube.com"
                                value={newProviderLink}
                                onChange={(e) => setNewProviderLink(e.target.value)}
                                required
                            />
                        </Form.Group>
                        <Button variant="dark" type="submit" className="w-100 mt-3">
                            추가하기
                        </Button>
                    </Form>
                </Modal.Body>
            </Modal>
        </Container>
    );
}

export default ProviderManagePage;