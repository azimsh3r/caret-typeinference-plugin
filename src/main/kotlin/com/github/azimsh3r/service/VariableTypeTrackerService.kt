package com.github.azimsh3r.service

import com.github.azimsh3r.listener.VariableChangeListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.messages.Topic
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.TypeEvalContext

@Service(Service.Level.PROJECT)
class VariableTypeTrackerService(private val project: Project) {
    private var topic = Topic.create("variable_change_update", VariableChangeListener::class.java)

    fun getTopic() = topic

    fun listenActions() {
        val actionListener = object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val project = editor.project ?: return
                val caretOffset = event.caret?.offset ?: return

                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

                val elementAtCaret = psiFile.findElementAt(caretOffset) ?: return

                val assignment = PsiTreeUtil.getParentOfType(elementAtCaret, PyAssignmentStatement::class.java)
                val variable = assignment?.targets?.firstOrNull() as? PyTargetExpression

                val newType = variable?.let {
                    val typeEvalContext = TypeEvalContext.codeAnalysis(project, psiFile)
                    typeEvalContext.getType(it)?.name ?: "unknown"
                } ?: "n/a"

                ApplicationManager.getApplication().invokeLater {
                    publishVariableChange(newType)
                }
            }
        }

        EditorFactory.getInstance().eventMulticaster.addCaretListener(
            actionListener
        ) {
            EditorFactory.getInstance().eventMulticaster.removeCaretListener(actionListener)
        }
    }

    private fun publishVariableChange(newType: String) {
        ApplicationManager.getApplication().messageBus.syncPublisher(topic).onVariableChanged(newType)
    }
}
