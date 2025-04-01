package network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response

object ApiClient {
    private val apiService: ApiService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun printApiError(username: String, code: Int, message: String){
        when (code) {
            404 -> {
                print("\nUser '$username' not found [HTTP 404].\n")
            }
            500 -> {
                print("\nServer error while fetching user '$username' [HTTP 500]. Please try again later.\n")
            }
            else -> {
                print("\nError fetching user '$username': HTTP ${code} - ${message}\n")
            }
        }
    }

    suspend fun getUser(username: String): GitHubUser? {
        return try {
            val response = apiService.getUser(username)
            return response.body()?.takeIf { response.isSuccessful } ?: run {
                printApiError(username, response.code(), response.message())
                null
            }
        } catch (e: java.net.UnknownHostException) {
            print("Network error: Could not reach the server. Please check your internet connection.")
            null
        } catch (e: java.net.SocketTimeoutException) {
            print("Network error: Request timed out. Please try again later.")
            null
        } catch (e: Exception) {
            print("Unexpected error: ${e.message}")
            null
        }
    }

    suspend fun getUserRepos(username: String): List<String>? {
        return try {
            val response = apiService.getUserRepos(username)
            if (response.isSuccessful) {
                response.body()?.map { it.name } ?: emptyList()
            } else {
                printApiError(username, response.code(), response.message())
                null
            }
        } catch (e: java.net.UnknownHostException) {
            print("Network error: Could not reach the server. Please check your internet connection.")
            null
        } catch (e: java.net.SocketTimeoutException) {
            print("Network error: Request timed out. Please try again later.")
            null
        } catch (e: Exception) {
            print("Unexpected error: ${e.message}")
            null
        }
    }        
}
