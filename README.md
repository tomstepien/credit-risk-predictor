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
- [ ] **Current:** Initializing **Spring Boot 3** project with Kotlin.
- [ ] Designing the PostgreSQL schema and JPA Entities.
- [ ] Implementing the `MLClient` to communicate with the FastAPI service.

### 🟡 Sprint 3: Infrastructure & UX (DevOps & React) - PLANNED 📅
- [ ] **Dockerization:** Creating Multi-stage Dockerfiles for Python, Kotlin, and React.
- [ ] **Orchestration:** Setting up `docker-compose` for the entire microservice ecosystem.
- [ ] **Frontend:** Building a React + TypeScript dashboard for real-time risk simulation.
---

## 🚀 Getting Started (ML Service Only)

Currently, you can run and test the ML inference engine independently.

### Installation
1. Run the following commands
    ```bash
   cd ml-service
   pip install -r requirements.txt
   uvicorn app.main:app --reload
    ```
   
2. Access the API:
   Open `http://localhost:8000/docs` in your browser.



