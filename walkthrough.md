# Port to Java Walkthrough

I have recreated the core structure of the LIA Finder in a new Java project located at [`LiaFinderJava/`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/).

## Project Structure
- **Build**: Maven (`pom.xml`)
- **Version**: Java 17+
- **Entry Point**: [`com.liafinder.Main`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/Main.java)

## Key Components Implemented
1.  **Configuration Loading**: [`ConfigLoader.java`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/config/ConfigLoader.java) uses Jackson to parse your existing `yaml` files.
2.  **Data Records**: [`Company.java`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/model/Company.java), [`AppConfig.java`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/config/AppConfig.java), etc.
3.  **Ranking Logic**: [`RankingService.java`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/service/RankingService.java) replicates the keyword scoring logic (and correctly uses `javaTerms`!).
4.  **Outreach Generation**: [`OutreachService.java`](file:///Users/stoil.steve/PycharmProjects/LIA_FINDER_AI_ASSISTANT-1/LiaFinderJava/src/main/java/com/liafinder/service/OutreachService.java) uses **Apache POI** to create `.docx` files.

## How to Run
I tried to run it automatically but `mvn` was not found in my shell environment. You can run it from your terminal or IDE:

1.  **Build**:
    ```bash
    cd LiaFinderJava
    mvn clean package
    ```

2.  **Run (Monitor)**:
    ```bash
    java -jar target/lia-finder-1.0-SNAPSHOT.jar monitor
    ```

3.  **Run (Outreach)**:
    ```bash
    # Ensure config.yaml, companies.yaml, etc. are in the current specific directory or update paths
    java -jar target/lia-finder-1.0-SNAPSHOT.jar outreach
    ```

## Next Steps
- Implement `JobSearchService` calling the JobTech API (using `java.net.http.HttpClient`).
- Flesh out the email template logic in `OutreachService`.
