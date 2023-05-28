# 소개
김영한님의 JPA 강의를 보면서 예제 코드와 설명 TIP 등을 적어놓고 수시로 참고하기 위해 만든 레포지토리

# 만든 이유
1. 노트에 적을 경우 관련 코드들에 대한 맥락을 지속적으로 적어주어야 해서 매우 귀찮고 비효율적이다.
2. 프로젝트를 이용해 공부할 경우 바로바로 예제를 실행해보면서 진행할 수 있어 효과적이다.
3. 원하는 부분을 참고할 때마다 주의사항과 팁 등을 반복적으로 인지함으로써 쉽게 암기할 수 있다.

# 공부 방식
1. ctrl + shif + f를 누른 후 "* #" 를 검색한다.
2. 검색창을 크게 키우고 필요한 부분을 찾아 참고한다.
3. 프로젝트를 진행하거나 공부하면서 알게 된 JPA 지식을 지속적으로 추가해 나간다.

키워드
* #jpabasic
* #jpa-springboot
* #data-jpa
* #querydsl


# 프로젝트 세팅

1. Intellij로 jpa-basic 실행
2. File -> Project Structure -> Modules에서 +버튼 클릭해서 나머지 모듈 등록
3. Settings -> Build -> Build Tools -> Gradle 각 모듈마다 Build and run using: Intellij IDEA, Run test using: Intellij IDEA로 바꾸기 
4. Settings -> Build -> Compiler -> Annotation Processors -> Enable annotation processing 체크
5. jpa-basic은 resources/META-INF/persistence.xml에서 DB 설정 , 나머지는 reources/application.yml을 통해 DB 설정


## QueryDSL 설정 
1. 모든 세팅 완료 후./gradlew compileQuerydsl을 실행
2. build/generated/querydsl/study/datajpa/entity에서 'QMember'같은 Querydsl이 만든 파일 생성된 것 확인 
