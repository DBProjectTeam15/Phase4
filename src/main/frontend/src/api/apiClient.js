import axios from 'axios';

// API 명세서에 따른 기본 URL 설정
const API_BASE_URL = 'http://localhost:8080/api'; 

// 사용자/관리자 토큰 키 정의
const USER_TOKEN_KEY = 'user_token'; 
const MANAGER_TOKEN_KEY = 'manager_token'; 

const apiClient = axios.create({
    baseURL: API_BASE_URL, 
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * 1. 요청 인터셉터: 사용자 토큰 또는 관리자 토큰 중 유효한 토큰 삽입
 */
apiClient.interceptors.request.use((config) => {
    // 1. 사용자 토큰 확인
    const userToken = localStorage.getItem(USER_TOKEN_KEY);
    // 2. 관리자 토큰 확인
    const managerToken = localStorage.getItem(MANAGER_TOKEN_KEY);

    let activeToken = null;
    
    // 관리자 토큰이 있으면 관리자 세션을 우선하고, 없으면 사용자 토큰을 사용합니다.
    if (managerToken) {
        activeToken = managerToken;
    } else if (userToken) {
        activeToken = userToken;
    }
    
    if (activeToken) {
        config.headers.Authorization = `Bearer ${activeToken}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

/**
 * 2. 응답 인터셉터: 에러 처리 및 세션 만료 시 리다이렉트 (토큰 키 모두 제거)
 */
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response ? error.response.status : null;
        
        // 401 UNAUTHORIZED 처리: 세션 만료, 로그인 필요
        if (status === 401) { 
            console.error('인증 실패 또는 세션 만료. 로그인 페이지로 리다이렉트합니다.');
            
            // 🚨 중요한 부분: 토큰 저장소 모두 제거
            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem(MANAGER_TOKEN_KEY);
            localStorage.removeItem('user_nickname');
            localStorage.removeItem('manager_id');
            
            // 로그인 페이지로 리다이렉트 로직 필요 (window.location.href 또는 React Router navigate)
            // (여기서는 컴포넌트 외부이므로 주석 처리 유지)
        }
        
        // 403 FORBIDDEN 처리: 접근 권한 없음
        if (status === 403) {
            alert("접근 권한이 없습니다. 권한을 확인해주세요.");
        }

        return Promise.reject(error);
    }
);

export default apiClient;