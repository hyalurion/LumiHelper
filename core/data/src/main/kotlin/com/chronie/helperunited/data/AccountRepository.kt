package com.chronie.helperunited.data

import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.HoYoAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for account management.
 * Ported from PizzaHelperUnited's account persistence layer.
 */
@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
) {
    fun observeAllAccounts(): Flow<List<HoYoAccount>> =
        accountDao.observeAll().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }

    suspend fun getAllAccounts(): List<HoYoAccount> =
        accountDao.getAll().mapNotNull { it.toDomain() }

    suspend fun getAccount(uuid: String): HoYoAccount? =
        accountDao.getById(uuid)?.toDomain()

    suspend fun findAccount(uid: String, game: SupportedGame): HoYoAccount? =
        accountDao.findByUidAndGame(uid, game.rawValue)?.toDomain()

    suspend fun addAccount(account: HoYoAccount) {
        accountDao.insert(AccountEntity.fromDomain(account))
    }

    suspend fun updateAccount(account: HoYoAccount) {
        accountDao.update(AccountEntity.fromDomain(account))
    }

    suspend fun deleteAccount(uuid: String) {
        accountDao.deleteById(uuid)
    }

    suspend fun getAccountsByGame(game: SupportedGame): List<HoYoAccount> =
        accountDao.getAll().mapNotNull { it.toDomain() }.filter { it.game == game }
}
