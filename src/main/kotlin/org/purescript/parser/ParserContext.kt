@file:Suppress("unused")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

@JvmInline
value class ParserContext(private val builder: PsiBuilder) {
    fun eof() = builder.eof()
    fun advance() = builder.advanceLexer()
    fun text() = builder.tokenText ?: ""
    fun peek() = builder.tokenType ?: EOF
    fun eat(type: IElementType): Boolean {
        if (builder.tokenType === type) {
            advance()
            return true
        }
        return false
    }
    fun start(): PsiBuilder.Marker = builder.mark()
    val position: Int get() = builder.currentOffset
    fun getText(start: Int, end: Int) =
        builder.originalText.subSequence(start, end).toString()
}
