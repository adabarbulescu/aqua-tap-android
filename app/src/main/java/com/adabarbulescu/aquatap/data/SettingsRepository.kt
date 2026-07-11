package com.adabarbulescu.aquatap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val pairedTagId: Flow<String?>
    val dailyGoalMl: Flow<Int>
    suspend fun savePairedTagId(tagId: String)
    suspend fun clearPairedTagId()
    suspend fun updateDailyGoal(goalMl: Int)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {
    private val bottleTagIdKey = stringPreferencesKey("bottle_tag_id")
    private val dailyGoalKey = intPreferencesKey("daily_goal_ml")

    override val pairedTagId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[bottleTagIdKey]
        }

    override val dailyGoalMl: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[dailyGoalKey] ?: 2000
        }

    override suspend fun savePairedTagId(tagId: String) {
        context.dataStore.edit { preferences ->
            preferences[bottleTagIdKey] = tagId
        }
    }

    override suspend fun clearPairedTagId() {
        context.dataStore.edit { preferences ->
            preferences.remove(bottleTagIdKey)
        }
    }

    override suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[dailyGoalKey] = goalMl
        }
    }
}
