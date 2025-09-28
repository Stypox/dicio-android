package org.stypox.dicio.skills.translation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.cldr.CldrLanguages.LocaleAndTranslation
import org.stypox.dicio.di.SkillContextImpl
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.io.graphical.Subtitle
import org.stypox.dicio.ui.home.SkillAnswerCard
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.getString

sealed interface TranslationOutput : SkillOutput {
    object EmptyQuery: HeadlineSpeechSkillOutput, TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_failed_no_query
        )
    }

    data class UnknownLanguage(val query: String) : TranslationOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_translation_unknown_language, query)
    }

    data class UnsupportedLanguage(val language: LocaleAndTranslation) : TranslationOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_unsupported_language,
            language.translation,
            language.locale
        )
    }

    data class ApiError(
        val error: String,
        val sourceLanguage: LocaleAndTranslation?,
        val targetLanguage: LocaleAndTranslation,
    ): TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_failed, targetLanguage.translation
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Headline(text = getSpeechOutput(ctx))
                Subtitle(text = error)
                TranslationHeader(sourceLanguage, targetLanguage)
            }
        }
    }

    data class Success(
        val translation: String,
        val sourceLanguage: LocaleAndTranslation?,
        val targetLanguage: LocaleAndTranslation,
    ): TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_success, targetLanguage.translation
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = translation,
                        textAlign = if (translation.length < 100) {
                            TextAlign.Center
                        } else {
                            TextAlign.Justify
                        },
                        style = if (translation.length < 100) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = { ShareUtils.copyToClipboard(ctx.android, translation) },
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy_to_clipboard),
                        )
                    }
                    TranslationHeader(
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage,
                        modifier = Modifier.weight(1.0f, fill = false),
                    )
                    IconButton(
                        onClick = { ShareUtils.shareText(ctx.android, "", translation) },
                        modifier = Modifier.padding(start = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationHeader(
    sourceLanguage: LocaleAndTranslation?,
    targetLanguage: LocaleAndTranslation,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TranslationLanguage(
                languageName = sourceLanguage?.translation
                    ?: stringResource(R.string.skill_translation_auto),
                locale = sourceLanguage?.locale,
                alignment = Alignment.End,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = stringResource(R.string.skill_translation_to),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(20.dp)
            )
            TranslationLanguage(
                languageName = targetLanguage.translation,
                locale = targetLanguage.locale,
                alignment = Alignment.Start,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

@Preview
@Composable
private fun TranslationHeaderPreview() {
    Column {
        TranslationHeader(
            LocaleAndTranslation("it", "Italian", ""),
            LocaleAndTranslation("en", "English", ""),
        )
        TranslationHeader(
            null,
            LocaleAndTranslation("long ".repeat(20), "Long ".repeat(20), ""),
        )
    }
}

@Composable
fun TranslationLanguage(
    languageName: String,
    locale: String?,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                append(languageName)
            }
            if (locale != null) {
                append(" (")
                append(locale)
                append(")")
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        textAlign = if (alignment == Alignment.Start) TextAlign.Start else TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TranslationLanguagePreview() {
    TranslationLanguage(
        languageName = "Italian",
        locale = "it",
        Alignment.End
    )
}

class TranslationOutputPrevParams : CollectionPreviewParameterProvider<TranslationOutput>(listOf(
    TranslationOutput.EmptyQuery,
    TranslationOutput.UnknownLanguage("languagewhichdoesntmakesense"),
    TranslationOutput.UnsupportedLanguage(LocaleAndTranslation("ace", "Acehnese", "")),
    TranslationOutput.ApiError("Invalid source language", null, LocaleAndTranslation("en", "English", "")),
    TranslationOutput.ApiError("Another long error ".repeat(10), LocaleAndTranslation("long long long", "Veeeery long language name", ""), LocaleAndTranslation("en", "English", "")),
    TranslationOutput.Success("Good translation", null, LocaleAndTranslation("en", "English", "")),
    TranslationOutput.Success(LoremIpsum(50).values.first().replace("\n", " "), LocaleAndTranslation("long long long", "Veeeery long language name", ""), LocaleAndTranslation("en", "English", "")),
))

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun TranslationOutputPreview(
    @PreviewParameter(TranslationOutputPrevParams::class) translationOutput: TranslationOutput
) {
    AppTheme {
        SkillAnswerCard {
            translationOutput.GraphicalOutput(SkillContextImpl.newForPreviews(LocalContext.current))
        }
    }
}
