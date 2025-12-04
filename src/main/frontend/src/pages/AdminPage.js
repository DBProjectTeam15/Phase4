import React, { useState, useEffect } from 'react';
import { Container, Card, Form, Button, Row, Col, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로 확인

// 관리자 세션 토큰이 저장될 localStorage 키
const MANAGER_TOKEN_KEY = 'manager_token'; 
const MANAGER_ID_KEY = 'manager_id'; // 관리자 ID/Username 저장 키

const ADMIN_CARDS = [
    { icon: '🎵', color: '#9370DB', title: '악곡 요청 관리', path: '/admin/requests', desc: '사용자 악곡 요청 확인 및 처리' },
    { icon: '🔗', color: '#3CB371', title: '제공원 관리', path: '/admin/providers', desc: '음악 제공원 추가 및 삭제' },
    { icon: '🧑‍🎤', color: '#BA55D3', title: '아티스트 관리', path: '/admin/artists', desc: '아티스트 정보 추가 및 관리' },
];

const AdminCard = ({ data, isLoggedIn, onClick }) => {
    const buttonStyle = isLoggedIn
        ? { backgroundColor: 'black', color: 'white', padding: '10px 20px' }
        : { backgroundColor: '#ddd', color: '#666', padding: '10px 20px', cursor: 'default' };

    return (
        <Col md={4} className="mb-4">
            <Card style={{ border: 'none', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)' }}>
                <Card.Body className="text-center p-4">
                    <div 
                        style={{ 
                            fontSize: '40px', 
                            color: data.color, 
                            marginBottom: '10px',
                            lineHeight: '1', 
                        }}
                    >
                        {data.icon}
                    </div>
                    <Card.Title style={{ fontWeight: 'bold', fontSize: '1.1em' }}>{data.title}</Card.Title>
                    <Card.Text className="text-muted mb-3" style={{ fontSize: '0.85em' }}>
                        {data.desc}
                    </Card.Text>
                    <Button 
                        variant={isLoggedIn ? 'dark' : 'light'} 
                        className="w-100"
                        style={buttonStyle}
                        disabled={!isLoggedIn}
                        onClick={isLoggedIn ? onClick : undefined}
                    >
                        관리하기
                    </Button>
                </Card.Body>
            </Card>
        </Col>
    );
};


function AdminPage() {
    // 세션 토큰의 존재 여부와 ID를 localStorage에서 확인하여 초기화
    const [isAdminLoggedIn, setIsAdminLoggedIn] = useState(
        !!localStorage.getItem(MANAGER_TOKEN_KEY)
    );
    const [adminId, setAdminId] = useState(
        localStorage.getItem(MANAGER_ID_KEY) || ''
    );
    
    const [idInput, setIdInput] = useState('');
    const [passwordInput, setPasswordInput] = useState('');
    const [isLoggingIn, setIsLoggingIn] = useState(false); // 로딩 상태 추가

    const navigate = useNavigate();

    // 1. 🔑 관리자 로그인 처리 (POST /api/manager/auth/login)
    const handleLogin = async (e) => {
        e.preventDefault();
        
        if (!idInput || !passwordInput) {
            alert('아이디와 비밀번호를 모두 입력해주세요.');
            return;
        }

        setIsLoggingIn(true);
        try {
            // API 4.6.1 관리자 로그인
            const response = await apiClient.post('/manager/auth/login', {
                username: idInput, // API 사양서에는 'username'으로 되어있음
                password: passwordInput,
            });
            
            // 응답 성공 시 (200 OK)
            // ⚠️ 참고: API 명세서에는 토큰을 어디서 받는지 명확하지 않아, 
            // 여기서는 단순 성공 메시지와 함께 세션이 생성된다고 가정하고, 
            // 로그인 성공 시 임시 토큰 또는 응답 데이터를 토큰으로 저장합니다.
            
            // 실제 토큰을 서버에서 받지 못했을 경우 임시 값 사용
            const tokenValue = 'manager-session-' + Date.now(); 
            
            localStorage.setItem(MANAGER_TOKEN_KEY, tokenValue); // 토큰 저장
            localStorage.setItem(MANAGER_ID_KEY, response.data.data.username); // ID 저장
            
            setAdminId(response.data.data.username);
            setIsAdminLoggedIn(true);
            
            alert(response.data.message || '관리자 로그인에 성공했습니다.');
            
        } catch (error) {
            console.error('관리자 로그인 오류:', error.response || error);
            // 401 Unauthorized 등 오류 처리
            const msg = error.response?.data?.message || '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.';
            alert(msg);
        } finally {
            setIsLoggingIn(false);
            setPasswordInput(''); // 보안을 위해 비밀번호 입력 필드 초기화
        }
    };

    // 2. 🚪 관리자 로그아웃 처리 (POST /api/manager/auth/logout)
    const handleLogout = async () => {
        try {
            // API 4.6.2 관리자 로그아웃
            const response = await apiClient.post('/manager/auth/logout');

            alert(response.data.message || '로그아웃 되었습니다.');

        } catch (error) {
            console.error('관리자 로그아웃 오류:', error.response || error);
            // 로그아웃 실패 시 (예: 서버 오류)에도 프론트엔드 세션은 정리
            alert('로그아웃 처리 중 오류가 발생했습니다. 브라우저 세션을 정리합니다.');
        } finally {
            // 성공/실패와 관계없이 클라이언트 측 세션 정보 제거
            localStorage.removeItem(MANAGER_TOKEN_KEY);
            localStorage.removeItem(MANAGER_ID_KEY);
            
            setIsAdminLoggedIn(false);
            setAdminId('');
            setIdInput('');
        }
    };

    const handleManageClick = (path) => {
        if (!isAdminLoggedIn) return;
        navigate(path);
    };

    const LoginContent = (
        <Card className="p-4 mb-5 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
            <h5 className="mb-3" style={{ fontWeight: 'bold' }}>관리자 로그인</h5>
            <Form onSubmit={handleLogin}>
                <Form.Group className="mb-3">
                    <Form.Label>관리자 아이디</Form.Label>
                    <Form.Control 
                        type="text" 
                        placeholder="관리자 아이디를 입력하세요 (예: admin)" 
                        value={idInput}
                        onChange={(e) => setIdInput(e.target.value)}
                        style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                        disabled={isLoggingIn}
                    />
                </Form.Group>
                <Form.Group className="mb-4">
                    <Form.Label>비밀번호</Form.Label>
                    <Form.Control 
                        type="password" 
                        placeholder="비밀번호를 입력하세요" 
                        value={passwordInput}
                        onChange={(e) => setPasswordInput(e.target.value)}
                        style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                        disabled={isLoggingIn}
                    />
                </Form.Group>
                <Button 
                    variant="dark" 
                    type="submit" 
                    className="w-100" 
                    style={{ backgroundColor: 'black', color: 'white', padding: '12px' }}
                    disabled={isLoggingIn}
                >
                    {isLoggingIn ? <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> 로그인 중...</> : '로그인'}
                </Button>
            </Form>
        </Card>
    );

    const LoggedInContent = (
        <Card className="p-4 mb-5 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
            <h5 className="mb-3" style={{ fontWeight: 'bold' }}>관리자 로그인 정보</h5>
            <div className="d-flex justify-content-between align-items-center">
                <div>
                    <div style={{ color: '#555', fontSize: '0.9em' }}>로그인된 관리자 계정</div>
                    <div style={{ fontWeight: 'bold' }}>{adminId}</div> 
                </div>
                <Button 
                    variant="light" 
                    onClick={handleLogout}
                    style={{ color: 'black', border: '1px solid #ddd' }}
                >
                    로그아웃
                </Button>
            </div>
        </Card>
    );


    return (
        <Container style={{ width: '100%', maxWidth: '900px', marginTop: '50px' }}>
            
            <div className="text-center mb-5">
                <h1 className="mb-1" style={{ color: '#333', fontWeight: 'normal' }}>관리자 사이트</h1>
                <p className="text-muted mb-4">시스템을 관리하고 운영하세요</p>
            </div>
            
            {isAdminLoggedIn ? LoggedInContent : LoginContent}

            <Row>
                {ADMIN_CARDS.map((card, index) => (
                    <AdminCard 
                        key={index} 
                        data={card} 
                        isLoggedIn={isAdminLoggedIn} 
                        onClick={() => handleManageClick(card.path)} // 경로를 직접 전달
                    />
                ))}
            </Row>
        </Container>
    );
}

export default AdminPage;