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
- [x] Implementing the MLClient via **Spring WebClient** to communicate with the FastAPI service (currently a blocking call).

### 🟡 Sprint 3: Infrastructure & UX (DevOps & React) - COMPLETED ✅
- [x] Multi-stage **Dockerfiles** for Python, Kotlin, and React web application.
- [x] Setting up production-ready **docker-compose** for the entire microservice ecosystem.
- [x] Implementing a **React** + **TypeScript** UI to handle evaluation forms and database lookup
---

## 🚀 Getting Started (Run the Whole Ecosystem)

The entire microservice ecosystem (React WebApp, Kotlin Backend, Python ML Service, and PostgreSQL) is fully containerized and automated via Docker Compose.

### Quick Start
1. Ensure you have Docker with the Compose plugin installed.
2. Create your local environment file (Compose refuses to start without it):
   ```bash
   cp .env.example .env
   ```
3. From the project root, build and spin up all services:
   ```bash
   docker compose up --build
   ```
   Services start in order thanks to healthchecks: PostgreSQL and the ML service first, then the backend, then the UI.
4. Once the containers are healthy (`docker compose ps`), open your browser and navigate to:
   ```
   http://localhost:3000
   ```

| Service | URL |
| :--- | :--- |
| React UI (proxies `/api` to the backend) | http://localhost:3000 |
| Kotlin backend | http://localhost:8080 (health: `/actuator/health`) |
| FastAPI ML service | http://localhost:8000 (Swagger UI: `/docs`, health: `/health`) |
| PostgreSQL | `localhost:5435` |

### Training the model (optional)
The trained model is committed in `ml-service/models/xgb_model.joblib`. To retrain it, put the Kaggle
"Give Me Some Credit" files in `ml-service/data/raw/` and run from `ml-service/`:
```bash
pip install -r requirements-dev.txt
```
```bash
python -m src.models.train
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

### ⚠️ Error responses
| Status | When |
| :--- | :--- |
| `400` | Request validation failed (`details` lists the invalid fields) |
| `404` | Application / decision not found |
| `409` | `credit_application_id` already exists with different input data (update it with `PUT` instead) |
| `422` | The ML service rejected the input |
| `502` / `503` | The ML service failed / is unreachable |
