package com.riskit.riskit

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.actionSystem.CommonDataKeys

class MaturityLevelAction() : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val maturityLevel: String = e.presentation.text
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.let {
            val selectionModel = it.selectionModel
            if (selectionModel.hasSelection()) {
                val trimmedSelection = EditorUtil.getTrimmedSelection(it.document, selectionModel)

                val project: Project? = e.project
                project?.let {
                    val file: VirtualFile? = FileDocumentManager.getInstance().getFile(editor.document)
                    file?.let {
                        val relativePath = project.basePath?.let { it1 -> file.path.substring(it1.length) } ?: file.path
                        RiskItUtil.saveRiskInformation(
                            project,
                            RiskItUtil.RiskInformation(
                                relativePath,
                                trimmedSelection.startLine,
                                trimmedSelection.startColumn,
                                trimmedSelection.endLine,
                                trimmedSelection.endColumn,
                                null,
                                maturityLevel,
                                null
                            )
                        )
                    }
                }
            }
        }
    }
}