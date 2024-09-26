package com.riskit.riskit

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon
import java.awt.Color

class RiskItGutterIconRenderer(
    private val tooltipText: String,
    private val startOffset: Int,
    private val endOffset: Int,
    private val editor: Editor
) : GutterIconRenderer() {

    private val icon: Icon = IconLoader.getIcon("/icons/riskit-icon.svg", javaClass)
    private var highlightHighlighter: RangeHighlighter? = null

    override fun getIcon(): Icon = icon

    override fun getTooltipText(): String = tooltipText

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RiskItGutterIconRenderer) return false
        return startOffset == other.startOffset && endOffset == other.endOffset && tooltipText == other.tooltipText
    }

    override fun hashCode(): Int {
        var result = tooltipText.hashCode()
        result = 31 * result + startOffset
        result = 31 * result + endOffset
        return result
    }

    fun highlightText() {
        val markupModel = editor.markupModel
        val textAttributes = EditorUtil.getHighlightTextAttributes()
        highlightHighlighter = markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            HighlighterLayer.SELECTION,
            textAttributes,
            HighlighterTargetArea.EXACT_RANGE
        )
    }

    fun clearHighlight() {
        highlightHighlighter?.let {
            editor.markupModel.removeHighlighter(it)
            highlightHighlighter = null
        }
    }
}