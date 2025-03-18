package com.github.azimsh3r.ui

import com.github.azimsh3r.listener.VariableChangeListener
import com.github.azimsh3r.service.VariableTypeTrackerService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.impl.status.TextPanel
import javax.swing.JComponent
import javax.swing.SwingUtilities

class VariableTypeStatusBarWidgetFactory : StatusBarWidgetFactory {

    companion object {
        const val ID = "Variable Type"
    }

    override fun getId(): String = ID

    override fun getDisplayName(): String = ID

    override fun createWidget(project: Project): StatusBarWidget = VariableTypeStatusBarWidget(project)

    class VariableTypeStatusBarWidget(private val project: Project) : TextPanel(), CustomStatusBarWidget {

        private val variableTypeTrackerService = project.getService(VariableTypeTrackerService::class.java)

        init {
            subscribeToVariableChanges()
        }

        override fun ID(): String = ID

        override fun getComponent(): JComponent = this

        override fun install(statusBar: StatusBar) {
            text = "Loading..."
        }

        private fun subscribeToVariableChanges() {
            project.messageBus.connect().subscribe(variableTypeTrackerService.getTopic(), object : VariableChangeListener {
                override fun onVariableChanged(newType: String) {
                    SwingUtilities.invokeLater {
                        text = newType
                        revalidate()
                        repaint()
                    }
                }
            })
        }
    }
}
