package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class SymbolicParsec(private val ref: Parsec, private val node: IElementType) :
    Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val startPosition = context.position
        val pack = context.start()
        var info = ref.parse(context)
        if (info.success) {
            pack.done(node)
        } else {
            pack.drop()
        }
        if (startPosition == info.position) {
            info = ParserInfo(info.position, this, info.success)
        }
        return info
    }
    public override fun calcName() = node.toString()
    override fun calcExpectedName() = setOf(node.toString())
    override val canStartWithSet: TokenSet
        get() = ref.canStartWithSet
    public override fun calcCanBeEmpty() = ref.canBeEmpty
}