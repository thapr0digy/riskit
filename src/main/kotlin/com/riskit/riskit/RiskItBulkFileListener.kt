package com.riskit.riskit

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent

class RiskItBulkFileListener(private val tooltipProvider: TooltipProvider) : BulkFileListener {
    override fun after(events: List<VFileEvent>) {
        val relevantEvents = events.filter { it.file?.name == "riskit.output.json" }
        println(relevantEvents)

        relevantEvents.forEach { event ->
            when (event) {
                is VFileContentChangeEvent, is VFileCreateEvent -> {
                    // Get the specific document for riskit.output.json
                    val virtualFile: VirtualFile? = event.file
                    if (virtualFile != null) {
                        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
                        if (document != null) {
                            // Save the specific document
                            FileDocumentManager.getInstance().saveDocument(document)
                            FileDocumentManager.getInstance().reloadFiles(virtualFile)
                            FileDocumentManager.getInstance().reloadFromDisk(document)
                            tooltipProvider.refreshHighlights()
                        }
                    }
                }
                is VFileDeleteEvent -> tooltipProvider.clearHighlights()
            }
        }
    }
}