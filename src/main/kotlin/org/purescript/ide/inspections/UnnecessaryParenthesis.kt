package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import org.purescript.module.declaration.value.parameters.Parameter
import org.purescript.module.declaration.value.binder.ParensBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelExprBinder
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.PSParens
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.expression.controll.caseof.CaseAlternative

class UnnecessaryParenthesis : LocalInspectionTool() {
    private val valueWithOneChild = psiElement(PSValue::class.java)
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val parentIsArgument = psiElement().withParent(Argument::class.java)
    private val parenthesis: Capture<PSParens> = psiElement(PSParens::class.java)
    private val hasOnlyOneChild = psiElement()
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val pattern = parenthesis
        .andOr(
            psiElement()
                .withChild(valueWithOneChild)
                .andNot(parentIsArgument),
            psiElement().withSuperParent(2, valueWithOneChild)
        )


    private val caseAlternative = psiElement(CaseAlternative::class.java)
    private val recordLabelExprBinder = psiElement(RecordLabelExprBinder::class.java)

    private val hasOnlyTwoChildren = psiElement()
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(2))

    private val binder = or(
        psiElement().withParent(hasOnlyOneChild.andNot(psiElement(Parameter::class.java))),
        psiElement().withParent(caseAlternative.and(hasOnlyTwoChildren)),
        psiElement().withParent(recordLabelExprBinder.and(hasOnlyOneChild)),
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        visitElement {
            when (this) {
                is PSParens ->
                    if (pattern.accepts(this)) {
                        holder.registerProblem(
                            this,
                            "Unnecessary parentheses",
                            RemoveParenthesis(this),
                        )
                    }

                is ParensBinder -> if (binder.accepts(this)) {
                    holder.registerProblem(
                        this,
                        "Unnecessary parentheses",
                        RemoveParenthesisBinder(this),
                    )
                }
            }

        }
}
