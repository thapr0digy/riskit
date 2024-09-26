package com.riskit.riskit

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor

object EditorUtil {
    data class TrimmedSelection(val startLine: Int, val startColumn: Int, val endLine: Int, val endColumn: Int)

    fun getTrimmedSelection(document: Document, selectionModel: SelectionModel): TrimmedSelection {
        var startOffset = selectionModel.selectionStart
        var endOffset = selectionModel.selectionEnd

        // Trim leading whitespace
        while (startOffset < endOffset && document.charsSequence[startOffset].isWhitespace()) {
            startOffset++
        }

        // Trim trailing whitespace
        while (endOffset > startOffset && document.charsSequence[endOffset - 1].isWhitespace()) {
            endOffset--
        }

        val startLine = document.getLineNumber(startOffset)
        val startColumn = startOffset - document.getLineStartOffset(startLine)
        val endLine = document.getLineNumber(endOffset)
        val endColumn = endOffset - document.getLineStartOffset(endLine)

        return TrimmedSelection(startLine, startColumn, endLine, endColumn)
    }

    fun highlightText(editor: Editor, startOffset: Int, endOffset: Int) {
        val markupModel = editor.markupModel
        val textAttributes = TextAttributes()
        textAttributes.backgroundColor = JBColor.GREEN.darker()
        markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            HighlighterLayer.SELECTION,
            textAttributes,
            HighlighterTargetArea.EXACT_RANGE
        )
    }

    fun getHighlightTextAttributes(): TextAttributes {
        val textAttributes = TextAttributes()
        textAttributes.backgroundColor = JBColor.YELLOW
        return textAttributes
    }

}