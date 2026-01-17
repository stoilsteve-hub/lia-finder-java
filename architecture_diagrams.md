# LIA Finder Java: Architecture Diagrams

## 1. System Architecture (Layered)
```mermaid
graph TD
    subgraph "Presentation Layer"
        CLI["Main (CLI)"]
    end

    subgraph "Business Logic Layer"
        CM["ConfigManager (Singleton)"]
        JSS["JobSearchService"]
        RS["RankingService (Strategy)"]
        DS["DaemonService (Observer)"]
        OS["OutreachService"]
    end

    subgraph "Domain Layer (Model)"
        Model["Models: Listing, Company, Profile"]
    end

    subgraph "Infrastructure / Data Layer"
        API["JobTech API (External)"]
        SS["StorageService"]
        FS["File System (YAML/JSON)"]
    end

    %% Interactions
    CLI --> CM
    CLI --> JSS
    CLI --> RS
    CLI --> DS
    CLI --> OS

    JSS --> API
    DS --> JSS
    DS --> RS
    
    RS --> Model
    JSS --> Model
    
    DS --> SS
    SS --> FS
    CM --> FS
```

## 2. Use Case Diagram (OOA)
```mermaid
graph LR
    User((Student))
    
    subgraph "LIA Finder System"
        UC1[Monitor LIA Listings]
        UC2[Generate Outreach Material]
        UC3[Run Background Daemon]
        
        UC_API[Fetch from JobTech API]
        UC_Score[Score & Rank Listings]
        UC_Save[Save Results to JSON]
        UC_Doc[Generate Email/DOCX]
    end

    User --> UC1
    User --> UC2
    User --> UC3
    
    UC1 -.->|include| UC_API
    UC1 -.->|include| UC_Score
    UC1 -.->|include| UC_Save
    
    UC2 -.->|include| UC_Doc
    
    UC3 -.->|include| UC_API
    UC3 -.->|include| UC_Score
    UC3 -.->|include| UC_Save
```

## 3. Class Diagram (OOD)
```mermaid
classDiagram
    %% Core
    class Main {
        +main(args)
    }

    %% Singleton Pattern
    class ConfigManager {
        -static instance : ConfigManager
        +getInstance() ConfigManager
        +getConfig() AppConfig
    }

    %% Services
    class JobSearchService {
        +fetchListings(AppConfig)
    }

    %% Strategy Pattern Context
    class RankingService {
        -strategies : List~ScoringStrategy~
        +scoreListings(listings)
    }

    %% Strategy Interface
    class ScoringStrategy {
        <<interface>>
        +score(ScoredListing, AppConfig)
    }

    %% Observer Pattern Subject
    class DaemonService {
        -observers : List~ListingObserver~
        +start(AppConfig)
        +notifyObservers(listings)
    }

    %% Observer Interface
    class ListingObserver {
        <<interface>>
        +onListingsFound(listings)
    }

    %% Relationships
    Main --> ConfigManager : Singleton Access
    Main --> JobSearchService : Uses
    Main --> RankingService : Uses
    Main --> DaemonService : Uses
    
    RankingService o-- ScoringStrategy : Aggregates
    ScoringStrategy <|.. KeywordScoringStrategy : Implements
    ScoringStrategy <|.. DateScoringStrategy : Implements
    ScoringStrategy <|.. LocationScoringStrategy : Implements

    DaemonService o-- ListingObserver : Notifies
    ListingObserver <|.. FileStorageObserver : Implements
    ListingObserver <|.. ConsoleLoggerObserver : Implements
```
