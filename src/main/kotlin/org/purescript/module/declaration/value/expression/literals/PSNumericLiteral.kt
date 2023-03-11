package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom

class PSNumericLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    override fun areSimilarTo(other: org.purescript.module.declaration.value.expression.Expression): Boolean = 
        other is PSNumericLiteral && other.text == this.text
}