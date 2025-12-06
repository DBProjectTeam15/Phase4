import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Form, Row, Col, Table, InputGroup, Card, Alert, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

const SORT_FIELDS_API = {
    '곡명': 'title',
    '아티스트명': 'artist',
    '재생시간': 'length',
    '발매일': 'date',
    '제공원': 'provider',
};
const SORT_ORDERS_API = {
    '오름차순': 'ASC',
    '내림차순': 'DESC',
};

const SORT_FIELDS_UI = Object.keys(SORT_FIELDS_API);
const SORT_ORDERS_UI = Object.keys(SORT_ORDERS_API);

const durationToSeconds = (m, s) => (parseInt(m || 0) * 60) + parseInt(s || 0);

const formatDate = (y, m, d) => {
    if (!y && !m && !d) return null;
    if (!y || !m || !d) return 'INVALID';

    const year = y.padStart(4, '0');
    const month = m.padStart(2, '0');
    const day = d.padStart(2, '0');

    return `${year}-${month}-${day}`;
};

const SearchResultTable = ({ results, isLoading, error }) => {
    const handleLinkClick = (link) => {
        if (link) window.open(link, '_blank');
    };

    if (isLoading) {
        return <div className="mt-5 text-center py-3"><Spinner animation="border" size="sm" /> <p className="mt-2">검색 중...</p></div>;
    }
    if (error) {
        return <Alert variant="danger" className="mt-5">{error}</Alert>;
    }

    return (
        <div className="mt-5">
            <h4 style={{ fontWeight: 'bold' }}>검색 결과 ({results.length}건)</h4>
            <div className="mt-3 p-4" style={{ backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                {results.length === 0 ? (
                    <Alert variant="info" className="mb-0">검색 결과가 없습니다.</Alert>
                ) : (
                    <Table borderless responsive>
                        <thead style={{ color: '#555' }}>
                        <tr>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '10%' }}>ID</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '20%' }}>곡명</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '20%' }}>아티스트</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '10%' }}>재생시간</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '15%' }}>발매일</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '15%' }}>제공원</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '10%' }}>링크</th>
                        </tr>
                        </thead>
                        <tbody>
                        {results.map((item) => (
                            <tr key={item.id}>
                                <td className="p-0 py-2">{item.id}</td>
                                <td className="p-0 py-2">{item.title}</td>
                                <td className="p-0 py-2">{item.artistName}</td>
                                <td className="p-0 py-2">{Math.floor(item.length / 60)}:{String(item.length % 60).padStart(2, '0')}</td>
                                <td className="p-0 py-2">{item.createAt}</td>
                                <td className="p-0 py-2">{item.providerName}</td>
                                <td className="p-0 py-2">
                                    <Button
                                        variant="link"
                                        onClick={() => handleLinkClick(item.playLink)}
                                        className="p-0"
                                        style={{ color: '#007bff', textDecoration: 'none', fontSize: '0.9em' }}
                                    >
                                        링크 {'⇗'}
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                )}
            </div>
        </div>
    );
};

function SongSearchPage() {
    const navigate = useNavigate();

    const [titleKeyword, setTitleKeyword] = useState('');
    const [titleExact, setTitleExact] = useState(false);
    const [artistKeyword, setArtistKeyword] = useState('');
    const [artistExact, setArtistExact] = useState(false);
    const [providerKeyword, setProviderKeyword] = useState('');
    const [providerExact, setProviderExact] = useState(false);

    const [durationMinM, setDurationMinM] = useState('');
    const [durationMinS, setDurationMinS] = useState('');
    const [durationMaxM, setDurationMaxM] = useState('');
    const [durationMaxS, setDurationMaxS] = useState('');

    const [releaseMinY, setReleaseMinY] = useState('');
    const [releaseMinM, setReleaseMinM] = useState('');
    const [releaseMinD, setReleaseMinD] = useState('');
    const [releaseMaxY, setReleaseMaxY] = useState('');
    const [releaseMaxM, setReleaseMaxM] = useState('');
    const [releaseMaxD, setReleaseMaxD] = useState('');

    const [orderBy, setOrderBy] = useState(SORT_FIELDS_UI[0]);
    const [orderDir, setOrderDir] = useState(SORT_ORDERS_UI[0]);

    const [results, setResults] = useState([]);
    const [showResults, setShowResults] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const isValidDate = (y, m, d) => {
        if (!y && !m && !d) return true;
        if (!y || !m || !d) return false;
        const date = new Date(y, m - 1, d);
        return date.getFullYear() === parseInt(y) && date.getMonth() === parseInt(m) - 1 && date.getDate() === parseInt(d);
    };

    const dateToTimestamp = (y, m, d) => {
        if (!y || !m || !d) return NaN;
        const date = new Date(y, m - 1, d);
        return date.getTime();
    };


    const handleGoBack = () => {
        navigate(-1);
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setShowResults(true);
        setIsLoading(true);
        setResults([]);

        const minMsg = '최소값과 최대값 설정이 잘못되었습니다.';
        const dateMsg = '발매일 날짜 형식이 잘못되었습니다.';

        const totalDurationMin = durationToSeconds(durationMinM, durationMinS);
        const totalDurationMax = durationToSeconds(durationMaxM, durationMaxS);
        if (totalDurationMin > totalDurationMax && (durationMaxM || durationMaxS)) {
            setErrorMessage(minMsg);
            setIsLoading(false);
            return;
        }

        if (!isValidDate(releaseMinY, releaseMinM, releaseMinD) || !isValidDate(releaseMaxY, releaseMaxM, releaseMaxD)) {
            setErrorMessage(dateMsg);
            setIsLoading(false);
            return;
        }

        const dateMin = formatDate(releaseMinY, releaseMinM, releaseMinD);
        const dateMax = formatDate(releaseMaxY, releaseMaxM, releaseMaxD);

        const minTime = dateToTimestamp(releaseMinY, releaseMinM, releaseMinD);
        const maxTime = dateToTimestamp(releaseMaxY, releaseMaxM, releaseMaxD);

        if (dateMin && dateMax && minTime > maxTime) {
            setErrorMessage(minMsg);
            setIsLoading(false);
            return;
        }

        // Backend expects: title, exactTitle, artistName, exactArtist, minTime, maxTime,
        // songName (provider name), exactsong, minDate, maxDate, sortBy, sortOrder
        const params = new URLSearchParams();

        if (titleKeyword.trim()) {
            params.append('title', titleKeyword.trim());
            params.append('exactTitle', titleExact);
        }
        if (artistKeyword.trim()) {
            params.append('artistName', artistKeyword.trim());
            params.append('exactArtist', artistExact);
        }
        if (providerKeyword.trim()) {
            params.append('songName', providerKeyword.trim());
            params.append('exactsong', providerExact);
        }
        if (totalDurationMin > 0) {
            params.append('minTime', totalDurationMin);
        }
        if (totalDurationMax > 0) {
            params.append('maxTime', totalDurationMax);
        }
        if (dateMin && dateMin !== 'INVALID') {
            params.append('minDate', dateMin);
        }
        if (dateMax && dateMax !== 'INVALID') {
            params.append('maxDate', dateMax);
        }

        params.append('sortBy', SORT_FIELDS_API[orderBy]);
        params.append('sortOrder', SORT_ORDERS_API[orderDir]);

        try {
            const response = await apiClient.get(`/api/songs/search?${params.toString()}`);

            setResults(response.data || []);

        } catch (err) {
            console.error("곡 검색 오류:", err.response || err);
            const msg = err.response?.data?.message || "검색 중 오류가 발생했습니다. 필터 조건을 확인해주세요.";
            setErrorMessage(msg);
            setResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    const DurationInputGroup = ({ label, isMin }) => {
        const m = isMin ? durationMinM : durationMaxM;
        const setM = isMin ? setDurationMinM : setDurationMaxM;
        const s = isMin ? durationMinS : durationMaxS;
        const setS = isMin ? setDurationMinS : setDurationMaxS;

        return (
            <Form.Group as={Col} md={6} className="mb-3">
                <Form.Label style={{ fontWeight: 'bold' }}>{label}</Form.Label>
                <Row className="g-2">
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="분" min="0" value={m} onChange={(e) => setM(e.target.value)} />
                            <InputGroup.Text>분</InputGroup.Text>
                        </InputGroup>
                    </Col>
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="초" min="0" max="59" value={s} onChange={(e) => setS(e.target.value)} />
                            <InputGroup.Text>초</InputGroup.Text>
                        </InputGroup>
                    </Col>
                </Row>
            </Form.Group>
        );
    };

    const ReleaseDateInputGroup = ({ label, isMin }) => {
        const y = isMin ? releaseMinY : releaseMaxY;
        const setY = isMin ? setReleaseMinY : setReleaseMaxY;
        const m = isMin ? releaseMinM : releaseMaxM;
        const setM = isMin ? setReleaseMinM : setReleaseMaxM;
        const d = isMin ? releaseMinD : releaseMaxD;
        const setD = isMin ? setReleaseMinD : setReleaseMaxD;

        return (
            <Form.Group as={Col} md={6} className="mb-3">
                <Form.Label style={{ fontWeight: 'bold' }}>{label}</Form.Label>
                <Row className="g-2">
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="년" max="2100" value={y} onChange={(e) => setY(e.target.value)} />
                            <InputGroup.Text>년</InputGroup.Text>
                        </InputGroup>
                    </Col>
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="월" min="1" max="12" value={m} onChange={(e) => setM(e.target.value)} />
                            <InputGroup.Text>월</InputGroup.Text>
                        </InputGroup>
                    </Col>
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="일" min="1" max="31" value={d} onChange={(e) => setD(e.target.value)} />
                            <InputGroup.Text>일</InputGroup.Text>
                        </InputGroup>
                    </Col>
                </Row>
            </Form.Group>
        );
    };


    return (
        <Container style={{ maxWidth: '900px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>

            <h2 className="mb-4" style={{ fontWeight: 'bold' }}>악곡 검색</h2>

            {errorMessage && (
                <Alert
                    variant="danger"
                    className="position-fixed top-0 start-50 translate-middle-x mt-3"
                    style={{ zIndex: 2000, width: '100%', maxWidth: '400px' }}
                    onClose={() => setErrorMessage('')}
                    dismissible
                >
                    {errorMessage}
                </Alert>
            )}

            <Card className="p-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                <Form onSubmit={handleSearch}>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>곡명</Form.Label>
                        <Form.Control type="text" placeholder="곡명 입력" className="mb-2" value={titleKeyword} onChange={(e) => setTitleKeyword(e.target.value)}/>
                        <div className="d-flex">
                            <Form.Check type="radio" label="포함" name="songMatch" id="songInclude" defaultChecked className="me-3" onChange={() => setTitleExact(false)}/>
                            <Form.Check type="radio" label="완전일치" name="songMatch" id="songExact" onChange={() => setTitleExact(true)}/>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>아티스트명</Form.Label>
                        <Form.Control type="text" placeholder="아티스트명 입력" className="mb-2" value={artistKeyword} onChange={(e) => setArtistKeyword(e.target.value)}/>
                        <div className="d-flex">
                            <Form.Check type="radio" label="포함" name="artistMatch" id="artistInclude" defaultChecked className="me-3" onChange={() => setArtistExact(false)}/>
                            <Form.Check type="radio" label="완전일치" name="artistMatch" id="artistExact" onChange={() => setArtistExact(true)}/>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>재생시간</Form.Label>
                        <Row>
                            <DurationInputGroup label="최소:" isMin={true} />
                            <DurationInputGroup label="최대:" isMin={false} />
                        </Row>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>제공원</Form.Label>
                        <Form.Control type="text" placeholder="제공원 입력 (youtube, spotify)" className="mb-2" value={providerKeyword} onChange={(e) => setProviderKeyword(e.target.value)}/>
                        <div className="d-flex">
                            <Form.Check type="radio" label="포함" name="sourceMatch" id="sourceInclude" defaultChecked className="me-3" onChange={() => setProviderExact(false)}/>
                            <Form.Check type="radio" label="완전일치" name="sourceMatch" id="sourceExact" onChange={() => setProviderExact(true)}/>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-5">
                        <Form.Label style={{ fontWeight: 'bold' }}>발매일</Form.Label>
                        <Row>
                            <ReleaseDateInputGroup label="최소:" isMin={true} />
                            <ReleaseDateInputGroup label="최대:" isMin={false} />
                        </Row>
                    </Form.Group>

                    {/* 정렬 기준 */}
                    <Form.Group className="mb-5">
                        <Form.Label style={{ fontWeight: 'bold' }}>정렬 기준</Form.Label>
                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Label className="text-muted" style={{ fontSize: '0.8em' }}>정렬 필드</Form.Label>
                                <Form.Select value={orderBy} onChange={(e) => setOrderBy(e.target.value)}>
                                    {SORT_FIELDS_UI.map(field => <option key={field} value={field}>{field}</option>)}
                                </Form.Select>
                            </Col>
                            <Col md={6}>
                                <Form.Label className="text-muted" style={{ fontSize: '0.8em' }}>정렬 순서</Form.Label>
                                <Form.Select value={orderDir} onChange={(e) => setOrderDir(e.target.value)}>
                                    {SORT_ORDERS_UI.map(order => <option key={order} value={order}>{order}</option>)}
                                </Form.Select>
                            </Col>
                        </Row>
                    </Form.Group>

                    <Button variant="dark" type="submit" className="w-100" style={{ backgroundColor: 'black', color: 'white', padding: '12px' }} disabled={isLoading}>
                        {isLoading ? <><Spinner animation="border" size="sm" className="me-2" /> 검색 중...</> : '검색'}
                    </Button>
                </Form>
            </Card>

            {showResults && <SearchResultTable results={results} isLoading={isLoading} error={errorMessage} />}

        </Container>
    );
}

export default SongSearchPage;