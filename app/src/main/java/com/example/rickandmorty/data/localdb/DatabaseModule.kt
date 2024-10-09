package com.example.rickandmorty.data.localdb

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RickAndMortyDatabase =
        Room.databaseBuilder(
            context,
            RickAndMortyDatabase::class.java,
            "character_database"
        ).build()

    @Provides
    fun provideCharacterDao(database: RickAndMortyDatabase): CharacterDao = database.characterDao()
}