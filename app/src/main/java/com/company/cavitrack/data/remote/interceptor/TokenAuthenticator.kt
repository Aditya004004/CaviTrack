package com.company.cavitrack.data.remote.interceptor

import com.company.cavitrack.data.remote.api.CaviTrackApi
import com.company.cavitrack.data.remote.dto.RefreshTokenRequestDto
import com.company.cavitrack.util.TokenManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiProvider: Provider<CaviTrackApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If the request itself was a refresh token request, give up to avoid loops
        if (response.request.url.encodedPath.contains("auth/refresh")) {
            tokenManager.clearTokens()
            return null
        }

        synchronized(this) {
            val oldToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // If the token has changed since the request was made, just retry with the new token
            if (oldToken != null && requestToken != oldToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $oldToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken() ?: return null

            return try {
                val refreshResponse = apiProvider.get().refreshTokenSync(
                    RefreshTokenRequestDto(refreshToken)
                ).execute()

                if (refreshResponse.isSuccessful) {
                    val newToken = refreshResponse.body()?.token
                    if (newToken != null) {
                        tokenManager.saveTokens(newToken, refreshToken)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    } else {
                        tokenManager.clearTokens()
                        null
                    }
                } else {
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        }
    }
}

