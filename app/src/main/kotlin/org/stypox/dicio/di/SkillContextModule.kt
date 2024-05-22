package org.stypox.dicio.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.dicio.skill.context.SkillContext
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.io.input.vosk.VoskInputDevice
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SkillContextModule {
    @Provides
    @Singleton
    fun provideSkillContext(skillContextImpl: SkillContextImpl): SkillContext = skillContextImpl

    @Provides
    @Singleton
    fun provideSttInputDevice(voskInputDevice: VoskInputDevice): SttInputDevice? {
        // TODO read from settings
        return voskInputDevice
    }
}
