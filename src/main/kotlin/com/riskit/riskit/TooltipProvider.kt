package com.riskit.riskit

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.Point

class TooltipProvider(private val project: Project) {

    private val riskInformation = RiskItUtil.loadRiskInformation(project)

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
                                    && it.startLine <= lineNumber && it.endLine >= lineNumber
                        }
                        if (matchingRiskInfo != null) {
                            val tooltipText = buildTooltipText(matchingRiskInfo)
                            showTooltip(editor, e.mouseEvent.point, tooltipText)
                        }
                    }
                }
            })
        }
    }

    private fun buildTooltipText(info: RiskItUtil.RiskInformation): String {
        return buildString {
            info.riskLevel?.let { append("Risk Level: $it\n") }
            info.maturityLevel?.let { append("Maturity Level: $it\n") }
            info.vulnerabilityClass?.let { append("Vulnerability Class: ${it.joinToString(", ")}\n") }
        }.trim()
    }

    private fun showTooltip(editor: com.intellij.openapi.editor.Editor, point: Point, text: String) {
        val balloon = JBPopupFactory.getInstance()
            .createBalloonBuilder(com.intellij.ui.components.JBLabel(text))
            .setFillColor(com.intellij.ui.JBColor.background())
            .createBalloon()

        val location = editor.contentComponent.locationOnScreen
        balloon.show(RelativePoint.fromScreen(Point(location.x + point.x, location.y + point.y)), Balloon.Position.below)
    }
}