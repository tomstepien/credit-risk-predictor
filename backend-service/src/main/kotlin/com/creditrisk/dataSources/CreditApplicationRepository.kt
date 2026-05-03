package com.creditrisk.dataSources

import com.creditrisk.model.ApplicationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditApplicationRepository : JpaRepository<ApplicationEntity, Long> {
    fun findByCreditApplicationId(creditApplicationId: String): ApplicationEntity?
    fun existsByCreditApplicationId(businessId: String): Boolean
    fun deleteByCreditApplicationId(businessId: String)
}