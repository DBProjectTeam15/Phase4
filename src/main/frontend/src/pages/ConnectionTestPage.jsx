import React, { useState, useEffect } from 'react';
import apiClient from '../api/apiClient';

function ConnectionTestPage() {
    const [message, setMessage] = useState('백엔드와 통신 시도 중...');

    useEffect(() => {
        apiClient.get('/api/hello')
            .then(response => {
                setMessage('✅ 통합 성공! 백엔드 메시지: ' + response.data);
            })
            .catch(error => {
                console.error('API 호출 오류:', error);
                setMessage('❌ 통신 실패: 서버 응답 또는 네트워크 상태를 확인하세요.');
            });
    }, []);

    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
            <h2>[백엔드 연결 테스트]</h2>
            <p style={{ fontSize: '1.2em', fontWeight: 'bold' }}>{message}</p>
            <p>이 페이지는 테스트가 완료되면 삭제하거나 경로에서 제거할 예정입니다.</p>
        </div>
    );
}
export default ConnectionTestPage;