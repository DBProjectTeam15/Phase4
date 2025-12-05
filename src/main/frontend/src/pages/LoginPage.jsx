import React, { useState } from 'react';
import { Card, Form, Button, Row, Col, Container, Spinner } from 'react-bootstrap';
import { useNavigate, useOutletContext } from 'react-router-dom';
import apiClient, { USER_TOKEN_KEY, USER_NICKNAME_KEY } from '../api/apiClient.js';


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
    const { isLoggedIn, setIsLoggedIn, username, setUsername } = useOutletContext();
    const navigate = useNavigate();

    const [emailInput, setEmailInput] = useState('');
    const [passwordInput, setPasswordInput] = useState('');
    const [isLoggingIn, setIsLoggingIn] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();

        if (!emailInput || !passwordInput) {
            alert('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        setIsLoggingIn(true);
        try {
            // API 경로 수정: /login -> /api/login
            const response = await apiClient.post('/api/login', {
                email: emailInput,
                password: passwordInput,
            });

            const userData = response.data.data;

            const tokenValue = `user-session-${userData.userld}-${Date.now()}`;

            localStorage.setItem(USER_TOKEN_KEY, tokenValue);
            localStorage.setItem(USER_NICKNAME_KEY, userData.nickname);

            setUsername(userData.nickname);
            setIsLoggedIn(true);

            alert(response.data.message || `${userData.nickname}님, 환영합니다!`);

        } catch (error) {
            console.error('사용자 로그인 오류:', error.response || error);
            const msg = error.response?.data?.message || '로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.';
            alert(msg);
        } finally {
            setIsLoggingIn(false);
            setPasswordInput('');
        }
    };

    const handleLogout = async () => {
        try {
            // API 경로 수정: /auth/logout -> /api/logout
            const response = await apiClient.post('/api/logout');

            alert(response.data.message || '로그아웃 되었습니다.');

        } catch (error) {
            console.error('사용자 로그아웃 오류:', error.response || error);
            alert('로그아웃 처리 중 오류가 발생했습니다. 브라우저 세션을 정리합니다.');
        } finally {
            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem(USER_NICKNAME_KEY);

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
                        disabled={!isLoggedIn}
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