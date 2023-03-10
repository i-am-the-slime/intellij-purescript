package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

class Parameters(node: ASTNode) : PSPsiElement(node) {
    val namedDescendant = parameterBinders.flatMap { it.namedDescendant }
    val parameterBinders get() = parameters.flatMap { it.childrenBinders }
    val varBinderParameters get() = parameterBinders.filterIsInstance<VarBinder>()
    val parameters get() = childrenOfType<Parameter>()
}