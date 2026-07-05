package com.adabarbulescu.aquatap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BottleTagRepository {
    val pairedTagId: Flow<String?>
    suspend fun savePairedTagId(tagId: String)
    suspend fun clearPairedTagId()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DataStoreBottleTagRepository(private val context: Context) : BottleTagRepository {
    private val bottleTagIdKey = stringPreferencesKey("bottle_tag_id")

    override val pairedTagId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[bottleTagIdKey]
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
}
