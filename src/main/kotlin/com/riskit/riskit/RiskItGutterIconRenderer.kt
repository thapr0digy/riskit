package com.riskit.riskit

import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class RiskItGutterIconRenderer(
    private var tooltipText: String,
    private var startOffset: Int,
    private var endOffset: Int
) : GutterIconRenderer() {

    override fun getIcon(): Icon = IconLoader.getIcon("/icons/riskit-icon.svg", javaClass)

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
}