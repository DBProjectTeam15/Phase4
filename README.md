# Phase 4

[Phase 3](https://github.com/DBProjectTeam15/Phase3) 에서 추가된 정보
들을 위주로 기술합니다. 전반적으로 추가된 작업은 아래와 같이 정됩니다.


- `Spring Boot` 추가 - 설정 자동화와 웹 컨텐츠 및 API 서버를 위해 추가했습니다.
- `jOOQ` 추가 - Dynamic Query 를 편리하게 하기 위해 추가했습니다.
- `Web Interface` 추가 - `React` 기반의 Web Interface를 추가합니다.

스키마 관련 변경사항은 아래와 같습니다.

- (중요) `Database INDEX` 추가 : 성능병목이 생기는 지점을 최적화하고자했습니다.
- `Transaction` 격리 수준 제어 : 격리수준을 최소한으로 제어하여 성능 병목을 완화하고자 합니다.
