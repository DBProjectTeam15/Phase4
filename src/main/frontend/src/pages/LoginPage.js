import React, { useState } from 'react';
import { Card, Form, Button, Row, Col, Container, Spinner } from 'react-bootstrap';
import { useNavigate, useOutletContext } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로 확인

// 사용자 세션 토큰 및 이름이 저장될 localStorage 키
const USER_TOKEN_KEY = 'user_token';
const USER_NICKNAME_KEY = 'user_nickname';


const LoggedInInfoCard = ({ userName, onLogout }) => (
    <Card className="p-4 mb-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
        <h5 className="mb-3" style={{ fontWeight: 'bold' }}>로그인 정보</h5>
        <div className="d-flex justify-content-between align-items-center">
            <div>
                <div style={{ color: '#555', fontSize: '0.9em' }}>로그인된 계정</div>
                <div style={{ fontWeight: 'bold' }}>{userName}</div> 
            </div>
            <Button 
                variant="light" 
                onClick={onLogout}
                style={{ color: 'black', border: '1px solid #ddd' }}
            >
                로그아웃
            </Button>
        </div>
    </Card>
);


function LoginPage() {
    // 최상위 Context에서 로그인 상태와 닉네임을 가져옴 (App.js 등의 상위 컴포넌트에서 관리)
    const { isLoggedIn, setIsLoggedIn, username, setUsername } = useOutletContext(); 
    const navigate = useNavigate(); 
    
    // 폼 입력 상태
    const [emailInput, setEmailInput] = useState('');
    const [passwordInput, setPasswordInput] = useState('');
    const [isLoggingIn, setIsLoggingIn] = useState(false);

    // 1. 🔑 사용자 로그인 처리 (POST /api/auth/login)
    const handleLogin = async (e) => {
        e.preventDefault();

        if (!emailInput || !passwordInput) {
            alert('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        setIsLoggingIn(true);
        try {
            // API 4.1.1 사용자 로그인
            const response = await apiClient.post('/auth/login', {
                email: emailInput,
                password: passwordInput,
            });
            
            // 로그인 성공 (200 OK)
            const userData = response.data.data;

            // ⚠️ API 명세에 토큰 저장 방식이 명확하지 않아 임시 토큰 값 사용
            // 실제 서버는 Set-Cookie 헤더나 body에 토큰을 제공해야 함
            const tokenValue = `user-session-${userData.userld}-${Date.now()}`; 

            localStorage.setItem(USER_TOKEN_KEY, tokenValue); 
            localStorage.setItem(USER_NICKNAME_KEY, userData.nickname);
            
            // 전역 상태 업데이트
            setUsername(userData.nickname);
            setIsLoggedIn(true);
            
            alert(response.data.message || `${userData.nickname}님, 환영합니다!`);
            
        } catch (error) {
            console.error('사용자 로그인 오류:', error.response || error);
            // 401 Unauthorized 처리
            const msg = error.response?.data?.message || '로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.';
            alert(msg);
        } finally {
            setIsLoggingIn(false);
            setPasswordInput(''); // 보안을 위해 비밀번호 입력 필드 초기화
        }
    };
    
    // 2. 🚪 사용자 로그아웃 처리 (POST /api/auth/logout)
    const handleLogout = async () => {
        try {
            // API 4.1.2 사용자 로그아웃 (인증 필요)
            const response = await apiClient.post('/auth/logout');

            alert(response.data.message || '로그아웃 되었습니다.');

        } catch (error) {
            console.error('사용자 로그아웃 오류:', error.response || error);
            // 로그아웃 API가 실패하더라도 클라이언트 세션은 정리하는 것이 일반적
            alert('로그아웃 처리 중 오류가 발생했습니다. 브라우저 세션을 정리합니다.');
        } finally {
            // 성공/실패와 관계없이 클라이언트 측 세션 정보 제거
            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem(USER_NICKNAME_KEY);
            
            // 전역 상태 업데이트
            setIsLoggedIn(false);
            setUsername('');
        }
    };

    const handleGoToMyPage = () => {
        navigate('/mypage'); 
    };
    
    const handleGoToPlaylists = () => {
        navigate('/playlists');
    };

    const handleGoToSearch = () => {
        navigate('/search');
    };

    const infoButtonVariant = isLoggedIn ? 'dark' : 'light';
    const infoButtonStyle = isLoggedIn
        ? { backgroundColor: 'black', color: 'white', padding: '20px' }
        : { backgroundColor: '#f0f0f0', color: '#333', padding: '20px' };

    return (
        <Container style={{ width: '100%', maxWidth: '700px' }}>
            
            {isLoggedIn ? (
                <LoggedInInfoCard userName={username} onLogout={handleLogout} />
            ) : (
                <Card className="p-4 mb-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                    <h5 className="mb-3" style={{ fontWeight: 'bold' }}>로그인</h5>
                    <Form onSubmit={handleLogin}>
                        
                        <Form.Group className="mb-3">
                            <Form.Label>이메일</Form.Label>
                            <Form.Control 
                                type="email" 
                                placeholder="이메일을 입력하세요" 
                                value={emailInput}
                                onChange={(e) => setEmailInput(e.target.value)}
                                style={{ backgroundColor: '#f0f0f0', border: 'none', padding: '12px' }}
                                disabled={isLoggingIn}
                                required
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
                                required
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
            )}

            <Row className="g-3">
                <Col>
                    <Button 
                        variant="dark" 
                        className="w-100" 
                        style={{ backgroundColor: 'black', color: 'white', padding: '20px' }}
                        onClick={handleGoToSearch}
                    >
                        검색하기
                    </Button>
                </Col>

                <Col>
                    <Button 
                        variant={infoButtonVariant} 
                        className="w-100" 
                        style={infoButtonStyle}
                        onClick={isLoggedIn ? handleGoToMyPage : undefined}
                        disabled={!isLoggedIn} // 로그인이 안 되면 비활성화
                    >
                        내 정보 보기
                    </Button>
                </Col>

                <Col>
                    <Button 
                        variant="dark" 
                        className="w-100" 
                        style={{ backgroundColor: 'black', color: 'white', padding: '20px' }}
                        onClick={handleGoToPlaylists}
                    >
                        플레이리스트 찾아보기
                    </Button>
                </Col>
            </Row>
        </Container>
    );
}

export default LoginPage;