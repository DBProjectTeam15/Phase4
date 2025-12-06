import React, { useState, useEffect } from 'react';
import { Container, Spinner } from 'react-bootstrap';
import { Outlet, useLocation } from 'react-router-dom';
import apiClient, { USER_TOKEN_KEY, USER_NICKNAME_KEY } from '../api/apiClient.js';

function MainLayout() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [username, setUsername] = useState('');
    const [isInitialLoading, setIsInitialLoading] = useState(true);
    const location = useLocation();

    const checkUserSession = async () => {
        const token = localStorage.getItem(USER_TOKEN_KEY);

        if (!token) {
            setIsLoggedIn(false);
            setUsername('');
            setIsInitialLoading(false);
            return;
        }

        try {
            const response = await apiClient.get('/api/my');

            if (response.data.isLoggedIn && response.data.user) {
                const user = response.data.user;
                setIsLoggedIn(true);
                setUsername(user.nickname);
                // 로컬 저장소 닉네임 키를 상수(USER_NICKNAME_KEY)로 통일
                localStorage.setItem(USER_NICKNAME_KEY, user.nickname);
            } else {
                localStorage.removeItem(USER_TOKEN_KEY);
                localStorage.removeItem(USER_NICKNAME_KEY); // 상수 사용
                setIsLoggedIn(false);
                setUsername('');
            }
        } catch (err) {
            console.error("세션 확인 중 오류 발생:", err);
            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem(USER_NICKNAME_KEY); // 상수 사용
            setIsLoggedIn(false);
            setUsername('');
        } finally {
            setIsInitialLoading(false);
        }
    };

    useEffect(() => {
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