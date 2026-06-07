# 기도제목 Android — GitHub에서 APK 만들기 (컴퓨터에 설치 불필요)

이 프로젝트를 GitHub에 올리면 **GitHub가 서버에서 APK를 자동으로 빌드**합니다.
내 컴퓨터에는 Android Studio도, 안드로이드 SDK도 깔 필요가 없습니다.

빌드 설정(Gradle/매니페스트/아이콘/워크플로)이 모두 들어 있어서, 올리기만 하면 됩니다.

---

## 1. 새 저장소 만들기

1. github.com 로그인 → 오른쪽 위 **+** ▸ **New repository**
2. 이름 예: `prayer-android`, **Public** 선택 → **Create repository**

## 2. 프로젝트 파일 올리기

받은 `PrayerJournalAndroid` 폴더의 **안쪽 내용물**을 올립니다.

1. 저장소 화면에서 **Add file ▸ Upload files**
2. Finder에서 `PrayerJournalAndroid` 폴더를 연 뒤, 그 안의
   **`app` 폴더, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`** 를
   업로드 영역으로 끌어다 놓습니다. (폴더를 드래그하면 하위 구조가 유지됩니다.)
3. **Commit changes** 클릭

> 참고: 워크플로 파일(`.github/...`)은 점(.)으로 시작해 Finder에서 숨겨져 있어
> 드래그가 까다롭습니다. 그래서 3단계에서 GitHub 안에서 직접 만듭니다.

## 3. 빌드 워크플로 파일 만들기

1. 저장소에서 **Add file ▸ Create new file**
2. 파일 이름 칸에 정확히 입력: `.github/workflows/build-apk.yml`
   (슬래시를 입력하면 폴더가 자동으로 만들어집니다.)
3. 받은 `build-apk.yml` 의 내용을 그대로 복사해 붙여넣습니다.
4. **Commit changes** 클릭

→ 커밋하는 순간 빌드가 자동으로 시작됩니다.

## 4. 빌드 결과(APK) 받기

1. 저장소 상단의 **Actions** 탭 클릭
2. 방금 실행된 **Build APK** 작업을 클릭 (노란 점=진행 중, 초록 체크=완료. 보통 2~5분)
3. 완료되면 화면 아래 **Artifacts** 의 **`prayer-journal-apk`** 를 클릭해 다운로드
4. 받은 zip을 풀면 **`app-debug.apk`** 가 들어 있습니다

## 5. 폰에 설치

1. `app-debug.apk` 를 폰으로 옮깁니다 (Google Drive·이메일·USB 등)
2. 폰에서 그 파일을 탭 → "이 출처의 앱 설치 허용" 을 물으면 **허용**
3. **설치** → 끝. **만료 없이** 계속 쓸 수 있습니다.

---

## 이름 바꾸기

- **앱 안 이름**: `app/src/main/java/com/example/prayer/AppConfig.kt` 의
  `const val OWNER_NAME = "김은생"` 한 줄
- **홈 화면 라벨**: `app/src/main/res/values/strings.xml` 의 `app_name`

GitHub 안에서 해당 파일을 열고 연필 아이콘으로 고쳐 커밋하면, 다음 빌드부터 반영됩니다.

## 빌드가 실패하면

- Actions 탭에서 빨간 X 가 뜬 작업을 열고, 빨간 단계를 펼치면 오류 로그가 보입니다.
- 그 **마지막 빨간 줄 몇 개**를 복사해서 알려주시면, 버전 설정을 맞춰 고쳐 드리겠습니다.
  (클라우드 빌드는 버전 조합이 민감해서 처음에 한두 번 조정이 필요할 수 있습니다.)
