# LIA Finder Java: Architecture Diagrams

This document visualizes the structure and functional flow of the **LIA Finder AI Assistant**.

## 1. Use Case Diagram
This diagram shows how you (the User) interact with the different modes of the application.

```mermaid
graph LR
    U["User (Student)"]
    
    subgraph "LIA Finder System"
        UC1("Monitor LIA (Option 1)")
        UC2("Build Outreach (Option 2)")
        UC3("Run Daemon (Option 3)")
        UC4("Fetch from JobTech API")
        UC5("Generate DOCX/Email")
        UC6("Save Results to JSON")
    end

    U --> UC1
    U --> UC2
    U --> UC3
    
    UC1 -.->|include| UC4
    UC1 -.->|include| UC6
    UC2 -.->|include| UC5
    UC3 -.->|include| UC4
    UC3 -.->|include| UC6
```

---

## 2. Class Diagram
This diagram shows the relationship between data models (Records) and the Business Logic (Services).

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

    class StorageService {
        +saveListings(List~ScoredListing~, String path)
    }

    class DaemonService {
        +start(AppConfig)
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

    class AppConfig {
        <<Record>>
        +SearchConfig search
        +LiaConfig lia
        +LinkedInConfig linkedin
    }

    %% Relationships
    Listing <|-- ScoredListing : Inheritance
    Main --> ConfigLoader : Uses
    Main --> JobSearchService : Orchestrates
    Main --> RankingService : Orchestrates
    Main --> OutreachService : Orchestrates
    Main --> StorageService : Orchestrates
    Main --> DaemonService : Orchestrates
    
    DaemonService --> JobSearchService : Uses
    DaemonService --> RankingService : Uses
    DaemonService --> StorageService : Uses
    
    JobSearchService ..> Listing : Produces
    RankingService ..> ScoredListing : Produces
    
    RankingService --> AppConfig : Configures Logic
    JobSearchService --> AppConfig : Configures Search
```

## 3. Design Pattern Summary
- **Service Layer Pattern**: Logic is separated into stateless service classes.
- **Data Transfer Object (DTO)**: Java Records are used to move data safely.
- **Static Factory**: `ConfigLoader` acts as a factory for configuration objects.
- **Inheritance**: `ScoredListing` extends `Listing` to add ranking data.
- **Daemon/Worker Pattern**: `DaemonService` runs background tasks on a schedule.
