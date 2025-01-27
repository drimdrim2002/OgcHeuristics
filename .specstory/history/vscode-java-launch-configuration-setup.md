## SpecStory

## VSCode Java Launch Configuration Setup (1/26/2025, 7:34:02 PM)

_**User**_

vscode에서 java를 실행하는 launch.json을 설정하고 싶습니다.
실행할 파일은 test\java\에 있는 PlannerMainTest.java입니다.

---

_**Assistant**_

launch.json 파일을 다음과 같이 설정하시면 됩니다:

```json:.vscode/launch.json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "PlannerMainTest",
            "request": "launch",
            "mainClass": "test.java.PlannerMainTest",
            "projectName": "프로젝트이름",
            "sourcePaths": ["${workspaceFolder}/test/java"]
        }
    ]
}
```

주요 설정 항목 설명:
- `type`: Java 프로그램을 실행하므로 "java"로 설정
- `name`: 실행 구성의 이름 (VSCode 디버그 메뉴에서 보이는 이름)
- `request`: 실행 모드 ("launch"는 프로그램 시작)
- `mainClass`: 실행할 메인 클래스의 전체 경로
- `projectName`: 프로젝트의 이름 (실제 프로젝트 이름으로 변경 필요)
- `sourcePaths`: 소스 코드가 있는 경로

이 설정 후에는:
1. VSCode에서 F5를 누르거나
2. 왼쪽 사이드바의 실행 및 디버그 메뉴(Run and Debug)에서 실행

하시면 PlannerMainTest.java 파일이 실행됩니다.

만약 실행이 되지 않는다면 다음을 확인해주세요:
1. Java Extension Pack이 VSCode에 설치되어 있는지
2. 프로젝트 구조가 올바른지
3. mainClass 경로가 정확한지 (패키지 구조에 맞게 설정)

---

_**User**_

ConfigError: The project 'ProjectName' is not a valid java project.

---

_**Assistant**_



이 오류는 Java 프로젝트 구성이 올바르지 않아서 발생하는 것 같습니다. 다음과 같이 launch.json을 수정해보시겠습니까?

```json:.vscode/launch.json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "PlannerMainTest",
            "request": "launch",
            "mainClass": "test.java.PlannerMainTest",
            "vmArgs": "",
            "sourcePaths": ["${workspaceFolder}"]
        }
    ]
}
```

주요 변경사항:
1. `projectName` 항목을 제거했습니다 (프로젝트 설정이 없는 경우에는 불필요)
2. `sourcePaths`를 워크스페이스 전체로 확장했습니다
3. `vmArgs`를 추가했습니다 (필요한 경우 JVM 인자를 추가할 수 있음)

추가로 프로젝트 구성을 위해 다음 사항들을 확인해주세요:

1. 프로젝트 루트에 `.classpath` 파일이 있는지
2. 프로젝트 루트에 `.project` 파일이 있는지
3. VSCode에서 Java Project Explorer에 프로젝트가 제대로 인식되는지

만약 여전히 문제가 있다면, 프로젝트의 전체 구조와 패키지 구성을 공유해 주시면 더 자세한 도움을 드릴 수 있습니다.

---

_**User**_

파일 패스는 아래와 같습니다.
\src\test\java\dev\brown\PlannerMainTest.java

---

_**Assistant**_



아, 파일 경로를 정확히 알게 되었네요. launch.json을 패키지 구조에 맞게 다시 수정하겠습니다:

```json:.vscode/launch.json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "PlannerMainTest",
            "request": "launch",
            "mainClass": "dev.brown.PlannerMainTest",
            "vmArgs": "",
            "sourcePaths": ["${workspaceFolder}/src"]
        }
    ]
}
```

주요 변경사항:
1. `mainClass`를 실제 패키지 구조에 맞게 수정: `dev.brown.PlannerMainTest`
2. `sourcePaths`를 `src` 디렉토리로 지정

이제 파일 구조와 정확히 일치하므로 실행이 될 것입니다. Java 파일이 `src/test/java/dev/brown/PlannerMainTest.java`에 위치해 있지만, 실제 패키지 이름은 `dev.brown`이 되어야 합니다.

참고로 Java 파일 내부의 패키지 선언도 다음과 같이 되어 있는지 확인해주세요:
```java
package dev.brown;
```

---

_**User**_

json 파일(input.json)을 일어서 gson의 JsonObject로 변환하는 함수를 만들어주세요

---

_**Assistant**_

JSON 파일을 읽어서 JsonObject로 변환하는 함수를 만들어드리겠습니다. 먼저 Gson 라이브러리가 필요합니

---



---

