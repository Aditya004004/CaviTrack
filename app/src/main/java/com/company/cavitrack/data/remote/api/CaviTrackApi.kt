package com.company.cavitrack.data.remote.api

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import retrofit2.Response
import retrofit2.http.*

interface CaviTrackApi {

    @GET("api/components")
    suspend fun getComponents(): Response<List<Component>>

    @POST("api/components")
    suspend fun createComponent(@Body component: Component): Response<Component>

    @PUT("api/components/{id}")
    suspend fun updateComponent(@Path("id") id: String, @Body component: Component): Response<Component>

    @GET("api/customers")
    suspend fun getCustomers(): Response<List<Customer>>

    @POST("api/customers")
    suspend fun createCustomer(@Body customer: Customer): Response<Customer>

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: Customer): Response<Customer>

    @GET("api/molds")
    suspend fun getMolds(): Response<List<Mold>>

    @POST("api/molds")
    suspend fun createMold(@Body mold: Mold): Response<Mold>

    @PUT("api/molds/{id}")
    suspend fun updateMold(@Path("id") id: String, @Body mold: Mold): Response<Mold>

    @GET("api/history")
    suspend fun getHistoryLogs(): Response<List<HistoryLog>>
}
