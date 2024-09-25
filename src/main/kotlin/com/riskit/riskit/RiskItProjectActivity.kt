package com.riskit.riskit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RiskItProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Perform the long-running operations off the EDT
        val tooltipProvider = withContext(Dispatchers.IO) {
            TooltipProvider(project).apply {
                registerTooltip()
                highlightLoadedRiskInformation()
            }
        }

        // Register the bulk file listener on the message bus
        ApplicationManager.getApplication().invokeLater {
            val connection: MessageBusConnection = project.messageBus.connect()
            connection.subscribe(VirtualFileManager.VFS_CHANGES, RiskItBulkFileListener(tooltipProvider))
        }
    }
}