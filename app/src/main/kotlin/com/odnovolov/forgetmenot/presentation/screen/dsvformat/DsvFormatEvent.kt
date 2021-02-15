package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import org.apache.commons.csv.QuoteMode

sealed class DsvFormatEvent {
    object BackButtonClicked : DsvFormatEvent()
    object CancelButtonClicked : DsvFormatEvent()
    object DoneButtonClicked : DsvFormatEvent()
    object FormatNameButtonClicked : DsvFormatEvent()
    object DeleteFormatButtonClicked : DsvFormatEvent()
    object CloseTipButtonClicked : DsvFormatEvent()
    class DelimiterChanged(val delimiter: Char?) : DsvFormatEvent()
    class TrailingDelimiterChanged(val trailingDelimiter: Boolean) : DsvFormatEvent()
    class QuoteCharacterChanged(val quoteCharacter: Char?) : DsvFormatEvent()
    class QuoteModeChanged(val quoteMode: QuoteMode?) : DsvFormatEvent()
    class EscapeCharacterChanged(val escapeCharacter: Char?) : DsvFormatEvent()
    class NullStringChanged(val nullString: String?) : DsvFormatEvent()
    class IgnoreSurroundingSpacesChanged(val ignoreSurroundingSpaces: Boolean) : DsvFormatEvent()
    class TrimChanged(val trim: Boolean) : DsvFormatEvent()
    class IgnoreEmptyLinesChanged(val ignoreEmptyLines: Boolean) : DsvFormatEvent()
    class RecordSeparatorChanged(val recordSeparator: String?) : DsvFormatEvent()
    class CommentMarkerChanged(val commentMarker: Char?) : DsvFormatEvent()
    class SkipHeaderRecordChanged(val skipHeaderRecord: Boolean) : DsvFormatEvent()
    class HeaderChanged(val header: Array<String>?) : DsvFormatEvent()
    class IgnoreHeaderCaseChanged(val ignoreHeaderCase: Boolean) : DsvFormatEvent()
    class AllowDuplicateHeaderNamesChanged(val allowDuplicateHeaderNames: Boolean) : DsvFormatEvent()
    class AllowMissingColumnNamesChanged(val allowMissingColumnNames: Boolean) : DsvFormatEvent()
    class HeaderCommentsChanged(val headerComments: Array<String>?) : DsvFormatEvent()
    class AutoFlushChanged(val autoFlush: Boolean) : DsvFormatEvent()
}