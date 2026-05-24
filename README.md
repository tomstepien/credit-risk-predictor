# 🚀 Credit Risk Predictor

![Status](https://img.shields.io/badge/status-work--in--progress-orange?style=for-the-badge)

A functional prototype of a credit scoring system, demonstrating how to bridge the gap between ML research and scalable backend architecture

---

## 🧪 Data Science & Research
The development process was preceded by an in-depth data exploration phase. This ensures that the model architecture and features are strictly data-driven and based on verified statistical insights.

**Key research steps found in [`/ml-service/notebooks`](./ml-service/notebooks):**
* **EDA & Feature Engineering:** Deep dive into the "Give Me Some Credit" dataset. Handling missing values (MonthlyIncome, NumberOfDependents).
* **Class Imbalance:** Applied **resampling** techniques where needed to address the skewed distribution of defaults.
* **Model Selection:** Comparative analysis of **baseline linear models** versus **ensemble learning (XGBoost)** to capture non-linear relationships in credit data.
* **Optimization:** Hyperparameter tuning using **Bayesian Optimization** (Scikit-Optimize).

---

## 🛠️ Technology Stack

| Domain | Tools                                          |
| :--- |:-----------------------------------------------|
| **Frontend** | React, TypeScript, Vite, CSS                   |
| **Backend** | Kotlin (JVM), Spring Boot, FastAPI             |
| **Data Science** | Python, XGBoost, Scikit-learn, Pandas, Jupyter |
| **Database** | PostgreSQL                                     |
| **Infrastructure** | Docker, Docker Compose                         |

---

## 📈 Project Roadmap & Milestones

### 🟢 Sprint 1: ML Core (Python) - COMPLETED ✅
- [x] Data exploration (EDA) and **feature engineering** in Jupyter Notebooks.
- [x] **Model training** and serialization using joblib.
- [x] Building a RESTful Inference API with **FastAPI** and **Pydantic** validation.

### 🔵 Sprint 2: Business Logic & Persistence (Kotlin) -  COMPLETED ✅
- [x] Initializing **Spring Boot 3** project with Kotlin.
- [x] Designing the **PostgreSQL** schema and JPA Entities.
- [x] Implementing the MLClient via **Spring WebClient** to asynchronously communicate with the FastAPI service.

### 🟡 Sprint 3: Infrastructure & UX (DevOps & React) - COMPLETED ✅
- [x] Multi-stage **Dockerfiles** for Python, Kotlin, and React web application.
- [x] Setting up production-ready **docker-compose** for the entire microservice ecosystem.
- [x] Implementing a **React** + **TypeScript** UI to handle evaluation forms and database lookup
---

## 🚀 Getting Started (Run the Whole Ecosystem)

The entire microservice ecosystem (React WebApp, Kotlin Backend, Python ML Service, and PostgreSQL) is fully containerized and automated via Docker Compose.

### Quick Start
1. Ensure you have Docker and Docker Compose installed.
2. From the project root, build and spin up all services:
   ```bash
   docker-compose up --build
   ```
3. Once the containers are healthy, open your browser and navigate to:
   ```bash
   http://localhost:3000
   ```

## 🔍 Testing the API Directly (via Postman)
You can test the full integration flow: Client -> Kotlin Backend -> Python ML -> PostgreSQL.

The Kotlin backend acts as an API Gateway — it receives the request, queries the ML engine, persists the logs in PostgreSQL, and returns the scoring result.
* **Method**:```POST``` 

* **URL**: ```http://localhost:8080/api/credit/applications/predict```

* **Body**: ```raw``` -> ```JSON```

* **Example Payload (JSON)**:

```json
{
   "RevolvingUtilizationOfUnsecuredLines": 0.35,
   "age": 45,
   "NumberOfTime30-59DaysPastDueNotWorse": 1,
   "NumberOfTime60-89DaysPastDueNotWorse": 0,
   "NumberOfTimes90DaysLate": 0,
   "DebtRatio": 0.4,
   "MonthlyIncome": 5500.0,
   "NumberOfOpenCreditLinesAndLoans": 12,
   "NumberRealEstateLoansOrLines": 1,
   "NumberOfDependents": 2
}
```

### 📊 Database & History Verification
Verify that the data has been correctly saved and processed:

* **All Applications**: ```GET http://localhost:8080/api/credit/applications/all```

* **All Decisions**: ```GET http://localhost:8080/api/credit/decisions/all```