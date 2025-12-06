import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button, Form, Row, Col, Table, InputGroup, Card, Alert, Spinner } from 'react-bootstrap';
import apiClient from '../api/apiClient.js';

const timeToSeconds = (h, m, s) => {
    return (parseInt(h || 0) * 3600) + (parseInt(m || 0) * 60) + parseInt(s || 0);
};

const SearchResultTable = ({ results, isLoading, error }) => {
    const navigate = useNavigate();

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
                    <Table borderless responsive hover style={{ cursor: 'pointer' }}>
                        <thead style={{ color: '#555' }}>
                        <tr>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '20%' }}>ID</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '55%' }}>플레이리스트명</th>
                            <th className="p-0 pb-2 border-bottom" style={{ width: '25%' }}>협업 여부</th>
                        </tr>
                        </thead>
                        <tbody>
                        {results.map((item) => (
                            <tr key={item.id} onClick={() => navigate(`/playlists/${item.id}`)}>
                                <td className="p-0 py-2">{item.id}</td>
                                <td className="p-0 py-2">{item.title}</td>
                                <td className="p-0 py-2">{item.isCollaborative === "True" ? 'Y' : 'N'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                )}
            </div>
        </div>
    );
};

function PlaylistSearchPage() {
    const navigate = useNavigate();

    const [titleKeyword, setTitleKeyword] = useState('');
    const [titleExact, setTitleExact] = useState(false);
    const [ownerKeyword, setOwnerKeyword] = useState('');
    const [ownerExact, setOwnerExact] = useState(false);

    const [songCountMin, setSongCountMin] = useState('');
    const [songCountMax, setSongCountMax] = useState('');
    const [commentCountMin, setCommentCountMin] = useState('');
    const [commentCountMax, setCommentCountMax] = useState('');

    const [timeMinH, setTimeMinH] = useState('');
    const [timeMinM, setTimeMinM] = useState('');
    const [timeMinS, setTimeMinS] = useState('');
    const [timeMaxH, setTimeMaxH] = useState('');
    const [timeMaxM, setTimeMaxM] = useState('');
    const [timeMaxS, setTimeMaxS] = useState('');

    const [results, setResults] = useState([]);
    const [showResults, setShowResults] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');


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

        const minSongs = parseInt(songCountMin);
        const maxSongs = parseInt(songCountMax);
        if (songCountMin && songCountMax && minSongs > maxSongs) {
            setErrorMessage(minMsg);
            setIsLoading(false);
            return;
        }

        const minComments = parseInt(commentCountMin);
        const maxComments = parseInt(commentCountMax);
        if (commentCountMin && commentCountMax && minComments > maxComments) {
            setErrorMessage(minMsg);
            setIsLoading(false);
            return;
        }

        const totalLengthMin = timeToSeconds(timeMinH, timeMinM, timeMinS);
        const totalLengthMax = timeToSeconds(timeMaxH, timeMaxM, timeMaxS);
        if (totalLengthMin > totalLengthMax) {
            const maxInputsEmpty = !timeMaxH && !timeMaxM && !timeMaxS;
            if (!maxInputsEmpty) {
                setErrorMessage(minMsg);
                setIsLoading(false);
                return;
            }
        }

        // Backend expects: title, songCount, songCountMax, commentCount, commentCountMax, owner, totalLength, sortBy, sortOrder
        const params = new URLSearchParams();

        if (titleKeyword.trim()) {
            params.append('title', titleKeyword.trim());
        }
        if (ownerKeyword.trim()) {
            params.append('owner', ownerKeyword.trim());
        }
        if (minSongs) {
            params.append('songCount', minSongs);
        }
        if (maxSongs) {
            params.append('songCountMax', maxSongs);
        }
        if (minComments) {
            params.append('commentCount', minComments);
        }
        if (maxComments) {
            params.append('commentCountMax', maxComments);
        }
        if (totalLengthMin > 0) {
            params.append('totalLength', totalLengthMin);
        }
        // Default sorting
        params.append('sortBy', 'title');
        params.append('sortOrder', 'asc');

        try {
            const response = await apiClient.get(`/api/playlists/search?${params.toString()}`);

            setResults(response.data || []);

        } catch (err) {
            console.error("플레이리스트 검색 오류:", err.response || err);
            const msg = err.response?.data?.message || "검색 중 오류가 발생했습니다. 필터 조건을 확인해주세요.";
            setErrorMessage(msg);
            setResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    const TimeInputGroup = ({ label, isMin }) => {
        const h = isMin ? timeMinH : timeMaxH;
        const setH = isMin ? setTimeMinH : setTimeMaxH;
        const m = isMin ? timeMinM : timeMaxM;
        const setM = isMin ? setTimeMinM : setTimeMaxM;
        const s = isMin ? timeMinS : timeMaxS;
        const setS = isMin ? setTimeMinS : setTimeMaxS;

        return (
            <Form.Group as={Col} md={6} className="mb-3">
                <Form.Label style={{ fontWeight: 'bold' }}>{label}</Form.Label>
                <Row className="g-2">
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="시" min="0" value={h} onChange={(e) => setH(e.target.value)} />
                            <InputGroup.Text>시</InputGroup.Text>
                        </InputGroup>
                    </Col>
                    <Col>
                        <InputGroup>
                            <Form.Control type="number" placeholder="분" min="0" max="59" value={m} onChange={(e) => setM(e.target.value)} />
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


    return (
        <Container style={{ maxWidth: '900px' }}>
            <div className="mb-4 d-flex align-items-center">
                <Button variant="link" onClick={handleGoBack} className="p-0" style={{ color: '#333' }}>
                    ← 뒤로가기
                </Button>
            </div>

            <h2 className="mb-4" style={{ fontWeight: 'bold' }}>플레이리스트 검색</h2>

            {errorMessage && (
                <Alert
                    variant="danger"
                    className="position-fixed top-0 start-50 translate-middle-x mt-3"
                    style={{ zIndex: 1050, width: '100%', maxWidth: '400px' }}
                    onClose={() => setErrorMessage('')}
                    dismissible
                >
                    {errorMessage}
                </Alert>
            )}

            <Card className="p-4 shadow-sm" style={{ border: 'none', backgroundColor: 'white' }}>
                <Form onSubmit={handleSearch}>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>플레이리스트명</Form.Label>
                        <Form.Control type="text" placeholder="플레이리스트명 입력" className="mb-2" value={titleKeyword} onChange={(e) => setTitleKeyword(e.target.value)}/>
                        <div className="d-flex">
                            <Form.Check type="radio" label="포함" name="nameMatch" id="nameInclude" defaultChecked className="me-3" onChange={() => setTitleExact(false)}/>
                            <Form.Check type="radio" label="완전일치" name="nameMatch" id="nameExact" onChange={() => setTitleExact(true)}/>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>악곡 수</Form.Label>
                        <Row className="g-2 align-items-center">
                            <Col md={5}>
                                <Form.Control
                                    type="number"
                                    placeholder="최소"
                                    min="0"
                                    value={songCountMin}
                                    onChange={(e) => setSongCountMin(e.target.value)}
                                />
                            </Col>
                            <Col md={2} className="text-center text-muted">~</Col>
                            <Col md={5}>
                                <Form.Control
                                    type="number"
                                    placeholder="최대"
                                    min="0"
                                    value={songCountMax}
                                    onChange={(e) => setSongCountMax(e.target.value)}
                                />
                            </Col>
                        </Row>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>댓글 수</Form.Label>
                        <Row className="g-2 align-items-center">
                            <Col md={5}>
                                <Form.Control
                                    type="number"
                                    placeholder="최소"
                                    min="0"
                                    value={commentCountMin}
                                    onChange={(e) => setCommentCountMin(e.target.value)}
                                />
                            </Col>
                            <Col md={2} className="text-center text-muted">~</Col>
                            <Col md={5}>
                                <Form.Control
                                    type="number"
                                    placeholder="최대"
                                    min="0"
                                    value={commentCountMax}
                                    onChange={(e) => setCommentCountMax(e.target.value)}
                                />
                            </Col>
                        </Row>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label style={{ fontWeight: 'bold' }}>소유자 닉네임</Form.Label>
                        <Form.Control type="text" placeholder="소유자 닉네임 입력" className="mb-2" value={ownerKeyword} onChange={(e) => setOwnerKeyword(e.target.value)}/>
                        <div className="d-flex">
                            <Form.Check type="radio" label="포함" name="ownerMatch" id="ownerInclude" defaultChecked className="me-3" onChange={() => setOwnerExact(false)}/>
                            <Form.Check type="radio" label="완전일치" name="ownerMatch" id="ownerExact" onChange={() => setOwnerExact(true)}/>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-5">
                        <Form.Label style={{ fontWeight: 'bold' }}>총 재생시간</Form.Label>
                        <Row>
                            <TimeInputGroup label="최소:" isMin={true} />
                            <TimeInputGroup label="최대:" isMin={false} />
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

export default PlaylistSearchPage;