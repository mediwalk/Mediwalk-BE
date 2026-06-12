# EC2 Docker 배포 가이드

mediwalk-be를 기존 JAR + systemd 방식에서 Docker(Compose) 방식으로 전환하기 위한 절차입니다.

## 사전 조건

- EC2 호스트에 Docker, Docker Compose 플러그인 설치됨 (`docker compose version`으로 확인)
- MySQL은 EC2 호스트에 그대로 설치되어 있고, 컨테이너는 호스트의 MySQL에 접속함 (컨테이너 내부에 MySQL을 새로 띄우지 않음)
- `/home/ubuntu/secrets/gcp-key.json` — Firebase / Vertex AI(의약품 검증)용 GCP 서비스 계정 키
- `/home/ubuntu/mediwalk.env` — `TMAP_APP_KEY`, `GCP_*`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS` 등 환경 변수 파일
- nginx는 `localhost:8080` → `https://api.mediwalk.site` 로 그대로 프록시 (변경 없음)

## 1. 이미지 빌드

```bash
cd /home/ubuntu/mediwalk-be   # 저장소 루트 (Dockerfile, docker-compose.yml 위치)
git pull
docker compose build
```

> 빌드 스테이지에서 `./gradlew bootJar`를 실행합니다. EC2 인스턴스 사양이 작아 빌드 중 메모리가
> 부족하면 swap을 추가하거나, 더 큰 인스턴스/별도 빌드 환경에서 이미지를 빌드 후
> `docker save` / `docker load`로 옮기는 방법을 검토하세요.

## 2. 환경 변수 / 시크릿 확인

`/home/ubuntu/mediwalk.env`에 최소한 다음 값들이 있어야 합니다 (기존 systemd 배포에서 쓰던 값과 동일):

- `SPRING_PROFILES_ACTIVE` (필요 시)
- `DB_PASSWORD` (또는 `SPRING_DATASOURCE_PASSWORD`)
- `TMAP_APP_KEY`
- `FIREBASE_ENABLED`, `FIREBASE_PROJECT_ID`
- `GCP_PROJECT_ID`, `GCP_REGION`, `GCP_ENDPOINT_ID` 등 Vertex AI 관련 값
- `CORS_ALLOWED_ORIGINS`

`docker-compose.yml`은 이 파일을 `env_file`로 그대로 읽고, 아래 3개 값만 컨테이너 환경에 맞게
**덮어씁니다** (mediwalk.env에 다른 값이 있어도 무시됨):

| 환경 변수 | 값 | 이유 |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host.docker.internal:3306/mediwalk?...` | 컨테이너 안에서 `localhost`는 컨테이너 자신을 가리키므로 호스트 MySQL로 우회 필요 |
| `GCP_CREDENTIALS_PATH` | `/app/secrets/gcp-key.json` | 컨테이너 내부 마운트 경로 |
| `FIREBASE_CREDENTIALS_PATH` | `/app/secrets/gcp-key.json` | 컨테이너 내부 마운트 경로 |

`SPRING_DATASOURCE_URL`을 EC2 프라이빗 IP로 쓰고 싶다면, `docker compose up` 실행 전에
셸에서 `export SPRING_DATASOURCE_URL="jdbc:mysql://<EC2_PRIVATE_IP>:3306/mediwalk?..."` 로
지정하면 compose 파일의 기본값(`host.docker.internal`)을 덮어씁니다.

## 3. GCP 키 마운트

`docker-compose.yml`은 호스트의 `/home/ubuntu/secrets/gcp-key.json`을 컨테이너의
`/app/secrets/gcp-key.json`(읽기 전용)으로 마운트합니다. `mediwalk.env`에 적힌
`GCP_CREDENTIALS_PATH`/`FIREBASE_CREDENTIALS_PATH`가 호스트 경로를 가리키더라도,
compose의 `environment` 설정이 `/app/secrets/gcp-key.json`으로 강제 override하므로
별도 수정이 필요 없습니다.

## 4. 기존 systemd JAR 서비스 중지

Docker 컨테이너와 기존 JAR 프로세스가 같은 8080 포트를 동시에 점유할 수 없으므로,
컨테이너 기동 전에 반드시 기존 서비스를 멈춰야 합니다.

```bash
sudo systemctl status mediwalk-be   # 실제 유닛 이름 확인 (예: mediwalk-be.service)
sudo systemctl stop mediwalk-be
sudo systemctl disable mediwalk-be   # 재부팅 시 JAR가 다시 뜨지 않도록 (선택)
```

> 유닛 이름이 다르면 `systemctl list-units | grep mediwalk` 등으로 실제 이름을 확인하세요.

## 5. 컨테이너 기동

```bash
docker compose up -d
docker compose logs -f app   # Ctrl+C로 로그 추적 종료
```

정상 기동 시 Spring Boot 배너와 `Started MediwalkBeApplication` 로그, 포트 8080 바인딩
로그가 출력됩니다.

## 6. 동작 확인 체크리스트

이 프로젝트에는 `spring-boot-starter-actuator`가 포함되어 있지 않으므로 `/actuator/health`는
사용할 수 없습니다. 대신 아래 항목으로 확인합니다.

1. **포트 충돌 / 컨테이너 기동 확인**
   ```bash
   docker compose ps              # app 서비스가 Up 상태인지
   sudo ss -tlnp | grep :8080     # 8080을 점유한 프로세스가 docker-proxy(컨테이너) 하나뿐인지 확인
   sudo systemctl status mediwalk-be   # inactive(dead)인지 — JAR가 같이 떠있으면 포트 충돌 발생
   ```
   - `ss` 결과에 `java`(systemd JAR)와 `docker-proxy`가 동시에 8080을 잡고 있으면
     컨테이너가 기동에 실패했거나(`docker compose logs app`에 `Address already in use`),
     systemd 서비스 중지가 안 된 것입니다. 4단계로 돌아가 systemd를 먼저 멈추세요.

2. **컨테이너 → 호스트 MySQL 연결 확인**
   ```bash
   docker compose logs app | grep -i "HikariPool\|Started Mediwalk"
   ```
   - `HikariPool-1 - Start completed` 같은 로그가 보이고 `Started MediwalkBeApplication`이
     출력되면 DB 연결 성공. 연결 실패 시 `Communications link failure` 등의 에러가 보이면
     `SPRING_DATASOURCE_URL`(host.docker.internal / EC2 private IP)과 MySQL의
     `bind-address`, 호스트 방화벽(3306)을 확인하세요.

3. **로컬(컨테이너 직접) API 확인**
   ```bash
   curl -i http://localhost:8080/api/missions
   ```
   - `permitAll` 설정이므로 인증 없이 200 + JSON 배열이 와야 합니다. nginx를 거치지 않고
     컨테이너 자체가 정상 응답하는지 먼저 확인하는 단계입니다.

4. **nginx 경유 외부 API 확인**
   ```bash
   curl -i https://api.mediwalk.site/api/missions
   ```
   - nginx 설정은 변경하지 않으므로 (`localhost:8080`으로 그대로 프록시) 3번이 성공했다면
     이 단계도 동일하게 200이 나와야 합니다. 200이 아니거나 502가 나오면 nginx → 8080
     프록시 설정 자체보다는, 1번(포트 점유)·3번(컨테이너 기동) 단계를 다시 확인하세요.

5. **Swagger / 라우트 생성 등 외부 연동 확인 (선택)**
   ```bash
   curl -i https://api.mediwalk.site/swagger-ui/index.html   # 200 또는 302
   ```
   - Tmap/Firebase/GCP 키가 정상 마운트/주입되었는지까지 보고 싶다면, 실제 라우트 생성
     API(`POST /api/routes/generate`)나 로그인 API를 Flutter 앱 또는 curl로 한 번
     호출해 503(키 미설정) 응답이 없는지 확인하세요.

모든 항목이 정상이면 Flutter 앱은 코드 변경 없이 동일한 `https://api.mediwalk.site`
엔드포인트를 계속 사용합니다.

## 7. 롤백

컨테이너 기동에 문제가 있어 기존 JAR 방식으로 즉시 되돌려야 하는 경우:

```bash
docker compose down
sudo systemctl start mediwalk-be
sudo systemctl status mediwalk-be
```

`docker compose down`은 컨테이너만 정리하며 이미지(`mediwalk-be:latest`)는 남아있으므로,
원인 파악 후 다시 `docker compose up -d`로 재시도할 수 있습니다.
