package ru.netology.nework.service

import com.google.firebase.installations.FirebaseInstallations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseInstallationsModule {
    @Provides
    @Singleton
    fun provideFirebaseInstallationsModule(): FirebaseInstallations {
        return FirebaseInstallations.getInstance()
    }
}