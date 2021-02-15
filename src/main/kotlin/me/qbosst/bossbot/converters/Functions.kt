@file:OptIn(ConverterToOptional::class, ConverterToDefaulting::class, ConverterToMulti::class, KordPreview::class)

package me.qbosst.bossbot.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import me.qbosst.bossbot.converters.impl.*
import me.qbosst.bossbot.util.Colour
import java.time.Duration
import java.time.ZoneId


// region: Converters

fun <T: Any> SingleConverter<T>.toCoalescing(
    signatureTypeString: String? = null,
    showTypeInSignature: Boolean? = null,
    errorTypeString: String? = null,
    shouldThrow: Boolean = false
) = SingleToCoalescingConverter(this, signatureTypeString, showTypeInSignature, errorTypeString, shouldThrow)

// region: Single Converters

fun Arguments.colour(
    displayName: String,
    description: String,
    colourProvider: suspend (CommandContext) -> Map<String, Colour> = { mapOf() }
) = arg(displayName, description, ColourConverter(colourProvider))

fun Arguments.duration(displayName: String, description: String) =
    arg(displayName, description, DurationConverter())

fun Arguments.maxLengthString(displayName: String, description: String, minLength: Int = 0, maxLength: Int) =
    arg(displayName, description, MaxLengthStringConverter(minLength, maxLength))

fun Arguments.zoneId(displayName: String, description: String) =
    arg(displayName, description, ZoneIdConverter())

// region: Single Optional Converters

fun Arguments.optionalColour(
    displayName: String,
    description: String,
    colourProvider: suspend (CommandContext) -> Map<String, Colour> = { mapOf() },
    outputError: Boolean = false
) = arg(displayName, description, ColourConverter(colourProvider).toOptional(outputError = outputError))

fun Arguments.optionalDuration(displayName: String, description: String, outputError: Boolean = false) =
    arg(displayName, description, DurationConverter().toOptional(outputError = outputError))

fun Arguments.optionalMaxLengthString(
    displayName: String,
    description: String,
    minLength: Int = 0,
    maxLength: Int,
    outputError: Boolean = false
) = arg(displayName, description, MaxLengthStringConverter(minLength, maxLength).toOptional(outputError = outputError))

fun Arguments.optionalZoneId(displayName: String, description: String, outputError: Boolean = false) =
    arg(displayName, description, ZoneIdConverter().toOptional(outputError = outputError))

// region: Single Default Converters

fun Arguments.defaultingColour(
    displayName: String,
    description: String,
    colourProvider: suspend (CommandContext) -> Map<String, Colour> = { mapOf() },
    defaultColour: Colour
) = arg(displayName, description, ColourConverter(colourProvider).toDefaulting(defaultColour))

fun Arguments.defaultingDuration(displayName: String, description: String, defaultValue: Duration) =
    arg(displayName, description, DurationConverter().toDefaulting(defaultValue))

fun Arguments.defaultingMaxLengthString(
    displayName: String,
    description: String,
    minLength: Int = 0,
    maxLength: Int,
    defaultValue: String
) = arg(displayName, description, MaxLengthStringConverter(minLength, maxLength).toDefaulting(defaultValue))

fun Arguments.defaultingZoneId(displayName: String, description: String, defaultValue: ZoneId) =
    arg(displayName, description, ZoneIdConverter().toDefaulting(defaultValue))

// region: Single List Converters

fun Arguments.colourList(
    displayName: String,
    description: String,
    required: Boolean = true,
    colourProvider: suspend (CommandContext) -> Map<String, Colour> = { mapOf() },
) = arg(displayName, description, ColourConverter(colourProvider).toMulti(required = required))

// region: Coalescing Converters

fun Arguments.coalescedColour(
    displayName: String,
    description: String,
    shouldThrow: Boolean = false,
    colourProvider: suspend (CommandContext) -> Map<String, Colour> = { mapOf() },
) = arg(displayName, description, ColourConverter(colourProvider).toCoalescing(shouldThrow = shouldThrow))

fun Arguments.coalescedZoneId(displayName: String, description: String, shouldThrow: Boolean = false) =
    arg(displayName, description, ZoneIdConverter().toCoalescing(shouldThrow = shouldThrow))

fun Arguments.coalescedMaxLengthString(displayName: String, description: String, minLength: Int = 0, maxLength: Int, shouldThrow: Boolean = false) =
    arg(displayName, description, MaxLengthStringConverter(minLength, maxLength).toCoalescing(shouldThrow = shouldThrow))

// region: Optional Coalescing Converters

fun Arguments.optionalCoalescedDuration(
    displayName: String,
    description: String,
    outputError: Boolean = false
) = arg(displayName, description, DurationConverter().toCoalescing(shouldThrow = outputError).toOptional(outputError = outputError))


