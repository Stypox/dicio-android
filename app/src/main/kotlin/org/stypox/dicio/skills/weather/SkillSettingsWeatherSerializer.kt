package org.stypox.dicio.skills.weather

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object SkillSettingsWeatherSerializer : Serializer<SkillSettingsWeather> {
    override val defaultValue: SkillSettingsWeather = SkillSettingsWeather.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SkillSettingsWeather {
        try {
            return SkillSettingsWeather.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: SkillSettingsWeather, output: OutputStream) {
        t.writeTo(output)
    }
}
