package com.github.azimsh3r

import com.github.azimsh3r.service.VariableTypeTrackerService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PluginStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val variableTypeTrackerService = project.getService(VariableTypeTrackerService::class.java)
        variableTypeTrackerService.listenActions()
    }
}
