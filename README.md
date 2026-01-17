# LIA Finder AI Assistant (Java Version)

## 1. Introduktion
LIA Finder är ett Java-baserat verktyg som har utvecklats för att automatisera processen att söka LIA-platser (Lärande i Arbete). Systemet hjälper studenter att identifiera relevanta praktikannonser genom att övervaka annonser, filtrera bort irrelevanta träffar och generera underlag för outreach mot företag.

## 2. Product Goal
Målet med produkten är att effektivisera processen för att hitta en LIA-plats. Istället för att manuellt söka på Platsbanken dagligen ska systemet automatiskt hitta, analysera, ranka och spara relevanta annonser samt underlätta skapandet av personalized cover letters och contact emails.

## 3. Target Audience and User Needs
**Target audience:**
Studenter inom IT och System Development (t.ex. Yrkeshögskola) som söker LIA/praktik.

**User needs:**
- Spara tid genom att automatiskt filtrera bort irrelevanta annonser (t.ex. “Senior Developer”).
- Få snabba notifications när nya relevanta annonser publiceras.
- Få stöd vid skapande av contact emails och personalized cover letters.
- Möjlighet att köra automatiska sökningar i bakgrunden via ett background process.

## 4. Requirements Specification (Minimum 12 Requirements)
Systemet ska uppfylla följande funktionella krav:

1. **Fetch Job Ads:** Systemet ska kunna hämta jobbannonser från JobTech (Platsbanken) API.
2. **Search Configuration:** Användaren ska kunna konfigurera search keywords (t.ex. “Java”, “LIA”) och location (t.ex. “Stockholm”).
3. **Exclusion Filtering:** Systemet ska automatiskt filtrera bort annonser som innehåller exclusion keywords (t.ex. “Senior”, “Manager”, “5 years experience”).
4. **Scoring and Ranking:** Varje annons ska tilldelas en relevance score baserad på förekomst av keywords i title och description.
5. **CLI Menu:** Vid uppstart ska användaren kunna välja execution mode via en textbaserad CLI menu.
6. **One-time Run Mode:** Systemet ska kunna köras en gång, presentera resultaten i console output och därefter avslutas.
7. **Daemon Mode:** Systemet ska kunna köras som ett background process som söker efter nya annonser varje timme.
8. **Result Storage:** Hittade annonser ska sparas lokalt i en JSON-fil för historik och spårbarhet.
9. **Configuration Management:** Alla inställningar (API keys, keywords, locations) ska läsas in från en extern `config.yaml`.
10. **Company List Loading:** Systemet ska kunna läsa in en lista med intressanta företag från `companies.yaml`.
11. **Student Profile Loading:** Systemet ska kunna läsa in studentens uppgifter (namn, kontaktinformation) från `profile.yaml`.
12. **Cover Letter Generation:** Systemet ska kunna generera ett `personalized_cover_letter.docx` anpassat för ett specifikt företag.
13. **Contact Email Generation:** Systemet ska kunna skapa ett utkast till ett contact email (`outreach_email.txt`) för spontaneous applications.

## 5. Use Case Diagram
Se [architecture_diagrams.md](architecture_diagrams.md#2-use-case-diagram-ooa) för diagrammet.

## 6. Class Diagram
Se [architecture_diagrams.md](architecture_diagrams.md#3-class-diagram-ood) för diagrammet.

**Visade relationer:**
- *Inheritance (Implements)*
- *Aggregation (RankingService innehåller en lista av ScoringStrategies)*
- *Dependency (Main class använder services)*

## 7. Design Patterns
I projektet har tre etablerade design patterns implementerats för att lösa specifika arkitektoniska problem.

### 7.1 Singleton Pattern (`ConfigManager`)
**Purpose:** Att säkerställa att en klass endast har en instans samt tillhandahålla en global access point.

**Motivation:** Konfigurationsfiler (t.ex. `config.yaml`) ska endast läsas in en gång vid uppstart. Genom att implementera `ConfigManager` som en Singleton kan systemet dela konfigurationsdata mellan olika komponenter utan att behöva skicka objektet som parameter, vilket minskar coupling och förbättrar minneshantering.

### 7.2 Strategy Pattern (`RankingService`)
**Purpose:** Att definiera en familj av scoring algorithms, kapsla in dem och göra dem utbytbara.

**Motivation:** Job ad ranking kan implementeras på flera sätt. Istället för att använda komplexa conditional statements har poängsättningslogiken delats upp i separata strategies såsom `KeywordScoringStrategy`, `DateScoringStrategy` och `LocationScoringStrategy`. Detta förbättrar readability och möjliggör enkel utbyggnad enligt Open/Closed Principle.

### 7.3 Observer Pattern (`DaemonService`)
**Purpose:** Att definiera ett dependency relationship där observers automatiskt notifieras när ett subject ändrar tillstånd.

**Motivation:** När `DaemonService` identifierar nya job ads i background execution ska den inte själv hantera vad som händer därefter. Istället notifieras registrerade observers, exempelvis `FileStorageObserver` (persistens) och `ConsoleLoggerObserver` (presentation). Detta separerar core logic från presentation och storage concerns, vilket ökar flexibiliteten.

## 8. Source Code
**GitHub repository:** [https://github.com/stoilsteve-hub/lia-finder-java](https://github.com/stoilsteve-hub/lia-finder-java)

*Made by Stoil Steve Zhelyazkov*
