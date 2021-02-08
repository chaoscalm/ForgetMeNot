package com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser
import com.odnovolov.forgetmenot.presentation.common.DarkPopupWindow
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.show
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetAdapter
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetItem
import com.odnovolov.forgetmenot.presentation.screen.fileimport.editor.myColorScheme
import com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext.ImportedTextEditorEvent.EncodingIsChanged
import kotlinx.android.synthetic.main.fragment_imported_text_editor.*
import kotlinx.android.synthetic.main.popup_charsets.view.*
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class ImportedTextEditorFragment : BaseFragment() {
    init {
        ImportedTextEditorDiScope.reopenIfClosed()
    }

    private var controller: ImportedTextEditorController? = null
    private lateinit var viewModel: ImportedTextEditorViewModel
    private var charsetPopup: PopupWindow? = null
    private var charsetAdapter: CharsetAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_imported_text_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ImportedTextEditorDiScope.getAsync() ?: return@launch
            editor.language = diScope.syntaxHighlighting
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        editor.colorScheme = myColorScheme
        charsetButton.setOnClickListener {
            showCharsetPopup()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            sourceTextWithNewEncoding.observe(editor::setTextContent)
            errorLines.observe { errorLines: List<Int> ->
                errorLines.forEach { errorLine: Int ->
                    editor.setErrorLine(errorLine + 1)
                }
            }
            currentCharset.observe { charset: Charset ->
                charsetButton.text = charset.name()
            }
        }
    }

    private fun showCharsetPopup() {
        requireCharsetPopup().show(anchor = charsetButton, gravity = Gravity.BOTTOM)
        charsetButton.requestLayout()
    }

    private fun requireCharsetPopup(): PopupWindow {
        if (charsetPopup == null) {
            val content: View = View.inflate(requireContext(), R.layout.popup_charsets, null)
            val onItemClicked: (Charset) -> Unit = { charset: Charset ->
                charsetPopup?.dismiss()
                controller?.dispatch(EncodingIsChanged(charset))
            }
            charsetAdapter = CharsetAdapter(onItemClicked)
            content.charsetRecycler.adapter = charsetAdapter
            charsetPopup = DarkPopupWindow(content)
            subscribeCharsetPopupToViewModel()
        }
        return charsetPopup!!
    }

    private fun subscribeCharsetPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = ImportedTextEditorDiScope.getAsync() ?: return@launch
            diScope.viewModel.availableCharsets.observe { availableCharsets: List<CharsetItem> ->
                charsetAdapter!!.items = availableCharsets
            }
        }
    }

    override fun onPause() {
        super.onPause()
        editor.hideSoftInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        charsetPopup?.dismiss()
        charsetPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            ImportedTextEditorDiScope.close()
        }
    }
}