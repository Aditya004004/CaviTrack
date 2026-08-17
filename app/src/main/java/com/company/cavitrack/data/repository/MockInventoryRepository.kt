package com.company.cavitrack.data.repository

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInventoryRepository @Inject constructor() : InventoryRepository {
    private val components = mutableListOf(
        Component("c1", "Resin A", "SKU-001", "Raw Material", 500, "kg", 100),
        Component("c2", "Connector Pin", "SKU-002", "Part", 50, "pcs", 100), // Low stock
        Component("c3", "Housing Body", "SKU-003", "Part", 1200, "pcs", 500)
    )
    
    private val customers = mutableListOf(
        Customer("cust1", "Acme Corp", "555-0100", "contact@acme.com", "123 Factory Ln"),
        Customer("cust2", "Globex", "555-0101", "sales@globex.com", "456 Industrial Blvd")
    )
    
    private val molds = mutableListOf(
        Mold("m1", "MOLD-A", 4, "c3", "Active", "Rack A1"),
        Mold("m2", "MOLD-B", 8, "c2", "In Maintenance", "Rack B2")
    )
    
    private val history = mutableListOf(
        HistoryLog("h1", "Component", "c2", "Connector Pin", "Stock Adjusted", "Manual", "150", "50", timestamp = System.currentTimeMillis() - 3600000)
    )

    override fun getComponents(): Flow<Result<List<Component>>> = flow {
        emit(Result.Loading)
        delay(500)
        emit(Result.Success(components.toList()))
    }

    override fun getComponent(id: String): Flow<Result<Component>> = flow {
        emit(Result.Loading)
        delay(300)
        val comp = components.find { it.id == id }
        if (comp != null) emit(Result.Success(comp))
        else emit(Result.Error("Component not found"))
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> = flow {
        emit(Result.Loading)
        delay(500)
        emit(Result.Success(customers.toList()))
    }

    override fun getMolds(): Flow<Result<List<Mold>>> = flow {
        emit(Result.Loading)
        delay(500)
        emit(Result.Success(molds.toList()))
    }

    override fun getHistory(): Flow<Result<List<HistoryLog>>> = flow {
        emit(Result.Loading)
        delay(500)
        emit(Result.Success(history.toList().sortedByDescending { it.timestamp }))
    }

    override suspend fun saveComponent(component: Component): Result<Unit> {
        delay(500)
        val index = components.indexOfFirst { it.id == component.id }
        if (index >= 0) components[index] = component else components.add(component)
        history.add(HistoryLog(
            id = System.currentTimeMillis().toString(),
            entityType = "Component",
            entityId = component.id,
            entityName = component.name,
            action = if (index >= 0) "Updated" else "Created",
            changeSource = "Manual"
        ))
        return Result.Success(Unit)
    }

    override suspend fun saveCustomer(customer: Customer): Result<Unit> {
        delay(500)
        val index = customers.indexOfFirst { it.id == customer.id }
        if (index >= 0) customers[index] = customer else customers.add(customer)
        return Result.Success(Unit)
    }

    override suspend fun saveMold(mold: Mold): Result<Unit> {
        delay(500)
        val index = molds.indexOfFirst { it.id == mold.id }
        if (index >= 0) molds[index] = mold else molds.add(mold)
        return Result.Success(Unit)
    }
}
