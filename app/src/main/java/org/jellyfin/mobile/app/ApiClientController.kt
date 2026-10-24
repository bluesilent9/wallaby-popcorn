package org.jellyfin.mobile.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.mobile.data.dao.ServerDao
import org.jellyfin.mobile.data.dao.UserDao
import org.jellyfin.mobile.data.entity.ServerEntity
import org.jellyfin.mobile.data.entity.ServerUser
import org.jellyfin.mobile.data.entity.UserEntity
import org.jellyfin.mobile.setup.SshServerAddress
import org.jellyfin.mobile.setup.ssh.SshTunnelManager
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.DeviceInfo
import java.util.UUID

class ApiClientController(
    private val appPreferences: AppPreferences,
    private val jellyfin: Jellyfin,
    private val apiClient: ApiClient,
    private val serverDao: ServerDao,
    private val userDao: UserDao,
    private val sshTunnelManager: SshTunnelManager,
) {
    private val baseDeviceInfo: DeviceInfo
        get() = jellyfin.options.deviceInfo!!

    /**
     * Store server with [hostname] in the database.
     */
    suspend fun setupServer(hostname: String) {
        val endpoint = resolveEndpoint(hostname)
        appPreferences.currentServerId = withContext(Dispatchers.IO) {
            serverDao.getServerByHostname(hostname)?.id ?: serverDao.insert(hostname)
        }
        apiClient.update(baseUrl = endpoint)
    }

    suspend fun setupUser(serverId: Long, userId: UUID, accessToken: String) {
        appPreferences.currentUserId = withContext(Dispatchers.IO) {
            userDao.upsert(serverId, userId, accessToken)
        }
        configureApiClientUser(userId, accessToken)
    }

    suspend fun loadSavedServer(): ServerEntity? {
        val server = loadSavedServerProfile()
        configureApiClientServer(server)
        return server
    }

    suspend fun loadSavedServerProfile(): ServerEntity? = withContext(Dispatchers.IO) {
        val serverId = appPreferences.currentServerId ?: return@withContext null
        serverDao.getServer(serverId)
    }

    suspend fun loadSavedUser(): UserEntity? = withContext(Dispatchers.IO) {
        val userId = appPreferences.currentUserId ?: return@withContext null
        userDao.getUser(userId)
    }

    suspend fun loadSavedServerUser(): ServerUser? {
        val serverUser = withContext(Dispatchers.IO) {
            val serverId = appPreferences.currentServerId ?: return@withContext null
            val userId = appPreferences.currentUserId ?: return@withContext null
            userDao.getServerUser(serverId, userId)
        }

        configureApiClientServer(serverUser?.server)

        if (serverUser?.user?.accessToken != null) {
            configureApiClientUser(serverUser.user.userId, serverUser.user.accessToken)
        } else {
            resetApiClientUser()
        }

        return serverUser
    }

    suspend fun loadPreviouslyUsedServers(): List<ServerEntity> = withContext(Dispatchers.IO) {
        serverDao.getAllServers().filterNot { server ->
            server.id == appPreferences.currentServerId
        }
    }

    private suspend fun configureApiClientServer(server: ServerEntity?) {
        if (server == null) sshTunnelManager.disconnect()
        apiClient.update(baseUrl = server?.let { resolveEndpoint(it.hostname) })
    }

    private suspend fun resolveEndpoint(hostname: String): String {
        val sshAddress = SshServerAddress.parse(hostname) ?: run {
            sshTunnelManager.disconnect()
            return hostname
        }
        return sshTunnelManager.currentEndpoint(sshAddress)
            ?: sshTunnelManager.connectWithEnrolledKey(sshAddress)
    }

    fun endpointForExistingConnection(hostname: String): String {
        val sshAddress = SshServerAddress.parse(hostname) ?: return hostname
        return sshTunnelManager.currentEndpoint(sshAddress)
            ?: throw IllegalStateException("The SSH tunnel is not active; direct server access is disabled")
    }

    private fun configureApiClientUser(userId: UUID, accessToken: String) {
        apiClient.update(
            accessToken = accessToken,
            // Append user id to device id to ensure uniqueness across sessions
            deviceInfo = baseDeviceInfo.copy(id = baseDeviceInfo.id + userId),
        )
    }

    private fun resetApiClientUser() {
        apiClient.update(
            accessToken = null,
            deviceInfo = baseDeviceInfo,
        )
    }

    fun getApiClient(server: Long, user: Long): ApiClient {
        val serverUser = userDao.getServerUser(server, user) ?: error("Invalid server user combination (server=$server, user=$user)")

        return jellyfin.createApi(
            baseUrl = endpointForExistingConnection(serverUser.server.hostname),
            accessToken = serverUser.user.accessToken,
            deviceInfo = baseDeviceInfo.copy(id = baseDeviceInfo.id + serverUser.user.userId),
        )
    }
}
