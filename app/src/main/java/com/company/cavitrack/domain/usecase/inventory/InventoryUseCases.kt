package com.company.cavitrack.domain.usecase.inventory

import androidx.paging.PagingData
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class InventoryUseCases @Inject constructor(
    val getComponents: GetComponentsUseCase,
    val getCustomers: GetCustomersUseCase,
    val getMolds: GetMoldsUseCase,
    val getHistory: GetHistoryUseCase,
    val getComponent: GetComponentUseCase,
    val getCustomer: GetCustomerUseCase,
    val getMold: GetMoldUseCase,
    val saveComponent: SaveComponentUseCase,
    val saveCustomer: SaveCustomerUseCase,
    val saveMold: SaveMoldUseCase,
    val deleteComponent: DeleteComponentUseCase,
    val deleteCustomer: DeleteCustomerUseCase,
    val deleteMold: DeleteMoldUseCase,
    val saveHistoryLog: SaveHistoryLogUseCase
)

class GetComponentsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(): Flow<PagingData<Component>> {
        return repository.getComponents()
    }
}

class GetCustomersUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(): Flow<PagingData<Customer>> {
        return repository.getCustomers()
    }
}

class GetMoldsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(): Flow<PagingData<Mold>> {
        return repository.getMolds()
    }
}

class GetHistoryUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(): Flow<DataResult<List<HistoryLog>>> {
        return repository.getHistory()
    }
}

class GetComponentUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Component> {
        return repository.getComponent(id)
    }
}

class GetCustomerUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Customer> {
        return repository.getCustomer(id)
    }
}

class GetMoldUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Mold> {
        return repository.getMold(id)
    }
}

class SaveComponentUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(component: Component): DataResult<Unit> {
        return repository.saveComponent(component)
    }
}

class SaveCustomerUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(customer: Customer): DataResult<Unit> {
        return repository.saveCustomer(customer)
    }
}

class SaveMoldUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(mold: Mold): DataResult<Unit> {
        return repository.saveMold(mold)
    }
}

class DeleteComponentUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Unit> {
        return repository.deleteComponent(id)
    }
}

class DeleteCustomerUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Unit> {
        return repository.deleteCustomer(id)
    }
}

class DeleteMoldUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(id: String): DataResult<Unit> {
        return repository.deleteMold(id)
    }
}

class SaveHistoryLogUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(log: HistoryLog): DataResult<Unit> {
        return repository.saveHistoryLog(log)
    }
}
