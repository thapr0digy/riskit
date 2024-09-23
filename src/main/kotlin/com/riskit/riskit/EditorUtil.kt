package com.riskit.riskit

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.SelectionModel

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

        val startLine = document.getLineNumber(startOffset) + 1
        val startColumn = startOffset - document.getLineStartOffset(startLine - 1) + 1
        val endLine = document.getLineNumber(endOffset) + 1
        val endColumn = endOffset - document.getLineStartOffset(endLine -1) + 1

        return TrimmedSelection(startLine, startColumn, endLine, endColumn)
    }
}