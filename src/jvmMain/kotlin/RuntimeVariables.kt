object RuntimeVariables {
    val botRedditUsername: String = System.getenv("markovbaj_username")
    val botRedditPassword: String = System.getenv("markovbaj_password")
    val botRedditClientId: String = System.getenv("markovbaj_clientid")
    val botRedditClientSecret: String = System.getenv("markovbaj_clientsecret")
    val botActuallySendReplies = System.getenv("markovbaj_actuallysendreplies") == "true"
    val botAppId: String = System.getenv("markovbaj_appid")
    val botAuthorRedditUsername: String = System.getenv("markovbaj_authorredditusername")

    val backendServerUrl: String = System.getenv("markovbaj_backend_serverurl")
    val backendServerPort = System.getenv("markovbaj_backend_serverport").toInt()
    val backendRedditClientId: String = System.getenv("markovbaj_backend_clientid")
    val backendRedditClientSecret: String = System.getenv("markovbaj_backend_clientsecret")
    val backendPermittedUsers = System.getenv("markovbaj_backend_permittedusers").split(",")
}