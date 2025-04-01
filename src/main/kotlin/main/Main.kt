package main

import cache.ApiCache
import network.ApiClient
import network.GitHubUser
import network.GitHubRepo
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

suspend fun main() {
    val apiCache = ApiCache<String, Pair<GitHubUser, List<String>>>()

    suspend fun fetchGitHubUser(username: String): Pair<GitHubUser, List<String>> {
        return apiCache.getOrFetch(username) {
            val user = ApiClient.getUser(username)
            val repos = if (user != null) {
                ApiClient.getUserRepos(username)
            } else {
                null
            }
            if (user != null && repos != null) {
                user to repos
            } else {
                GitHubUser("", 0, 0, "", "") to emptyList()
            }   
        }
    }    

    fun printUserData(user: GitHubUser, repos: List<String>) {
        if (user.login != ""){
            println("\nUsername: ${user.login}")
            println("Followers: ${user.followers}")
            println("Following: ${user.following}")
            val date = Instant.parse(user.created_at)
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
            println("Account Created: ${date}")
            println("Repositories: ${repos.joinToString(", ")}")
        }
    }

    fun listCachedUsers() {
        val cachedUsers = apiCache.getCachedKeys()
        if (cachedUsers.isEmpty()) {
            println("\nNo users in cache.")
        } else {
            println("\nCached Users: ${cachedUsers.joinToString(", ")}")
        }
    }

    fun searchUserInCache(username: String) {
        val result = apiCache.getCachedValue(username)
        if (result != null) {
            println("\nUser ${result.first.login} found in cache.")
        } else {
            println("\nUser ${username} not found in cache.")
        }
    }

    fun searchRepoInCache(repoName: String) {
        val matchingUsers = apiCache.getCachedKeys().filter { username ->
            val (_, repos) = apiCache.getCachedValue(username) ?: return@filter false
            repoName in repos
        }
    
        if (matchingUsers.isNotEmpty()) {
            println("\nUsers with repository '$repoName': ${matchingUsers.joinToString(", ")}")
        } else {
            println("\nNo cached user has a repository named '$repoName'.")
        }
    }

    while (true) {
        println("\n----- GitHub User Data CLI -----")
        println("1. Get user data by username")
        println("2. List cached users")
        println("3. Search cached users by username")
        println("4. Search cached data by repository name")
        println("5. Quit")
        print("Choose an option: ")

        when (readLine()?.trim()) {
            "1" -> {
                print("Enter GitHub username: ")
                val username = readLine()?.trim()
                if (!username.isNullOrEmpty()) {
                    val (user, repos) = fetchGitHubUser(username)
                    printUserData(user, repos)
                } else {
                    println("\nInvalid input.")
                }
            }

            "2" -> listCachedUsers()

            "3" -> {
                print("\nEnter GitHub username: ")
                val username = readLine()?.trim()
                if (!username.isNullOrEmpty()) {
                    searchUserInCache(username)
                } else {
                    println("\nInvalid input.")
                }
            }

            "4" -> {
                print("Enter GitHub repository name: ")
                val repoName = readLine()?.trim()
                if (!repoName.isNullOrEmpty()) {
                    searchRepoInCache(repoName)
                } else {
                    println("\nInvalid input.")
                }
            }

            "5" -> {
                println("\nExiting program.")
                return
            }

            else -> println("\nInvalid option. Please try again.")
        }
    }
}
