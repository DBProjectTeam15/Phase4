import React, { useState, useEffect } from 'react';
import { Container, Spinner } from 'react-bootstrap';
import { Outlet, useLocation } from 'react-router-dom';
import apiClient from '../api/apiClient'; // apiClient 임포트 경로 확인

// 사용자 세션 토큰 키 (LoginPage.js와 동일해야 함)
const USER_TOKEN_KEY = 'user_token'; 

function MainLayout() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [username, setUsername] = useState('');
    const [isInitialLoading, setIsInitialLoading] = useState(true); // 초기 로딩 상태
    const location = useLocation();

    // 1. 🔑 세션 상태 확인 로직 (GET /api/auth/session)
    const checkUserSession = async () => {
        const token = localStorage.getItem(USER_TOKEN_KEY);
        
        // 토큰 자체가 없으면 로그인 상태가 아님
        if (!token) {
            setIsLoggedIn(false);
            setUsername('');
            setIsInitialLoading(false);
            return;
        }

        try {
            // API 4.1.3 세션 상태 확인 (토큰을 포함하여 요청)
            const response = await apiClient.get('/auth/session'); 
            
            // 응답 구조: { isLoggedIn: boolean, user: { userld, nickname, email } }
            if (response.data.isLoggedIn && response.data.user) {
                const user = response.data.user;
                setIsLoggedIn(true);
                setUsername(user.nickname);
                // 브라우저 리프레시 시 로컬 저장소의 닉네임도 업데이트
                localStorage.setItem('user_nickname', user.nickname); 
            } else {
                // 서버가 토큰은 받았으나 무효하다고 판단 (isLoggedIn: false)
                localStorage.removeItem(USER_TOKEN_KEY);
                localStorage.removeItem('user_nickname');
                setIsLoggedIn(false);
                setUsername('');
            }
        } catch (err) {
            // 401 에러는 apiClient에서 리다이렉트 처리되지만, 기타 네트워크 오류 대비
            console.error("세션 확인 중 오류 발생:", err);
            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem('user_nickname');
            setIsLoggedIn(false);
            setUsername('');
        } finally {
            setIsInitialLoading(false);
        }
    };
    
    useEffect(() => {
        // 앱이 시작될 때 세션 상태를 확인합니다.
        checkUserSession();
    }, []);

    const context = { 
        isLoggedIn, 
        setIsLoggedIn, 
        username, 
        setUsername 
    };
    
    const isHomePage = location.pathname === '/'; 

    return (
        <div className="d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '100vh', backgroundColor: '#f8f9fa' }}>
            
            {isInitialLoading && (
                 <div className="text-center py-5">
                    <Spinner animation="border" /> 
                    <p className="mt-2">세션 확인 중...</p>
                 </div>
            )}
            
            {!isInitialLoading && (
                <Outlet context={context} />
            )}
            
            {isHomePage && !isInitialLoading && (
                <Container className="text-center position-absolute top-0 pt-5">
                    <h1 className="mb-1" style={{ color: '#333', fontWeight: 'normal' }}>사용자 사이트</h1>
                    <p className="text-muted mb-4">플레이리스트를 검색하고 관리하세요</p>
                </Container>
            )}
        </div>
    );
}

export default MainLayout;