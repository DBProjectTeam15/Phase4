import axios from 'axios';

// API 명세서에 따른 기본 URL 설정 (프록시 설정을 위해 빈 문자열로 설정)
const API_BASE_URL = '';

// 사용자/관리자 토큰 키 정의 및 export (일관성 확보)
export const USER_TOKEN_KEY = 'user_token';
export const MANAGER_TOKEN_KEY = 'manager_token';
export const USER_NICKNAME_KEY = 'user_nickname';
export const MANAGER_ID_KEY = 'manager_id';

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

apiClient.interceptors.request.use((config) => {
    const userToken = localStorage.getItem(USER_TOKEN_KEY);
    const managerToken = localStorage.getItem(MANAGER_TOKEN_KEY);

    let activeToken = null;

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

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response ? error.response.status : null;

        if (status === 401) {
            console.error('인증 실패 또는 세션 만료. 로그인 페이지로 리다이렉트합니다.');

            localStorage.removeItem(USER_TOKEN_KEY);
            localStorage.removeItem(MANAGER_TOKEN_KEY);
            localStorage.removeItem(USER_NICKNAME_KEY);
            localStorage.removeItem(MANAGER_ID_KEY);
        }

        if (status === 403) {
            alert("접근 권한이 없습니다. 권한을 확인해주세요.");
        }

        return Promise.reject(error);
    }
);

export default apiClient;