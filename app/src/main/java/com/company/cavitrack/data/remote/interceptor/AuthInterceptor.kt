package com.company.cavitrack.data.remote.interceptor

import com.company.cavitrack.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        // Do not add header for auth/login or auth/register requests
        if (!chain.request().url.encodedPath.contains("auth/login") && 
            !chain.request().url.encodedPath.contains("auth/register") && 
            !chain.request().url.encodedPath.contains("auth/refresh")) {
            tokenManager.getAccessToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}
