# 🚀 Credit Risk Predictor

![Status](https://img.shields.io/badge/status-work--in--progress-orange?style=for-the-badge)

A functional prototype of a credit scoring system, demonstrating how to bridge the gap between ML research and scalable backend architecture

---

## 🧪 Data Science & Research (The "Brain")
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
| **Data Science** | Python, XGBoost, Scikit-learn, Pandas, Jupyter |
| **Backend** | Kotlin (JVM), Spring Boot, FastAPI             |
| **Database** | PostgreSQL                                     |
| **Infrastructure** | Docker, Docker Compose                         |

---

## 📈 Project Roadmap & Milestones

### 🟢 Sprint 1: ML Core (Python) - COMPLETED ✅
- [x] Data exploration (EDA) and feature engineering in Jupyter Notebooks.
- [x] Model training and serialization using `joblib`.
- [x] Building a RESTful Inference API with **FastAPI** and **Pydantic** validation.

### 🔵 Sprint 2: Business Logic & Persistence (Kotlin) - IN PROGRESS 🏗️
- [x] **Current:** Initializing **Spring Boot 3** project with Kotlin.
- [x] Designing the PostgreSQL schema and JPA Entities.
- [ ] Implementing the `MLClient` to communicate with the FastAPI service.

### 🟡 Sprint 3: Infrastructure & UX (DevOps & React) - PLANNED 📅
- [ ] **Dockerization:** Creating Multi-stage Dockerfiles for Python, Kotlin, and React.
- [ ] **Orchestration:** Setting up `docker-compose` for the entire microservice ecosystem.
- [ ] **Frontend:** Building a React + TypeScript dashboard for real-time risk simulation.
---

## 🚀 Getting Started (Integrated Backend & ML)

The easiest way to run the entire ecosystem (Kotlin Backend, Python ML Service, and PostgreSQL) is via Docker Compose.

### Quick Start
1. Ensure you have Docker installed.
2. From the project root, run:
   ```bash
   docker-compose up --build
   ```

### 🧪 Testing the API
Now you can test the full integration flow: Client -> Kotlin Backend -> Python ML -> PostgreSQL.

#### Full Integration Test (via Postman)
   Use this endpoint to trigger the entire logic. The Kotlin backend will call the ML engine and persist the results in the database.

- Open **Postman** and create a new ```POST``` request

- **URL**: ```http://localhost:8080/api/credit/applications/predict```

- In the **Body** tab, select **raw** and choose **JSON**.

- **Example Payload (JSON)**:

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

#### Database & History Verification
   Verify that the data has been correctly saved and processed:

- **All Applications**: ```GET http://localhost:8080/api/credit/applications/all```

- **All Decisions**: ```GET http://localhost:8080/api/credit/decisions/all```

#### Independent ML Testing (Swagger)
   To test the Python ML engine independently (bypassing the Kotlin backend)

- **Swagger UI URL**: ```http://localhost:8000/docs```