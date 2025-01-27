
## 팀프로젝트 1,2 엉금엉금 팀

---

## 📌 [목차](#index) <a name = "index"></a>

- [프로젝트 소개](#intro_project)
- [기술 스택](#teck_stack)
- [아키텍처](#architecture)
- [협업 방식](#cooperation)
- [API Docs](#api)
- [기술 사용 이유](#why)
- [결과물](#outputs)


<br>

## 📝 프로젝트 소개 <a name = "intro_project"></a>


### 💡 스튜디오아이 홈페이지 및 관리자 서비스

주식회사 스튜디오아이의 정보 제공 및 문의를 위한 홈페이지와 이를 관리하기 위한 관리자 서비스입니다.
관리자 페이지는 PA(Production Admin (Page)), 일반 사용자 홈페이지는 PP(Production Page)로 구분했습니다.

### 🚀 배경

2024 팀프로젝트1, 2 과목 프로젝트로, 실제 고객사인 스튜디오아이 측에서 필요한 내용을 바탕으로 실제 사용할 수 있는 서비스 개발을 위해 진행하게 되었습니다.
2023년 수강생들이 개발한 프로그램을 토대로 전면 리팩토링과 지속적으로 고객사에게 요구 사항을 수집 후 개발했습니다.

<br />

## ⚙️ 기술 스택 <a name = "teck_stack"></a>
![](https://velog.velcdn.com/images/phonil/post/d085d340-3004-4fda-8234-b4dcf3657a90/image.png)
 
 
<br />


## 🧬 아키텍처 <a name = "architecture"></a>

### 개발 및 테스트
![Image](https://github.com/user-attachments/assets/48945c31-6749-4c75-9b5a-21f8fb224818)
 
학습과 특정 클라우드에 종속되지 않는 환경을 위해 AWS에서 제공하는 기능보다 직접 구축하여 사용하는 방향으로 진행했습니다.

- Nginx를 로드밸런서로 3개의 스프링 서버로 트래픽 분산
- 4개의 스프링 서버 중 한 개는 백업 서버로, 다른 세 개의 서버가 모두 종료될 시 트래픽을 받음
- Github Main Push를 트리거로 Jenkins에서 빌드, 테스트와 정적 분석 후 배포

<br>

### 고객사 이전 시 사용 예정 아키텍처
![](https://velog.velcdn.com/images/phonil/post/7009cfe7-397a-4f46-af3b-d164da3fc55f/image.png)

고객사의 요구 사항인 '개발자 없이 운영할 수 있는 서비스'에 맞추어 고객사로 서버 이전 시 사용할 아키텍처입니다. 많은 사용과 그로 인한 트래픽보다 가끔 사용할 서비스가 필요한 상황이었고, 비용과 편의성을 고려하여 구성했습니다.
최소한의 서버 확장을 위해 AWS 오토 스케일링으로 서버를 늘리고, 서버에 문제가 생겼을 시 AWS Cloudwatch 알림을 통한 자동 회복이 가능하도록 구성했습니다.

<br />


## ⏰ 협업 방식 <a name = "cooperation"></a>
![Image](https://github.com/user-attachments/assets/30a537ea-b8b2-4a3d-9231-8e07dda2219b)
 
매주 수업 시간 기준 1주 단위 스프린트의 애자일 방식으로 개발을 진행했습니다. 돌아가는 소프트웨어를 중심으로 데모를 진행하여 작업 할당량을 조절하고 계획을 유동적으로 관리했습니다.
전체 일정과 큰 작업들을 기반으로 할 일을 할당했고, 이를 효율적으로 관리하기 위해 Notion, Jira, Discord를 사용했습니다.

### Notion
Notion에서 공간을 나누어 수업 및 프로젝트 정보와 회의록을 관리했습니다.

#### 파트 및 역할별
![](https://velog.velcdn.com/images/phonil/post/582dd7de-0b6b-4819-8b31-7614f2d5728e/image.png)

#### 링크, 회의록
![](https://velog.velcdn.com/images/phonil/post/9ed4fb8c-c306-43cb-9552-e4c8143b1dcc/image.png)


### Jira
초기에 스프린트를 위해 전체 일정과 작업(백로그)를 설정했고, Jira를 사용하여  매주 업무를 할당했습니다. 해야할 작업을 에픽으로 설정하여 프로젝트 완성을 위한 큰 틀을 잡고, 세부 task를 만들었습니다. task는 데모 시나리오를 포함하였고, 매주 Jira에서 할당받은 작업을 데모했습니다.

#### 전체 기간 작업 상태
![](https://velog.velcdn.com/images/phonil/post/d930ca50-d72a-4b53-b0a6-07845a4eef35/image.png)

<br>

#### 2학기 백로그 추이
![](https://velog.velcdn.com/images/phonil/post/4a40caec-d203-4696-af3c-85da82e2a6f9/image.png)


<br>

#### task 목록
![](https://velog.velcdn.com/images/phonil/post/f1791de2-0657-4dbf-8e32-d56b979c3a3f/image.png)

<br>

#### task 상세 - 다중 서버 테스트
![](https://velog.velcdn.com/images/phonil/post/dddf2ce1-b57e-4b65-aae8-bb13cdea489e/image.png)

작성한 시나리오를 바탕으로 매주 데모를 진행하여 즉시 피드백을 주고받았습니다.


<br />


## 📑 API Docs <a name = "api"></a>

팀 내 API 문서 공유를 위해 **Swagger**를 사용했습니다.
아래는 문서 내용 일부로, 전체 문서는 [여기](https://www.studio-eye.kro.kr/swagger-ui/index.html)에서 확인하실 수 있습니다.

<img src="https://velog.velcdn.com/images/phonil/post/a4330d64-69bf-4000-a391-69cabf1026e8/image.png" alt="메인" width="650" />


<br />


## 💎 기술 사용 이유 <a name = "why"></a>

### Nginx / AWS ALB, Auto Scaling
![Image](https://github.com/user-attachments/assets/4f4b774a-8811-4653-957f-806f4b22f556)
 

#### 개발 및 테스트

개발 및 테스트 환경에서는 HTTPS 설정과 로드밸런서 역할을 위해 Nginx를 사용했습니다. 학교에서 제공하는 비용으로 추가적인 인프라 자원을 사용할 수 있었고, 직접 구축하는 방향으로 설정했습니다.
Nginx와 다중 서버가 구축된 환경에서 Jmeter로 부하 분산 및 장애 상황 테스트를 진행했습니다.

<br>

#### 고객사

고객사 요구 사항인 '자주 사용되지 않으며, 개발자가 없는 서비스'를 위해 **최소 비용과 자동 시스템이 필요**했습니다. 자주 사용되지 않을 것이라고 확신해주셨지만, 만약을 대비하여 최소한의 부하 분산과 오토 스케일링을 설정했습니다.
이 기능들은 트래픽이 많아져 추가적인 인스턴스 생성이 되지 않는 한 비용이 발생하지 않고, 추가 작업이 필요 없기 때문에 선택했습니다.


<br>

### Jenkins

![](https://velog.velcdn.com/images/phonil/post/0cf2ddd3-0e8c-4c65-86b1-b96707130214/image.png)

1주 단위라는 짧은 텀의 스프린트를 진행했기 때문에 수시 배포가 필요했습니다. 번거롭지 않고 빠르게, 쉽게 배포되도록 자동 배포 환경을 구축했습니다.

#### 1학기

1학기에는 지난 학생들이 진행하던 프로젝트를 이해하고 동일한 환경을 구축하는 것이 우선이었습니다. 애자일 방식에 대한 적응과 빠른 템포의 개발이 진행되었고 새로운 것보다 익숙한 기술로 템포를 맞추는 것이 중요하다고 생각했습니다.
젠킨스에 대해 잘 알지 못했기 때문에 학습에 소요되는 시간이 있었고, 빠르게 구축할 수 있는 **Github Actions와 AWS Code Deploy를 사용하여 배포 자동화 환경을 구축**했습니다.

<br>

#### 2학기
<img src="https://velog.velcdn.com/images/phonil/post/d8508a98-9e15-4dfa-95ac-a3056677548f/image.png" alt="메인" width="650" />

인프라 요소들은 아키텍트와 PM이 관리를 했고, 깃허브에서 팀원이 함께 관리하는 것보다 중앙에서 관리하는 시스템이 효율적이었습니다.
**젠킨스**는 아이템 별 스크립트의 진행 상황을 별도 대시보드에서 확인할 수 있다는 점이 좋았습니다. 메모리 소모가 큰 편이라 구축 시 스프링 서버와 별도로 추가적인 서버를 위한 비용이 필요했지만, 학교의 지원금으로 해결할 수 있었습니다.


### Junit5

![Image](https://github.com/user-attachments/assets/6f48198e-ef04-4672-9870-280534db6a94)

#### 단위 테스트
주된 로직이 존재하는 Application Layer의 Service 클래스 위주로 단위 테스트를 작성했습니다.
총 291개의 단위 테스트를 작성했으며, 각 테스트마다 성공, 실패 케이스가 최소 1개씩 포함되도록 했습니다. 실패 케이스는 다양한 이유로 발생할 수 있기 때문에 여러 상황을 가정하여 작성하고자 했습니다.

![](https://velog.velcdn.com/images/phonil/post/0a493281-4774-4d9f-95e8-35f8bbca6701/image.png)

작성된 단위 테스트는 Jenkins 파이프라인에서 자동화되어 배포 전 빌드와 함께 테스트를 진행하도록 했습니다.

<br>

### SonarQube & Jacoco

![Image](https://github.com/user-attachments/assets/0bbe0319-2d06-4ccd-96cd-63e2663b940a)

#### 정적 분석

코드 정적 분석을 위해 SonarQube를 사용했습니다. Code Smell 확인하여 불필요한 코드를 줄이고 가독성 및 일관성을 높여 코드 품질을 향상시키고자 했습니다.

![](https://velog.velcdn.com/images/phonil/post/147c1aa5-537a-409a-99d3-2acdb42bdeb9/image.png)

![](https://velog.velcdn.com/images/phonil/post/46314a49-2977-42ed-b255-ad7044651495/image.png)


<br>

#### 테스트 커버리지

테스트 코드 작성 후 커버리지를 확인하여 테스트 범위를 늘려갔습니다. 커버리지를 확인하며 부족한 부분을 확인할 수 있었습니다. 특히, 다양한 실패 케이스 작성에 큰 도움이 되었습니다.
명령어 커버리지와 분기 커버리지를 기준으로 각 80%, 60% 이상을 목표로 했습니다. 초기 50% 미만이었던 명령어 커버리지는 프로젝트 종료 시점 95%를 달성했으며, 분기 커버리지는 76%를 달성했습니다.


![](https://velog.velcdn.com/images/phonil/post/7231b2ec-d669-4337-a681-0da3289532a1/image.png)


<br />

## 🎁 결과물 <a name = "outputs"></a>

Artwork (작업물) : 프로모션 페이지에서 회사 외부인들이 볼 수 있는 스튜디오 아이의 작업물.
작업물 등록이란 스튜디오 아이가 외부인들에게 노출시키고 싶은(홈페이지에 올리고싶은) 내용을 등록하는 것을 말함.

Request (문의) : 외부인이 스튜디오 아이 측에 작업을 맡기고 싶을 경우 이용.
회사는 프로모션 페이지에서 외부인이 입력한 문의 내용을 프로모션 관리 페이지에서 확인할 수 있음.
문의 ⇒ 프로젝트로 반드시 이어지는 게 아님. 말 그대로 단순 문의임!


일반 사용자가 방문하는 사용자 페이지(PP)는 회사의 관리자가 관리자 페이지(PA)에서 설정한 내용을 바탕으로 표시됩니다.

### 관리자 페이지 Home

<img src="https://velog.velcdn.com/images/phonil/post/cb211005-0c83-4343-adf5-44e9795b389e/image.jpg" alt="홈" width="600" />

- 요약된 문의 내역과 기간 별 통계를 확인할 수 있습니다.

<br>

### 메인 화면 - 작업물

<img src="https://velog.velcdn.com/images/phonil/post/e4a8da0e-8f7b-4444-b57e-c4f2e6d261b2/image.jpg" alt="메인_작업물" width="600" />

#### PA
- 작업물의 정보(제목, 설명, 고객사, 제작 날짜, 카테고리, 링크 등)를 설정할 수 있습니다.
- 메인 화면에 표시될 작업물을 선택하며, 순서를 설정할 수 있습니다.
- 메인 화면에 표시되지 않을 작업물을 포함한 순서를 설정할 수 있습니다.

#### PP
- 관리자 페이지(PA)에서 설정한 작업물들을 볼 수 있으며, 첨부된 링크로 이동할 수 있습니다.

<br>

### 메인 화면 - 메뉴, 고객

<img src="https://velog.velcdn.com/images/phonil/post/152d5eee-1478-4c5c-8af3-d86efd0ee1fa/image.jpg" alt="메인_메뉴_고객" width="600" />

#### PA
- 메뉴의 공개 여부 및 순서를 변경할 수 있습니다.
- 고객사를 등록하여 관리할 수 있습니다.

#### PP
- 관리자의 설정에 따라 공개 처리된 메뉴를 확인할 수 있습니다.

<br>

### 문의

<img src="https://velog.velcdn.com/images/phonil/post/90307a70-d5bc-4a92-b7d0-2e58982587f8/image.jpg" alt="문의" width="600" />

#### PA
- 문의 내역을 확인하고, 답변 메일을 보낼 수 있습니다.

#### PP
- 문의를 남길 프로젝트를 입력합니다. 관리자 승인 후 PP에 등록될 수 있습니다.

<br>

### 회사 정보

<img src="https://velog.velcdn.com/images/phonil/post/cd53d085-7494-4c55-9c25-b254347e883b/image.jpg" alt="회사정보" width="600" />

#### PA
- 회사의 정보(로고, 주소, 연락처, 설명 등)를 설정할 수 있습니다.
- 협력사를 등록하고, 관리할 수 있습니다.

#### PP
- 관리자가 등록한 회사 정보 및 협력사를 확인할 수 있습니다.

<br>

### FAQ

<img src="https://velog.velcdn.com/images/phonil/post/5c82fa91-2366-444d-af0a-e8640bc3e2df/image.jpg" alt="FAQ" width="600" />

#### PA
- 자주 받은 질문(FAQ)를 등록하고 관리할 수 있습니다.

#### PP
- 관리자가 등록한 자주 받은 질문(FAQ)를 확인할 수 있습니다.

<br>

### 채용 정보

<img src="https://velog.velcdn.com/images/phonil/post/860901e6-429d-4f97-9446-a8015fe202fd/image.jpg" alt="FAQ" width="600" />

#### PA
- 회사의 채용 정보 및 복지를 등록하고 관리할 수 있습니다.

#### PP
- 관리자가 등록한 회사의 채용 정보 및 복지를 확인할 수 있습니다.

<br>

### 뉴스

<img src="https://velog.velcdn.com/images/phonil/post/fabaec39-2ec9-4e55-9e02-4c492b1161d8/image.jpg" alt="FAQ" width="600" />

#### PA
- 회사 관련 뉴스를 링크와 함께 등록할 수 있습니다.

#### PP
- 관리자가 등록한 회사 관련 뉴스를 확인할 수 있으며, 첨부된 링크로 이동할 수 있습니다.

<br>
