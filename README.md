# LIA Finder AI Assistant (Java Version) 🤖🇸🇪

Welcome to the Java port of the **LIA Finder AI Assistant**. This tool is designed to help Java developer students proactively find LIA (Lärande i Arbete) opportunities in Sweden.

The tool monitors the **JobTech (Platsbanken) API**, applies strict relevance filtering, and helps you build outreach materials.

## 📊 Architecture & Use Cases
We follow a modern **Service/DTO architecture**.

### Use Case Diagram
```mermaid
graph LR
    U["User (Student)"]
    
    subgraph "LIA Finder System"
        UC1("Monitor LIA (Option 1)")
        UC2("Build Outreach (Option 2)")
        UC3("Run Daemon (Option 3)")
        UC4("Fetch from JobTech API")
        UC5("Generate DOCX/Email")
    end

    U --> UC1
    U --> UC2
    U --> UC3
    
    UC1 -.->|include| UC4
    UC2 -.->|include| UC5
```

### Class Diagram
```mermaid
classDiagram
    class Main {
        +main(args)
        -chooseMode() String
    }

    class ConfigLoader {
        +loadConfig(path) AppConfig
        +loadCompanies(path) List~Company~
        +loadProfile(path) Profile
    }

    class JobSearchService {
        +fetchListings(AppConfig) List~Listing~
        -parseResponse(json, AppConfig)
        -buildQueries(AppConfig)
    }

    class RankingService {
        +scoreListings(AppConfig, List~Listing~) List~ScoredListing~
    }

    class OutreachService {
        +generateOutreach(AppConfig, Company, Profile)
    }

    %% Data Models
    class Listing {
        +String title
        +String company
        +String location
        +String url
        +String description
    }

    class ScoredListing {
        +double score
        +List~String~ reasons
    }

    %% Relationships
    Listing <|-- ScoredListing : Inheritance
    Main --> ConfigLoader : Uses
    Main --> JobSearchService : Orchestrates
    Main --> RankingService : Orchestrates
    Main --> OutreachService : Orchestrates
```

Detailed design patterns and descriptions can be found in [**Architecture Diagrams**](architecture_diagrams.md)

## 🚀 How to Run

### 1. Prerequisites
- **Java 17** or higher
- **Maven**
- A **JobTech API Key** (Set as environment variable `JOBTECH_API_KEY`)

### 2. Build the Project
```bash
mvn clean package
```

### 3. Run the Application
You can run the interactive menu by executing the JAR or using Maven:
```bash
java -jar target/lia-finder-1.0-SNAPSHOT.jar
```
Or via Maven:
```bash
mvn exec:java -Dexec.mainClass="com.liafinder.Main"
```

## 🛠️ Configuration
Before running, ensure you have the following files in the project root:
- `config.yaml`: Search terms and strictness rules.
- `companies.yaml`: Your target company list.
- `profile.yaml`: Your personal data for outreach (git-ignored).

## 📖 Walkthrough
For a detailed guide on how the project was built and how to use it, see:
👉 [**Project Walkthrough**](walkthrough.md)

---
*Created with ❤️ for Java students.*
