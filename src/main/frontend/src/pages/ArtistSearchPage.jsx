import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Form, Row, Col, Table, Card, Spinner, Alert } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

const GENDER_OPTIONS = ['전체', '남성', '여성'];
const ROLE_OPTIONS = ['전체', '가수', '작곡가', '작사가'];

const mapGenderToApi = (uiGender) => {
    switch (uiGender) {
        case '남성': return 'M';
        case '여성': return 'F';
        case '전체': return null;
        default: return null;
    }
};

const mapGenderToUi = (apiGender) => {
    switch (apiGender) {
        case 'M': return '남성';
        case 'F': return '여성';
        case 'None': return '선택 안함';
        default: return '전체';
    }
};

const SearchResultTable = ({ results, isLoading, error }) => (
    <div className="mt-5">
        <h4 style={{ fontWeight: 'bold' }}>검색 결과 ({isLoading ? '로딩 중' : results.length + '건'})</h4>
        <div className="mt-3 p-4" style={{ backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            {isLoading ? (
                <div className="text-center py-3"><Spinner animation="border" size="sm" /> <p className="mt-2">검색 중...</p></div>
            ) : error ? (
                <Alert variant="danger">{error}</Alert>
            ) : results.length === 0 ? (
                <Alert variant="info" className="mb-0">검색 결과가 없습니다.</Alert>
            ) : (
                <Table borderless responsive>
                    <thead style={{ color: '#555' }}>
                    <tr>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '30%' }}>아티스트 ID</th>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '40%' }}>아티스트명</th>
                        <th className="p-0 pb-2 border-bottom" style={{ width: '30%' }}>성별</th>
                    </tr>
                    </thead>
                    <tbody>
                    {results.map((item) => (
                        <tr key={item.id}>
                            <td className="p-0 py-2">{item.id}</td>
                            <td className="p-0 py-2">{item.name}</td>
                            <td className="p-0 py-2">{mapGenderToUi(item.gender)}</td>
                        </tr>
                    ))}
                    </tbody>
                </Table>
            )}
        </div>
    </div>
);

function ArtistSearchPage() {
    const navigate = useNavigate();

    const [nameKeyword, setNameKeyword] = useState('');
    const [nameExact, setNameExact] = useState(false);
    const [gender, setGender] = useState('전체');
    const [role, setRole] = useState('전체');

    const [results, setResults] = useState([]);
    const [showResults, setShowResults] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleGoBack = () => {
        navigate(-1);
    };

    // 1. 🔍 아티스트 검색 실행 (GET /api/artists/search)
    const handleSearch = async (e) => {
        e.preventDefault();

        setIsLoading(true);
        setError(null);
        setShowResults(true);

        // Backend expects: name, gender, role, sortBy, sortOrder
        const params = new URLSearchParams();

        if (nameKeyword.trim()) {
            params.append('name', nameKeyword.trim());
        }

        const apiGender = mapGenderToApi(gender);
        if (apiGender) {
            params.append('gender', apiGender);
        }

        if (role !== '전체') {
            params.append('role', role);
        }

        // Default sorting
        params.append('sortBy', 'name');
        params.append('sortOrder', 'asc');

        try {
            const response = await apiClient.get(`/api/artists/search?${params.toString()}`);

            setResults(response.data || []);

        } catch (err) {
            console.error("아티스트 검색 오류:", err.response || err);
            const msg = err.response?.data?.message || "검색 중 오류가 발생했습니다. 필터 조건을 확인해주세요.";
            setError(msg);
            setResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Container style={{ maxWidth: '900px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>

            <h2 className="mb-4" style={{ fontWeight: 'bold' }}>아티스트 검색</h2>

            <Card className="p-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                <Form onSubmit={handleSearch}>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>아티스트명</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="아티스트명 입력"
                            className="mb-2"
                            value={nameKeyword}
                            onChange={(e) => setNameKeyword(e.target.value)}
                        />
                        <div className="d-flex">
                            <Form.Check
                                type="radio"
                                label="포함"
                                name="artistNameMatch"
                                id="artistNameInclude"
                                defaultChecked
                                className="me-3"
                                onChange={() => setNameExact(false)}
                            />
                            <Form.Check
                                type="radio"
                                label="완전일치"
                                name="artistNameMatch"
                                id="artistNameExact"
                                onChange={() => setNameExact(true)}
                            />
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>성별</Form.Label>
                        <Form.Select value={gender} onChange={(e) => setGender(e.target.value)}>
                            {GENDER_OPTIONS.map(genderOption => <option key={genderOption} value={genderOption}>{genderOption}</option>)}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-5">
                        <Form.Label style={{ fontWeight: 'bold' }}>역할</Form.Label>
                        <Form.Select value={role} onChange={(e) => setRole(e.target.value)}>
                            {ROLE_OPTIONS.map(roleOption => <option key={roleOption} value={roleOption}>{roleOption}</option>)}
                        </Form.Select>
                    </Form.Group>

                    <Button variant="dark" type="submit" className="w-100" style={{ backgroundColor: 'black', color: 'white', padding: '12px' }} disabled={isLoading}>
                        {isLoading ? '검색 중...' : '검색'}
                    </Button>
                </Form>
            </Card>

            {showResults && <SearchResultTable results={results} isLoading={isLoading} error={error} />}

        </Container>
    );
}

export default ArtistSearchPage;