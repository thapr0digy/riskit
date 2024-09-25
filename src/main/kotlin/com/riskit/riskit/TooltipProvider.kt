package com.riskit.riskit

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.util.concurrent.ConcurrentHashMap

class TooltipProvider(private val project: Project) {

    private var riskInformation = RiskItUtil.loadRiskInformation(project)
    private val gutterIcons = ConcurrentHashMap<Int, RangeHighlighter>()


    fun registerTooltip() {
        val editorFactory = EditorFactory.getInstance()
        for (editor in editorFactory.allEditors) {
            editor.addEditorMouseMotionListener(object : EditorMouseMotionListener {
                override fun mouseMoved(e: EditorMouseEvent) {
                    val editor = e.editor
                    if (e.area == EditorMouseEventArea.LINE_NUMBERS_AREA) {
                        val logicalPosition = editor.xyToLogicalPosition(e.mouseEvent.point)
                        val lineNumber = logicalPosition.line + 1
                        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
                        val matchingRiskInfo = riskInformation.find {
                            it.filename == virtualFile?.path?.substringAfter(project.basePath ?: "")
                                    && it.startLine == lineNumber
                        }
                        if (matchingRiskInfo != null) {
                            val tooltipText = buildTooltipText(matchingRiskInfo)
                            showTooltip(editor, e.mouseEvent.point, tooltipText)
                            highlightText(editor, matchingRiskInfo)
                        }
                    }
                }
            })
        }
    }

    fun refreshHighlights() {
        riskInformation = RiskItUtil.loadRiskInformation(project)
        highlightLoadedRiskInformation()
    }

    // Update clearHighlights to clear the map
    fun clearHighlights() {
        val editorFactory = EditorFactory.getInstance()
        for (editor in editorFactory.allEditors) {
            val markupModel = editor.markupModel
            markupModel.removeAllHighlighters()
        }
        gutterIcons.clear()
    }

    private fun addOrUpdateGutterIcon(editor: Editor, info: RiskItUtil.RiskInformation) {
        val startOffset = editor.document.getLineStartOffset(info.startLine)
        val endOffset = editor.document.getLineEndOffset(info.endLine)
        val tooltipText = buildTooltipText(info)
        val markupModel = editor.markupModel

        val existingHighlighter = gutterIcons[startOffset]
        if (existingHighlighter != null) {
            // Remove the existing highlighter
            markupModel.removeHighlighter(existingHighlighter)
        }

        // Add a new highlighter with the gutter icon
        val highlighter = markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            HighlighterLayer.ADDITIONAL_SYNTAX,
            null,
            HighlighterTargetArea.EXACT_RANGE
        )
        highlighter.gutterIconRenderer = RiskItGutterIconRenderer(tooltipText, startOffset, endOffset)
        gutterIcons[startOffset] = highlighter
    }

    // Update highlightLoadedRiskInformation to call addOrUpdateGutterIcon
    fun highlightLoadedRiskInformation() {
        val editorFactory = EditorFactory.getInstance()
        for (editor in editorFactory.allEditors) {
            val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
            val matchingRiskInfos = riskInformation.filter {
                it.filename == virtualFile?.path?.substringAfter(project.basePath ?: "")
            }
            for (info in matchingRiskInfos) {
                highlightText(editor, info)
                addOrUpdateGutterIcon(editor, info)
            }
        }
    }

    private fun buildTooltipText(info: RiskItUtil.RiskInformation): String {
        return buildString {
            info.riskLevel?.let { append("Risk Level: $it\n") }
            info.maturityLevel?.let { append("Maturity Level: $it\n") }
            info.vulnerabilityClass?.let { append("Vulnerability Class: ${it.joinToString(", ")}\n") }
        }.trim()
    }

    private fun showTooltip(editor: Editor, point: Point, text: String) {
        val balloon = JBPopupFactory.getInstance()
            .createBalloonBuilder(com.intellij.ui.components.JBLabel(text))
            .setFillColor(com.intellij.ui.JBColor.background())
            .createBalloon()

        val location = editor.contentComponent.locationOnScreen
        balloon.show(RelativePoint.fromScreen(Point(location.x + point.x, location.y + point.y)), Balloon.Position.below)
    }

    private fun highlightText(editor: Editor, info: RiskItUtil.RiskInformation) {
        val startOffset = editor.document.getLineStartOffset(info.startLine)
        val endOffset = editor.document.getLineEndOffset(info.endLine)
        EditorUtil.highlightText(editor, startOffset, endOffset)
    }
}