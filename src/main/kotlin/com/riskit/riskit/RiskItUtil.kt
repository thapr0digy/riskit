package com.riskit.riskit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.FileReader

object RiskItUtil {
    data class RiskInformation(
        val filename: String,
        val startLine: Int,
        val startColumn: Int,
        val endLine: Int,
        val endColumn: Int,
        val riskLevel: String?,
        val maturityLevel: String?,
        val vulnerabilityClass: List<String>?
    )

    private const val OUTPUT_FILE = "riskit.output.json"

    fun saveRiskInformation(project: Project, info: RiskInformation) {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val projectBaseDir = project.basePath ?: ""
        val outputFile = File(projectBaseDir, OUTPUT_FILE)
        val jsonArray: JsonArray = if (outputFile.exists()) {
            try {
                gson.fromJson(outputFile.readText(), JsonArray::class.java) ?: JsonArray()
            } catch (e: IOException) {
                JsonArray()
            }
        } else {
            JsonArray()
        }

        // Check if the filename already exists in the JSON array
        var existingFileObject: JsonObject? = null
        for (jsonElement in jsonArray) {
            val jsonObject = jsonElement.asJsonObject
            if (jsonObject.get("filename").asString == info.filename) {
                existingFileObject = jsonObject
                break
            }
        }

        if (existingFileObject == null) {
            // If the filename does not exist, create a new JsonObject for the file
            existingFileObject = JsonObject()
            existingFileObject.addProperty("filename", info.filename)
            existingFileObject.add("details", JsonArray())
            jsonArray.add(existingFileObject)
        }

        // Get the details array from the existing file object
        val detailsArray = existingFileObject.getAsJsonArray("details")

        // Check if an entry with the same startLine and endLine exists
        var existingDetailsObject: JsonObject? = null
        for (jsonElement in detailsArray) {
            val jsonObject = jsonElement.asJsonObject
            val startLine = jsonObject.get("startLine").asString.split(":")[0].toInt()
            val endLine = jsonObject.get("endLine").asString.split(":")[0].toInt()
            if (startLine == info.startLine && endLine == info.endLine) {
                existingDetailsObject = jsonObject
                break
            }
        }

        if (existingDetailsObject == null) {
            // If no matching entry exists, create a new details object
            existingDetailsObject = JsonObject()
            existingDetailsObject.addProperty("startLine", "${info.startLine}:${info.startColumn}")
            existingDetailsObject.addProperty("endLine", "${info.endLine}:${info.endColumn}")
            detailsArray.add(existingDetailsObject)
        }

        // Update the existing details object
        info.riskLevel?.let { existingDetailsObject.addProperty("riskLevel", it) }
        info.maturityLevel?.let { existingDetailsObject.addProperty("maturityLevel", it) }
        info.vulnerabilityClass?.let {
            val existingVulnerabilityClassArray = existingDetailsObject.getAsJsonArray("vulnerabilityClass") ?: JsonArray()
            val newVulnerabilityClasses = info.vulnerabilityClass.toSet()
            val currentVulnerabilityClasses = existingVulnerabilityClassArray.map { it.asString }.toSet()
            val updatedVulnerabilityClasses = currentVulnerabilityClasses + newVulnerabilityClasses
            val updatedVulnerabilityClassArray = JsonArray()
            updatedVulnerabilityClasses.forEach { updatedVulnerabilityClassArray.add(it) }
            existingDetailsObject.add("vulnerabilityClass", updatedVulnerabilityClassArray)
        }

        // Write the updated JSON array to the output file
        FileWriter(outputFile).use { writer ->
            gson.toJson(jsonArray, writer)
        }

        // Refresh the virtual file system to ensure changes are recognized
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputFile)
        virtualFile?.let {
            VirtualFileManager.getInstance().syncRefresh()
        }

    }
    fun loadRiskInformation(project: Project): List<RiskInformation> {
        val projectBaseDir = project.basePath ?: throw IllegalStateException("Project base path is null")
        val outputFile = File(projectBaseDir, OUTPUT_FILE)
        if (!outputFile.exists()) {
            return emptyList()
        }

        return try {
            val gson = Gson()
            val jsonArray = gson.fromJson(FileReader(outputFile), JsonArray::class.java)
            jsonArray.map { jsonElement: JsonElement ->
                val jsonObject = jsonElement.asJsonObject
                val filename = jsonObject.get("filename").asString
                val details = jsonObject.getAsJsonArray("details").map { detailElement ->
                    val detailObject = detailElement.asJsonObject
                    RiskInformation(
                        filename,
                        detailObject.get("startLine").asString.split(":")[0].toInt(),
                        detailObject.get("startLine").asString.split(":")[1].toInt(),
                        detailObject.get("endLine").asString.split(":")[0].toInt(),
                        detailObject.get("endLine").asString.split(":")[1].toInt(),
                        detailObject.get("riskLevel")?.asString,
                        detailObject.get("maturityLevel")?.asString,
                        detailObject.getAsJsonArray("vulnerabilityClass")?.map { it.asString }
                    )
                }
                details
            }.flatten()
        } catch (e: IOException) {
            emptyList()
        }
    }
}