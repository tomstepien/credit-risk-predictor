package com.creditrisk.dataSources

import com.creditrisk.model.DecisionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CreditDecisionRepository : JpaRepository<DecisionEntity, Long>{
    @Query("SELECT d FROM DecisionEntity d WHERE d.application.creditApplicationId = :appId")
    fun findAllDecisionsByApplicationId(appId: String): List<DecisionEntity>
}