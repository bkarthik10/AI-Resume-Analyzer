# AI Resume Analyzer & Skill Gap Recommendation System

A full-stack web application that analyzes a resume against a target job role, calculates ATS compatibility, identifies skill gaps and missing keywords, and provides personalized recommendations.

**Workflow:**

`Upload Resume → Select Target Role → Add Job Description (Optional) → Analyze Resume → View Results`

---

## Screenshots

### Dashboard

![Dashboard](docs/screenshots/01-dashboard.png)

### Resume Upload

![Resume Upload](docs/screenshots/02-resume-upload.png)

### Target Job Role

![Target Job Role](docs/screenshots/03-target-role.png)

### Analysis Results

![Analysis Results](docs/screenshots/04-analysis-results.png)

### Recommendations

![Recommendations](docs/screenshots/05-recommendations.png)

### ATS Breakdown

![ATS Breakdown](docs/screenshots/06-ats-breakdown.png)

---

## Features

* PDF and DOCX resume upload
* Resume text extraction
* Target job role selection
* Optional job description analysis
* ATS compatibility scoring
* Weighted skill matching
* Matched skill detection
* Missing skill detection
* Missing keyword detection
* Resume section analysis
* Personalized recommendations
* Project recommendations
* Learning resource recommendations
* Detailed ATS score breakdown
* MySQL data persistence
* REST API backend
* Single-page React interface

---

## Technology Stack

### Frontend

* React
* Vite
* JavaScript
* CSS

### Backend

* Java 21
* Spring Boot 3
* Spring Data JPA
* Hibernate
* Apache PDFBox
* Apache POI
* REST APIs

### Database

* MySQL

### Tools

* Maven
* npm
* Git
* GitHub
* VS Code

---

## Project Structure

```text
AI-RESUME-ANALYZER/
│
├── backend/
│   └── Spring Boot REST API
│
├── frontend/
│   └── React/Vite application
│
├── data/
│   ├── skills.json
│   ├── job-roles.json
│   ├── projects.json
│   └── resources.json
│
├── docs/
│   └── screenshots/
│       ├── 01-dashboard.png
│       ├── 02-resume-upload.png
│       ├── 03-target-role.png
│       ├── 04-analysis-results.png
│       ├── 05-recommendations.png
│       └── 06-ats-breakdown.png
│
├── uploads/
├── .env.example
├── .gitignore
└── README.md
```

---

## System Architecture

```text
┌──────────────────────┐
│    React Frontend    │
│        Vite          │
└──────────┬───────────┘
           │
           │ REST API
           ▼
┌──────────────────────┐
│   Spring Boot API    │
│       Java 21        │
└──────────┬───────────┘
           │
     ┌─────┼─────┐
     │     │     │
     ▼     ▼     ▼
  Resume  Skill  ATS
  Parser  Matcher Scorer
     │     │     │
     └─────┼─────┘
           │
           ▼
┌──────────────────────┐
│        MySQL         │
└──────────────────────┘
```

---

## Analysis Workflow

```text
Upload Resume
      ↓
Extract Resume Text
      ↓
Select Target Job Role
      ↓
Load Role Requirements
      ↓
Match Resume Skills
      ↓
Calculate Skill Match
      ↓
Calculate ATS Score
      ↓
Identify Skill Gaps
      ↓
Generate Recommendations
      ↓
Display Results
```

---

## ATS Scoring

The ATS score is calculated using the following components:

| Category               | Maximum Score |
| ---------------------- | ------------: |
| Contact Information    |            10 |
| Resume Sections        |            15 |
| Keyword Coverage       |            30 |
| Skill Coverage         |            25 |
| Formatting / Structure |            10 |
| Job Alignment          |            10 |
| **Total**              |       **100** |

The score is calculated from the extracted resume content and target job requirements.

---

## Skill Matching

The system matches resume skills against the selected job role using reference data from `data/skills.json`.

Skills are normalized using aliases.

Example:

```text
springboot → Spring Boot
```

Role skills are weighted according to priority:

```text
High   → 3
Medium → 2
Low    → 1
```

### Skill Match Formula

```text
Skill Match =
Matched Skill Weight
-------------------- × 100
Total Target Weight
```

If an optional job description contains at least three recognized skills, those skills are used as the analysis target.

---

## Resume Analysis

The application supports:

* PDF resumes using Apache PDFBox
* DOCX resumes using Apache POI

The extracted resume content is analyzed for:

* Contact information
* Resume sections
* Skills
* Keywords
* Job alignment
* Formatting and structure

Scanned or image-only PDFs without extractable text are rejected.

---

## Recommendations

Recommendations are generated based on skills and keywords that are not detected in the uploaded resume.

The system provides:

* Skill improvement recommendations
* Missing keyword recommendations
* Project recommendations
* Learning resources

For missing skills, the system reports:

> Not detected in uploaded resume.

---

## API Endpoints

| Method | Endpoint                            | Description             |
| ------ | ----------------------------------- | ----------------------- |
| GET    | `/api/health`                       | API health check        |
| GET    | `/api/job-roles`                    | Get available job roles |
| GET    | `/api/job-roles/{id}`               | Get role details        |
| GET    | `/api/job-roles/{id}/skills`        | Get role requirements   |
| POST   | `/api/resumes/upload`               | Upload resume           |
| POST   | `/api/analysis/create`              | Analyze resume          |
| GET    | `/api/analysis/{id}`                | Get stored analysis     |
| GET    | `/api/recommendations/{analysisId}` | Get recommendations     |

---

## Running the Project

### Prerequisites

* Java 21
* Maven
* Node.js
* npm
* MySQL

### Backend

Open a terminal in the project root:

```bash
cd backend
```

Set your MySQL credentials.

#### Windows PowerShell

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="yourpassword"
```

Run:

```bash
mvn clean test
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/api/health
```

### Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

## Reference Data

The `data` directory contains the reference datasets used by the analyzer:

```text
data/
├── skills.json
├── job-roles.json
├── projects.json
└── resources.json
```

These files can be updated to add or modify:

* Skills
* Skill aliases
* Job roles
* Role requirements
* Projects
* Learning resources

---

## Testing

The application was tested using resumes for different target roles, including:

* Java Backend Developer
* Frontend Developer
* Machine Learning Engineer
* Data Analyst

Example test results:

| Resume                      | Result |
| --------------------------- | -----: |
| Java Backend — Strong Match |     79 |
| Java Backend — Weak Match   |     27 |
| Machine Learning Engineer   |     79 |
| Frontend Developer          |     72 |

The results demonstrate that the analyzer produces different scores and skill gaps depending on the relationship between the resume and selected target role.

---

## Future Improvements

* Semantic skill matching
* Advanced resume formatting analysis
* Additional job roles and datasets
* Job portal integration
* Cloud deployment
* Resume improvement suggestions
* Industry-specific ATS scoring
* Candidate analytics

## Developer

# B Karthik

Java | Spring Boot | React | MySQL | Full-Stack Development

- LinkedIn: https://www.linkedin.com/in/bkarthik10
- GitHub: https://github.com/bkarthik