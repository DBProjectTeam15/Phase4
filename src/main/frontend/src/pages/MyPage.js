import React, { useState, useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { Container, Card, Button, Form, Row, Col, Table, Modal, Alert, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로

// ====================================================================
// 🔑 상태 및 API 정의
// ====================================================================

const TAB_API_MAP = {
    '소유': '/playlists/my',
    '공유': '/playlists/shared',
    '편집 가능': '/playlists/editable',
    '내 댓글': '/comments/my',
};
const TAB_NAMES = Object.keys(TAB_API_MAP);

// 테이블 컴포넌트 (API 데이터 구조에 맞춰 수정)
const PlaylistTable = ({ content, navigate }) => {
    if (content.isLoading) {
        return <div className="text-center py-5"><Spinner animation="border" size="sm" /> <p className="mt-2">목록 로딩 중...</p></div>;
    }
    if (content.error) {
        return <Alert variant="warning" className="mt-3">데이터를 불러오지 못했습니다. ({content.error})</Alert>;
    }
    if (content.data.length === 0) {
        return <Alert variant="info" className="mt-3">조회된 목록이 없습니다.</Alert>;
    }

    // 탭 이름에 따라 헤더와 표시 데이터를 동적으로 설정
    let headers, dataDisplay;
    
    if (content.tabName === '내 댓글') {
        headers = ['플레이리스트 제목', '댓글 내용', '작성 시각'];
        dataDisplay = content.data.map(d => ({
            id: d.commentedAt, // 댓글은 고유 ID가 없으므로 작성 시각을 키로 사용
            col1: d.playlistTitle || '플레이리스트 ID: ' + d.playlistId,
            col2: d.content,
            col3: new Date(d.commentedAt).toLocaleDateString('ko-KR'),
            playlistId: d.playlistId,
        }));
    } else {
        headers = ['제목', '협업', '곡 수', content.tabName === '소유' ? '소유자 ID' : '소유자 닉네임'];
        dataDisplay = content.data.map(d => ({
            id: d.id,
            col1: d.title,
            col2: d.isCollaborative ? 'Y' : 'N',
            col3: d.songCount || '-', // 곡 수는 API 응답에 songCount 필드가 있다고 가정
            col4: d.ownerNickname || d.userId,
            playlistId: d.id,
        }));
    }

    return (
        <>
            <div className="text-muted mt-3 mb-3" style={{ fontSize: '0.9em' }}>
                {content.title} ({content.data.length}건)
            </div>
            <Table borderless hover style={{ fontSize: '0.9em' }}>
                <thead style={{ color: '#555' }}>
                    <tr>
                        {headers.map((header) => (
                            <th key={header} className="p-0 pb-2 border-bottom">
                                {header}
                            </th>
                        ))}
                        <th className="p-0 pb-2 border-bottom"></th>
                    </tr>
                </thead>
                <tbody>
                    {dataDisplay.map((row) => (
                        <tr 
                            key={row.id} 
                            onClick={() => navigate(`/playlists/${row.playlistId}`)} 
                            style={{ cursor: 'pointer' }}
                        >
                            <td className="p-0 py-2" title={row.col1}>{row.col1}</td>
                            <td className="p-0 py-2">{row.col2}</td>
                            <td className="p-0 py-2">{row.col3}</td>
                            <td className="p-0 py-2">{row.col4}</td>
                            <td className="p-0 py-2 text-end">
                                <span style={{ color: '#007bff', fontSize: '0.9em' }}>상세 보기 →</span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </>
    );
};


function MyPage() {
    const navigate = useNavigate();
    // useOutletContext를 통해 최상위 로그인 상태 변경 함수를 가져옵니다.
    const { setIsLoggedIn } = useOutletContext(); 
    
    // 유저 정보 상태
    const [userProfile, setUserProfile] = useState({ nickname: '', email: '', id: null });
    
    // 폼 상태
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [newNickname, setNewNickname] = useState('');
    
    // UI 상태
    const [activeTab, setActiveTab] = useState('소유');
    const [showModal, setShowModal] = useState(false);
    const [showAlert, setShowAlert] = useState(false);
    const [alertMessage, setAlertMessage] = useState('');
    const [isProfileLoading, setIsProfileLoading] = useState(true);

    // 탭별 플레이리스트/댓글 데이터
    const [tabContentState, setTabContentState] = useState({
        '소유': { data: [], isLoading: false, error: null, title: '내가 소유한 플레이리스트' },
        '공유': { data: [], isLoading: false, error: null, title: '나와 공유된 플레이리스트' },
        '편집 가능': { data: [], isLoading: false, error: null, title: '편집 가능한 플레이리스트' },
        '내 댓글': { data: [], isLoading: false, error: null, title: '내가 작성한 댓글' },
    });

    const handleShowAlert = (message) => {
        setAlertMessage(message);
        setShowAlert(true);
        setTimeout(() => setShowAlert(false), 3000);
    };

    const handleGoBack = () => {
        navigate(-1);
    };

    // 1. 🖼️ 사용자 프로필 정보 로드 (GET /api/user/profile)
    const fetchUserProfile = async () => {
        setIsProfileLoading(true);
        try {
            const response = await apiClient.get('/user/profile'); // ⬅️ API 4.2.1
            const data = response.data.data;
            setUserProfile({
                id: data.userld,
                nickname: data.nickname,
                email: data.email,
            });
        } catch (err) {
            console.error("프로필 로드 오류:", err.response || err);
            // 401 에러는 apiClient에서 리다이렉트 처리 가정
            handleShowAlert("프로필 정보를 불러오는 데 실패했습니다.");
        } finally {
            setIsProfileLoading(false);
        }
    };
    
    // 2. 🎶 탭별 목록 로드 (GET /api/playlists/my, /shared, /editable, /comments/my)
    const fetchTabContent = async (tabName) => {
        const endpoint = TAB_API_MAP[tabName];
        if (!endpoint) return;

        setTabContentState(prev => ({ 
            ...prev, 
            [tabName]: { ...prev[tabName], isLoading: true, error: null } 
        }));

        try {
            const response = await apiClient.get(endpoint); // ⬅️ API 4.3.2, 4.3.4, 4.3.3, 4.5.1
            
            // 응답 데이터 구조에 따라 목록 필드 선택 (플레이리스트는 .playlists, 댓글은 .comments)
            const listKey = tabName === '내 댓글' ? 'comments' : 'playlists';
            const data = response.data.data[listKey] || [];
            
            setTabContentState(prev => ({ 
                ...prev, 
                [tabName]: { ...prev[tabName], data: data, isLoading: false } 
            }));

        } catch (err) {
            console.error(`${tabName} 목록 로드 오류:`, err.response || err);
            const msg = err.response?.data?.message || "목록을 불러오는 데 오류가 발생했습니다.";
            setTabContentState(prev => ({ 
                ...prev, 
                [tabName]: { ...prev[tabName], error: msg, isLoading: false } 
            }));
        }
    };
    
    // 마운트 시 프로필 로드, 탭 변경 시 해당 탭 데이터 로드
    useEffect(() => {
        fetchUserProfile();
    }, []);
    
    useEffect(() => {
        fetchTabContent(activeTab);
    }, [activeTab]);


    const handleShowModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    // 3. 💣 계정 삭제 처리 (DELETE /api/user/account)
    const handleAccountDeletion = async () => {
        try {
            // API 4.2.4 계정 삭제
            const response = await apiClient.delete('/user/account', {
                data: { confirmation: 'y' } // 사양서에 따라 Body에 confirmation:'y' 전송
            });
            
            // 성공 (200 OK) 후 처리
            alert(response.data.message || '계정이 성공적으로 삭제되었습니다.');
            
            // 전역 상태 및 localStorage 정리 (로그아웃 처리)
            localStorage.removeItem('session_token'); // 일반 사용자 토큰 가정
            setIsLoggedIn(false); // 상위 컴포넌트의 로그인 상태 업데이트
            
            navigate('/'); // 메인 페이지로 이동
            
        } catch (err) {
            console.error("계정 삭제 오류:", err.response || err);
            const msg = err.response?.data?.message || '계정 삭제에 실패했습니다.';
            alert(msg);
        } finally {
            setShowModal(false);
        }
    };

    // 4. 🔒 비밀번호 변경 처리 (PUT /api/user/password)
    const handlePasswordChange = async (e) => {
        e.preventDefault();
        
        // 폼에서 현재 비밀번호와 변경할 비밀번호의 입력 값을 가져와야 합니다.
        // 현재는 상태가 없으므로 임시로 폼 제출만 막습니다. (실제 구현 시 상태와 연결 필수)
        const currentPass = e.target.currentPassword.value;
        const newPass = e.target.newPassword.value;

        try {
            // API 4.2.2 비밀번호 변경
            const response = await apiClient.put('/user/password', {
                currentPassword: currentPass,
                newPassword: newPass,
            });
            
            // 성공 (200 OK) 후 처리
            handleShowAlert(response.data.message || '비밀번호가 변경되었습니다.');
            e.target.reset(); // 폼 초기화
            
        } catch (err) {
            console.error("비밀번호 변경 오류:", err.response || err);
            const msg = err.response?.data?.message || '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.';
            handleShowAlert(msg);
        }
    };

    // 5. ✏️ 닉네임 변경 처리 (PUT /api/user/nickname)
    const handleNicknameChange = async (e) => {
        e.preventDefault();
        
        const nickname = e.target.newNickname.value;

        try {
            // API 4.2.3 닉네임 변경
            const response = await apiClient.put('/user/nickname', {
                newNickname: nickname,
            });

            // 성공 (200 OK) 후 처리
            const updatedNickname = response.data.data.nickname;
            
            // 로컬 상태와 UI 업데이트
            setUserProfile(prev => ({ ...prev, nickname: updatedNickname }));
            
            // API 명세 비고: 닉네임 변경 후 세션 정보도 업데이트되어야 함.
            handleShowAlert(response.data.message || '닉네임이 변경되었습니다.');
            e.target.reset(); // 폼 초기화
            
        } catch (err) {
            console.error("닉네임 변경 오류:", err.response || err);
            const msg = err.response?.data?.message || '닉네임 변경에 실패했습니다.';
            handleShowAlert(msg);
        }
    };


    return (
        <Container style={{ maxWidth: '800px' }}>
            
            {showAlert && (
                <Alert variant={alertMessage.includes('성공') || alertMessage.includes('변경') ? "success" : "danger"} onClose={() => setShowAlert(false)} dismissible className="position-absolute top-0 start-50 translate-middle-x mt-3" style={{ zIndex: 1050, width: '100%', maxWidth: '300px' }}>
                    {alertMessage}
                </Alert>
            )}

            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>

            <h2 className="mb-1" style={{ fontWeight: 'normal' }}>내 정보</h2>
            <p className="mb-4 text-muted" style={{ fontWeight: 'bold' }}>{isProfileLoading ? '로딩 중...' : userProfile.email}</p>
            
            <Card className="p-4 mb-5 shadow-sm" style={{ border: 'none' }}>
                <h4 className="mb-4" style={{ fontWeight: 'bold', fontSize: '1.2em' }}>계정 설정</h4>
                
                {isProfileLoading && <div className="text-center py-3"><Spinner animation="border" size="sm" /></div>}

                {/* 닉네임 정보 표시 */}
                <div className="mb-4">
                    <h5 className="mb-2" style={{ fontSize: '1em' }}>현재 닉네임</h5>
                    <p className="mb-3" style={{ fontSize: '1.2em', fontWeight: 'bold' }}>{userProfile.nickname || '닉네임 없음'}</p>
                </div>

                <div className="mb-4">
                    <h5 className="mb-3" style={{ fontSize: '1em' }}>비밀번호 변경</h5>
                    <Form onSubmit={handlePasswordChange}>
                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Label className="text-muted" style={{ fontSize: '0.8em' }}>현재 비밀번호</Form.Label>
                                <Form.Control 
                                    type="password" 
                                    name="currentPassword"
                                    placeholder="현재 비밀번호 입력" 
                                    style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                                    required
                                />
                            </Col>
                            <Col md={6}>
                                <Form.Label className="text-muted" style={{ fontSize: '0.8em' }}>변경할 비밀번호</Form.Label>
                                <Form.Control 
                                    type="password" 
                                    name="newPassword"
                                    placeholder="새 비밀번호 입력" 
                                    style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                                    required
                                />
                            </Col>
                            <Col xs={12}>
                                <Button variant="dark" type="submit" style={{ backgroundColor: 'black', color: 'white', padding: '8px 20px' }}>
                                    비밀번호 변경
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </div>

                <div className="mb-4">
                    <h5 className="mb-3" style={{ fontSize: '1em' }}>닉네임 변경</h5>
                    <Form className="d-flex align-items-end" onSubmit={handleNicknameChange}>
                        <div style={{ flexGrow: 1, maxWidth: '300px' }}>
                            <Form.Label className="text-muted" style={{ fontSize: '0.8em' }}>새 닉네임</Form.Label>
                            <Form.Control 
                                type="text" 
                                name="newNickname"
                                placeholder="새 닉네임을 입력하세요" 
                                style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                                required
                            />
                        </div>
                        <Button variant="dark" type="submit" className="ms-3" style={{ backgroundColor: 'black', color: 'white', padding: '12px 20px', height: '48px' }}>
                            닉네임 변경
                        </Button>
                    </Form>
                </div>

                <div>
                    <h5 className="mb-2" style={{ fontSize: '1em' }}>계정 삭제</h5>
                    <p className="text-muted mb-3" style={{ fontSize: '0.9em' }}>계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.</p>
                    <Button 
                        variant="danger" 
                        onClick={handleShowModal}
                        style={{ backgroundColor: '#dc3545', color: 'white', padding: '8px 20px', border: 'none' }}
                    >
                        계정 삭제
                    </Button>
                </div>
            </Card>
            
            <Card className="p-4 shadow-sm" style={{ border: 'none' }}>
                <h4 className="mb-4" style={{ fontWeight: 'bold', fontSize: '1.2em' }}>플레이리스트 및 댓글</h4>

                <div className="mb-4 p-1 rounded-pill" style={{ backgroundColor: '#eee', display: 'flex' }}>
                    {TAB_NAMES.map((tab) => (
                        <Button
                            key={tab}
                            variant="link"
                            onClick={() => setActiveTab(tab)}
                            className="flex-grow-1 text-center p-2 rounded-pill"
                            style={{
                                color: 'black',
                                fontWeight: 'normal',
                                backgroundColor: activeTab === tab ? 'white' : 'transparent',
                                boxShadow: activeTab === tab ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                                transition: 'background-color 0.2s',
                                textDecoration: 'none'
                            }}
                        >
                            {tab}
                        </Button>
                    ))}
                </div>

                <PlaylistTable content={tabContentState[activeTab]} navigate={navigate} />
            </Card>
            
            <Modal show={showModal} onHide={handleCloseModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>계정 삭제</Modal.Title>
                </Modal.Header>
                <Modal.Body>정말 계정을 영구적으로 삭제하시겠습니까? **이 작업은 되돌릴 수 없습니다.**</Modal.Body>
                <Modal.Footer>
                    <Button variant="danger" onClick={handleAccountDeletion}>
                        예, 계정을 삭제합니다
                    </Button>
                    <Button variant="secondary" onClick={handleCloseModal}>
                        아니오
                    </Button>
                </Modal.Footer>
            </Modal>

        </Container>
    );
}

export default MyPage;