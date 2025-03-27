package main

import cache.ApiCache
import network.ApiClient
import network.GitHubUser
import network.GitHubRepo

suspend fun main() {
    val apiCache = ApiCache<String, Pair<GitHubUser, List<String>>>()

    suspend fun fetchGitHubUser(username: String): Pair<GitHubUser, List<String>> {
        return apiCache.getOrFetch(username) {
            val user = ApiClient.apiService.getUser(username)
            val repos = ApiClient.apiService.getUserRepos(username)
            user to repos.map { it.name }
        }
    }

    fun listCachedUsers() {
        val cachedUsers = apiCache.getCachedKeys()
        if (cachedUsers.isEmpty()) {
            println("No users in cache.")
        } else {
            println("Cached Users: ${cachedUsers.joinToString(", ")}")
        }
    }

    fun searchUserInCache(username: String) {
        val result = apiCache.getCachedValue(username)
        if (result != null) {
            println("User found: ${result.first.login}")
        } else {
            println("User not found in cache.")
        }
    }

    fun searchRepoInCache(repoName: String) {
        val matchingUsers = apiCache.getCachedKeys().filter { username ->
            val (_, repos) = apiCache.getCachedValue(username) ?: return@filter false
            repoName in repos
        }
    
        if (matchingUsers.isNotEmpty()) {
            println("Users with repository '$repoName': ${matchingUsers.joinToString(", ")}")
        } else {
            println("No cached user has a repository named '$repoName'.")
        }
    }

    while (true) {
        println("\n--- GitHub User Data CLI ---")
        println("1. Retrieve user data")
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
                    println("Username: ${user.login}")
                    println("Followers: ${user.followers}")
                    println("Following: ${user.following}")
                    println("Account Created: ${user.created_at}")
                    println("Repositories: ${repos.joinToString(", ")}")
                } else {
                    println("Invalid input!")
                }
            }

            "2" -> listCachedUsers()

            "3" -> {
                print("Enter username to search in cache: ")
                val username = readLine()?.trim()
                if (!username.isNullOrEmpty()) {
                    searchUserInCache(username)
                } else {
                    println("Invalid input!")
                }
            }

            "4" -> {
                print("Enter repository name to search in cache: ")
                val repoName = readLine()?.trim()
                if (!repoName.isNullOrEmpty()) {
                    searchRepoInCache(repoName)
                } else {
                    println("Invalid input!")
                }
            }

            "5" -> {
                println("Exiting program.")
                return
            }

            else -> println("Invalid option. Please try again.")
        }
    }
}
