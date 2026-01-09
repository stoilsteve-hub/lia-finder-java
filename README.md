# LIA Finder AI Assistant (Java Version) 🤖🇸🇪

Welcome to the Java port of the **LIA Finder AI Assistant**. This tool is designed to help Java developer students proactively find LIA (Lärande i Arbete) opportunities in Sweden.

The tool monitors the **JobTech (Platsbanken) API**, applies strict relevance filtering, and helps you build outreach materials.

## 📊 Analysis & Design
This project follows a structured Object-Oriented Analysis and Design (OOAD) process.

For detailed diagrams (System Architecture, Use Case, Class Diagram), please refer to [architecture_diagrams.md](architecture_diagrams.md).

## 🏗️ Design Patterns (VG Requirements)
I have implemented several design patterns to make the code more flexible and professional.

### 1. Singleton Pattern (`ConfigManager`)
**What is it?** Ensures a class has only one instance and provides a global point of access to it.
- **Where:** `ConfigManager.java`
- **Why:** I only want to load the configuration file (`config.yaml`) once. By making it a Singleton, I can access the settings from anywhere in the app without passing the config object around constantly.

### 2. Strategy Pattern (`RankingService`)
**What is it?** Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
- **Where:** `RankingService.java` uses `ScoringStrategy` interface.
- **Why:** Scoring a job listing is complex. Instead of one giant `if-else` block, I split the logic into strategies:
    - `KeywordScoringStrategy`: Checks for "Java", "LIA".
    - `DateScoringStrategy`: Checks for "2026", "October".
    - `LocationScoringStrategy`: Checks for "Stockholm", "Remote".
  This makes it super easy to add new rules later (e.g., a "SalaryStrategy") without breaking the existing code.

### 3. Observer Pattern (`DaemonService`)
**What is it?** Defines a subscription mechanism to notify multiple objects about any events that happen to the object they're observing.
- **Where:** `DaemonService.java` notifies `ListingObserver`s.
- **Why:** When the background daemon finds new jobs, it shouldn't care *how* we save them. It just shouts "I found jobs!" and the observers react:
    - `FileStorageObserver`: Saves them to a JSON file.
    - `ConsoleLoggerObserver`: Prints a message to the screen.
  This separates the "searching" logic from the "saving" logic.

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

## 🛠️ Configuration
- `config.yaml`: Search terms, LIA dates, and strictness rules.
- `companies.yaml`: Your target company list.
- `profile.yaml`: Your personal data for outreach.

---
*Created for the Analysis & Design VG Assignment.*
