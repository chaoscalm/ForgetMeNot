package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.*
import kotlinx.android.synthetic.main.fragment_dsv_format.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.coroutines.launch
import org.apache.commons.csv.QuoteMode

class DsvFormatFragment : BaseFragment() {
    init {
        DsvFormatDiScope.reopenIfClosed()
    }

    private var controller: DsvFormatController? = null
    private lateinit var viewModel: DsvFormatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dsv_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DsvFormatDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        tipTextView.setTextWithClickableAnnotations(
            stringId = R.string.tip_dsv_format,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "csv_library" -> openUrl(APACHE_COMMONS_CSV_LIBRARY_URL)
                    "CSVFormat" -> openUrl(CSV_FORMAT_URL)
                }
            },
            linkColor = Color.WHITE
        )
        closeTipButton.setOnClickListener {
            controller?.dispatch(CloseTipButtonClicked)
        }
        recordSeparatorEditText.isSelected = true
        delimiterEditText.isSelected = true
        nullStringEditText.isSelected = true
    }

    private fun observeViewModel() {
        with(viewModel) {
            formatName.observe { formatName: String ->
                dsvFormatNameTextView.text = formatName
            }
            isTipVisible.observe { isTipVisible: Boolean ->
                tipLayout.isVisible = isTipVisible
            }
            setReadOnly(isReadOnly)
            delimiterEditText.setText(delimiter?.toString()?.toDisplayedString())
            trailingDelimiter.observe { trailingDelimiter: Boolean ->
                yesTrailingDelimiterButton.isSelected = trailingDelimiter
                noTrailingDelimiterButton.isSelected = !trailingDelimiter
            }
            quoteCharacterEditText.setText(
                quoteCharacter.firstBlocking()?.toString()?.toDisplayedString()
            )
            quoteCharacter.observe { quoteCharacter: Char? ->
                quoteCharacterEditText.isSelected = quoteCharacter != null
                disabledQuoteCharacterButton.isSelected = quoteCharacter == null
            }
            quoteMode.observe { quoteMode: QuoteMode? ->
                allQuoteModeButton.isSelected = quoteMode == QuoteMode.ALL
                allNonNullQuoteModeButton.isSelected = quoteMode == QuoteMode.ALL_NON_NULL
                minimalQuoteModeButton.isSelected =
                    quoteMode == null || quoteMode == QuoteMode.MINIMAL
                nonNumericQuoteModeButton.isSelected = quoteMode == QuoteMode.NON_NUMERIC
                noneQuoteModeButton.isSelected = quoteMode == QuoteMode.NONE
            }
            escapeCharacterEditText.setText(
                escapeCharacter.firstBlocking()?.toString()?.toDisplayedString()
            )
            escapeCharacter.observe { escapeCharacter: Char? ->
                escapeCharacterEditText.isSelected = escapeCharacter != null
                disabledEscapeCharacterButton.isSelected = escapeCharacter == null
            }
            nullString.observe { nullString: String? ->
                nullStringEditText.isSelected = nullString != null
            }
            ignoreSurroundingSpaces.observe { ignoreSurroundingSpaces: Boolean ->
                yesIgnoreSurroundingSpacesButton.isSelected = ignoreSurroundingSpaces
                noIgnoreSurroundingSpacesButton.isSelected = !ignoreSurroundingSpaces
            }
            trim.observe { trim: Boolean ->
                yesTrimButton.isSelected = trim
                noTrimButton.isSelected = !trim
            }
            ignoreEmptyLines.observe { ignoreEmptyLines: Boolean ->
                yesIgnoreEmptyLinesButton.isSelected = ignoreEmptyLines
                noIgnoreEmptyLinesButton.isSelected = !ignoreEmptyLines
            }
            recordSeparatorEditText.setText(recordSeparator?.toDisplayedString())
            commentCharacterEditText.setText(
                commentMarker.firstBlocking()?.toString()?.toDisplayedString()
            )
            commentMarker.observe { commentMarker: Char? ->
                commentCharacterEditText.isSelected = commentMarker != null
                disabledCommentCharacterButton.isSelected = commentMarker == null
            }
            skipHeaderRecord.observe { skipHeaderRecord: Boolean ->
                yesSkipHeaderRecordButton.isSelected = skipHeaderRecord
                noSkipHeaderRecordButton.isSelected = !skipHeaderRecord
            }
            header?.let { headerColumnNames: Array<String> ->
                headerColumnNames.forEachIndexed { index: Int, columnName: String ->
                    if (index == 0) {
                        firstHeaderColumnNameEditText.setText(columnName)
                    } else {
                        with(headerColumnNamesLinearLayout) {
                            if (childCount < index + 1) {
                                val newInput =
                                    View.inflate(context, R.layout.dsv_list_input, null) as EditText
                                val position = childCount
                                newInput.isEnabled = !isReadOnly
                                newInput.observeText { newText: String ->
                                    controller?.dispatch(HeaderColumnNameChanged(position, newText))
                                }
                                addView(newInput)
                                newInput.updateLayoutParams<MarginLayoutParams> {
                                    topMargin = -(2.dp)
                                }
                            }
                        }
                    }
                }
            }
            headerColumnCount.observe { headerColumnCount: Int ->
                val inputCountShouldBe = headerColumnCount + 1
                with(headerColumnNamesLinearLayout) {
                    while (childCount > inputCountShouldBe) {
                        val lastViewPosition = childCount - 1
                        removeViewAt(lastViewPosition)
                    }
                    while (childCount < inputCountShouldBe) {
                        val newInput =
                            View.inflate(context, R.layout.dsv_list_input, null) as EditText
                        val position = childCount
                        newInput.isEnabled = true //!isReadOnly
                        newInput.observeText { newText: String ->
                            controller?.dispatch(HeaderColumnNameChanged(position, newText))
                        }
                        addView(newInput)
                        newInput.updateLayoutParams<MarginLayoutParams> {
                            topMargin = -(2.dp)
                        }
                    }
                    children.toList().forEachIndexed { index, view ->
                        val isLastView = index == childCount - 1
                        view.isSelected = !isLastView
                    }
                }
            }
            ignoreHeaderCase.observe { ignoreHeaderCase: Boolean ->
                yesIgnoreHeaderNamesCaseButton.isSelected = ignoreHeaderCase
                noIgnoreHeaderNamesCaseButton.isSelected = !ignoreHeaderCase
            }
            allowDuplicateHeaderNames.observe { allowDuplicateHeaderNames: Boolean ->
                yesAllowDuplicateHeaderNamesButton.isSelected = allowDuplicateHeaderNames
                noAllowDuplicateHeaderNamesButton.isSelected = !allowDuplicateHeaderNames
            }
            allowMissingColumnNames.observe { allowMissingColumnNames: Boolean ->
                yesAllowMissingColumnNamesButton.isSelected = allowMissingColumnNames
                noAllowMissingColumnNamesButton.isSelected = !allowMissingColumnNames
            }
            headerComments?.let { headerComments: Array<String> ->
                headerComments.forEachIndexed { index: Int, comment: String ->
                    if (index == 0) {
                        firstHeaderCommentEditText.setText(comment)
                    } else {
                        with(headerCommentsLinearLayout) {
                            if (childCount < index + 1) {
                                val newInput =
                                    View.inflate(context, R.layout.dsv_list_input, null) as EditText
                                val position = childCount
                                newInput.isEnabled = !isReadOnly
                                newInput.observeText { newText: String ->
                                    controller?.dispatch(HeaderCommentChanged(position, newText))
                                }
                                addView(newInput)
                                newInput.updateLayoutParams<MarginLayoutParams> {
                                    topMargin = -(2.dp)
                                }
                            }
                        }
                    }
                }
            }
            headerCommentsCount.observe { headerCommentsCount: Int ->
                val inputCountShouldBe = headerCommentsCount + 1
                with(headerCommentsLinearLayout) {
                    while (childCount > inputCountShouldBe) {
                        val lastViewPosition = childCount - 1
                        removeViewAt(lastViewPosition)
                    }
                    while (childCount < inputCountShouldBe) {
                        val newInput =
                            View.inflate(context, R.layout.dsv_list_input, null) as EditText
                        val position = childCount
                        newInput.isEnabled = true //!isReadOnly
                        newInput.observeText { newText: String ->
                            controller?.dispatch(HeaderCommentChanged(position, newText))
                        }
                        addView(newInput)
                        newInput.updateLayoutParams<MarginLayoutParams> {
                            topMargin = -(2.dp)
                        }
                    }
                    children.toList().forEachIndexed { index, view ->
                        val isLastView = index == childCount - 1
                        view.isSelected = !isLastView
                    }
                }
            }
            autoFlush.observe { autoFlush: Boolean ->
                yesAutoFlushButton.isSelected = autoFlush
                noAutoFlushButton.isSelected = !autoFlush
            }
        }
    }

    private fun setReadOnly(readOnly: Boolean) {
        backButton.isVisible = readOnly
        cancelButton.isVisible = !readOnly
        doneButton.isVisible = !readOnly
        deleteDSVFormatButton.isVisible = !readOnly
        dsvFormatNameTextView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            marginEnd = if (readOnly) 16.dp else 4.dp
        }
        if (readOnly) {
            backButton.setOnClickListener {
                controller?.dispatch(BackButtonClicked)
            }
        } else {
            cancelButton.setOnClickListener {
                controller?.dispatch(CancelButtonClicked)
            }
            doneButton.setOnClickListener {
                controller?.dispatch(DoneButtonClicked)
            }
            dsvFormatNameTextView.setOnClickListener {
                controller?.dispatch(FormatNameButtonClicked)
            }
            deleteDSVFormatButton.setOnClickListener {
                controller?.dispatch(DeleteFormatButtonClicked)
            }
            //makeControllersEnabled()
        }
        makeControllersEnabled()
    }

    private fun makeControllersEnabled() {
        // Delimiter
        delimiterEditText.isEnabled = true
        delimiterEditText.observeText { newText: String ->
            val delimiter: Char? =
                if (newText.isEmpty()) null else newText.toRegularString().first()
            controller?.dispatch(DelimiterChanged(delimiter))
        }
        // Trailing delimiter
        yesTrailingDelimiterButton.setOnClickListener {
            controller?.dispatch(TrailingDelimiterChanged(true))
        }
        noTrailingDelimiterButton.setOnClickListener {
            controller?.dispatch(TrailingDelimiterChanged(false))
        }
        // Quote character
        quoteCharacterEditText.isEnabled = true
        quoteCharacterEditText.observeText { newText: String ->
            val quoteCharacter: Char? =
                if (newText.isEmpty()) null else newText.toRegularString().first()
            controller?.dispatch(QuoteCharacterChanged(quoteCharacter))
        }
        disabledQuoteCharacterButton.setOnClickListener {
            quoteCharacterEditText.text.clear()
        }
        // Quote mode
        allQuoteModeButton.setOnClickListener {
            controller?.dispatch(QuoteModeChanged(QuoteMode.ALL))
        }
        allNonNullQuoteModeButton.setOnClickListener {
            controller?.dispatch(QuoteModeChanged(QuoteMode.ALL_NON_NULL))
        }
        minimalQuoteModeButton.setOnClickListener {
            controller?.dispatch(QuoteModeChanged(QuoteMode.MINIMAL))
        }
        nonNumericQuoteModeButton.setOnClickListener {
            controller?.dispatch(QuoteModeChanged(QuoteMode.NON_NUMERIC))
        }
        noneQuoteModeButton.setOnClickListener {
            controller?.dispatch(QuoteModeChanged(QuoteMode.NONE))
        }
        // Escape character
        escapeCharacterEditText.isEnabled = true
        escapeCharacterEditText.observeText { newText: String ->
            val escapeCharacter: Char? =
                if (newText.isEmpty()) null else newText.toRegularString().first()
            controller?.dispatch(EscapeCharacterChanged(escapeCharacter))
        }
        disabledEscapeCharacterButton.setOnClickListener {
            escapeCharacterEditText.text.clear()
        }
        // Null string
        nullStringEditText.isEnabled = true
        nullStringEditText.observeText { newText: String ->
            val nullString: String? = if (newText.isEmpty()) null else newText.toRegularString()
            controller?.dispatch(NullStringChanged(nullString))
        }
        // Ignore surrounding spaces
        yesIgnoreSurroundingSpacesButton.setOnClickListener {
            controller?.dispatch(IgnoreSurroundingSpacesChanged(true))
        }
        noIgnoreSurroundingSpacesButton.setOnClickListener {
            controller?.dispatch(IgnoreSurroundingSpacesChanged(false))
        }
        // Trim
        yesTrimButton.setOnClickListener {
            controller?.dispatch(TrimChanged(true))
        }
        noTrimButton.setOnClickListener {
            controller?.dispatch(TrimChanged(false))
        }
        // Ignore empty lines
        yesIgnoreEmptyLinesButton.setOnClickListener {
            controller?.dispatch(IgnoreEmptyLinesChanged(true))
        }
        noIgnoreEmptyLinesButton.setOnClickListener {
            controller?.dispatch(IgnoreEmptyLinesChanged(false))
        }
        // Record separator
        recordSeparatorEditText.isEnabled = true
        recordSeparatorEditText.observeText { newText: String ->
            val recordSeparator: String? =
                if (newText.isEmpty()) null else newText.toRegularString()
            controller?.dispatch(RecordSeparatorChanged(recordSeparator))
        }
        // Comment character
        commentCharacterEditText.isEnabled = true
        commentCharacterEditText.observeText { newText: String ->
            val commentCharacter: Char? =
                if (newText.isEmpty()) null else newText.toRegularString().first()
            controller?.dispatch(CommentMarkerChanged(commentCharacter))
        }
        disabledCommentCharacterButton.setOnClickListener {
            commentCharacterEditText.text.clear()
        }
        // Skip header record
        yesSkipHeaderRecordButton.setOnClickListener {
            controller?.dispatch(SkipHeaderRecordChanged(true))
        }
        noSkipHeaderRecordButton.setOnClickListener {
            controller?.dispatch(SkipHeaderRecordChanged(false))
        }
        // Header column names
        firstHeaderColumnNameEditText.isEnabled = true
        firstHeaderColumnNameEditText.observeText { newText: String ->
            controller?.dispatch(HeaderColumnNameChanged(0, newText))
        }
        // Ignore header names case
        yesIgnoreHeaderNamesCaseButton.setOnClickListener {
            controller?.dispatch(IgnoreHeaderCaseChanged(true))
        }
        noIgnoreHeaderNamesCaseButton.setOnClickListener {
            controller?.dispatch(IgnoreHeaderCaseChanged(false))
        }
        // Allow duplicate header names
        yesAllowDuplicateHeaderNamesButton.setOnClickListener {
            controller?.dispatch(AllowDuplicateHeaderNamesChanged(true))
        }
        noAllowDuplicateHeaderNamesButton.setOnClickListener {
            controller?.dispatch(AllowDuplicateHeaderNamesChanged(false))
        }
        // Allow missing column names
        yesAllowMissingColumnNamesButton.setOnClickListener {
            controller?.dispatch(AllowMissingColumnNamesChanged(true))
        }
        noAllowMissingColumnNamesButton.setOnClickListener {
            controller?.dispatch(AllowMissingColumnNamesChanged(false))
        }
        // Header comments
        firstHeaderCommentEditText.isEnabled = true
        firstHeaderCommentEditText.observeText { newText: String ->
            controller?.dispatch(HeaderCommentChanged(0, newText))
        }
        // Auto-flush
        yesAutoFlushButton.setOnClickListener {
            controller?.dispatch(AutoFlushChanged(true))
        }
        noAutoFlushButton.setOnClickListener {
            controller?.dispatch(AutoFlushChanged(false))
        }
    }

    private fun String.toDisplayedString(): String {
        return replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun String.toRegularString(): String {
        return replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DsvFormatDiScope.close()
        }
    }

    companion object {
        const val APACHE_COMMONS_CSV_LIBRARY_URL =
            "https://commons.apache.org/proper/commons-csv/"
        const val CSV_FORMAT_URL =
            "https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html"
    }
}