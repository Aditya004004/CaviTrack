package com.company.cavitrack.data.remote.api

import com.company.cavitrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CaviTrackApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/refresh")
    fun refreshTokenSync(@Body request: RefreshTokenRequestDto): retrofit2.Call<RefreshTokenResponseDto>

    @GET("api/components")
    suspend fun getComponents(): Response<List<ComponentDto>>

    @POST("api/components")
    suspend fun createComponent(@Body component: ComponentDto): Response<ComponentDto>

    @PUT("api/components/{id}")
    suspend fun updateComponent(@Path("id") id: String, @Body component: ComponentDto): Response<ComponentDto>

    @GET("api/customers")
    suspend fun getCustomers(): Response<List<CustomerDto>>

    @POST("api/customers")
    suspend fun createCustomer(@Body customer: CustomerDto): Response<CustomerDto>

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: CustomerDto): Response<CustomerDto>

    @GET("api/molds")
    suspend fun getMolds(): Response<List<MoldDto>>

    @POST("api/molds")
    suspend fun createMold(@Body mold: MoldDto): Response<MoldDto>

    @PUT("api/molds/{id}")
    suspend fun updateMold(@Path("id") id: String, @Body mold: MoldDto): Response<MoldDto>

    @GET("api/history")
    suspend fun getHistoryLogs(): Response<List<HistoryLogDto>>
}
