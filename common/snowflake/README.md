# Snowflake ID 생성기

## 개요

이 라이브러리는 분산 시스템에서 고유한 64비트 ID를 생성하기 위한 Snowflake 알고리즘의 구현체입니다.

## ID 구조

Snowflake ID는 64비트 정수로 다음과 같은 구조를 가집니다:

- 1비트: 미사용 (항상 0)
- 41비트: 타임스탬프 (밀리초 단위, 기준 시간: 2024-01-01 00:00:00 UTC)
- 10비트: 노드 ID (서버/인스턴스 식별자, 0-1023)
- 12비트: 시퀀스 번호 (같은 밀리초 내 생성된 ID 구분, 0-4095)

## 분산 환경에서 고유성 보장

### 노드 ID 할당

분산 환경에서 ID 고유성을 보장하려면 각 서버/인스턴스에 고유한 노드 ID를 할당해야 합니다:

1. **중앙 집중식 할당**: 설정 서버나 데이터베이스에서 노드 ID를 할당
2. **인프라 기반 할당**: 쿠버네티스 포드 번호, AWS 인스턴스 ID 등에서 파생
3. **서비스 디스커버리**: Consul, ZooKeeper 등의 서비스를 사용하여 동적 할당

### 구현 예시

```java
// 기본 생성자: 자동으로 외부 설정에서 노드 ID 가져오기
// 1. 환경 변수 SNOWFLAKE_NODE_ID 확인
// 2. 시스템 프로퍼티 snowflake.node.id 확인
// 3. 설정이 없는 경우 개발 환경으로 간주하고 랜덤 노드 ID 사용
Snowflake snowflake = new Snowflake();

// 명시적으로 노드 ID 직접 할당
Snowflake snowflake = new Snowflake(42); // 노드 ID = 42

// 환경 변수 설정 예시
// $ export SNOWFLAKE_NODE_ID=42

// JVM 시스템 프로퍼티 설정 예시
// $ java -Dsnowflake.node.id=42 -jar application.jar

// 또는 쿠버네티스 환경에서 포드 이름에서 추출
String podName = System.getenv("POD_NAME");
long nodeId = extractNodeIdFromPodName(podName);
Snowflake snowflake = new Snowflake(nodeId);
```

### 주의사항

- 기본 생성자(랜덤 노드 ID)는 개발 환경에서만 사용해야 합니다.
- 프로덕션 환경에서는 항상 명시적으로 노드 ID를 지정하세요.
- 시계 동기화(NTP 등)가 정확히 설정되어 있어야 합니다.
- 서버 시간이 역행하면 ID 충돌이 발생할 수 있으므로 시간 동기화에 주의하세요.

## 사용 예시

```java
// 방법 1: 외부 설정을 통한 자동 노드 ID 할당
// 환경 변수 또는 시스템 프로퍼티에서 값을 읽어옴
Snowflake snowflake = new Snowflake();
long id = snowflake.nextId();

// 방법 2: 명시적 노드 ID 사용
Snowflake explicitSnowflake = new Snowflake(42); // 노드 ID = 42
long explicitId = explicitSnowflake.nextId();

// 외부 설정 방법
// 1. 환경 변수: SNOWFLAKE_NODE_ID=42
// 2. 시스템 프로퍼티: -Dsnowflake.node.id=42
// 3. application.properties: snowflake.node.id=42
```
