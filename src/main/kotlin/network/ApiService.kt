package network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

data class GitHubUser(
    val login: String,
    val followers: Int,
    val following: Int,
    val created_at: String,
    val repos_url: String
)

data class GitHubRepo(
    val name: String 
)

interface ApiService {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): Response<GitHubUser>

    @GET("users/{username}/repos")
    suspend fun getUserRepos(@Path("username") username: String): Response<List<GitHubRepo>>
}
