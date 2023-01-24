@file:Suppress("ComplexRedundantLet")

package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.LayoutDelimiter.Root
import org.purescript.lexer.token.SourcePos
import org.purescript.lexer.token.SourceToken
import org.purescript.parser.*


fun offsideP(tokPos: SourcePos, lytPos: SourcePos, lyt: LayoutDelimiter) =
    lyt.isIndent && tokPos.column < lytPos.column


fun lex(tokens: List<SuperToken>): List<SuperToken> {
    val sourcePos = SourcePos(0, 0, 0)
    var stack = LayoutStack(sourcePos, Root, null)
    val tokensOut = mutableListOf<SuperToken>()
    var startPos = sourcePos
    for (posToken in tokens) {
        val nextStart = posToken.end
        val (nextStack, toks) = stack.insertLayout(posToken, nextStart)
        tokensOut += toks.map { it.first }
        startPos = nextStart
        stack = nextStack
    }
    val layoutEnd = startPos.asEnd
    tokensOut += List(stack.count { it.isIndent }) { layoutEnd }
    return tokensOut
}

fun correctLineAndColumn(source: CharSequence) =
    { previous: SourceToken, current: SourceToken ->
        val start = previous.end
        val end = current.end
        // might be expensive
        val subSequence = source.subSequence(start.offset, end.offset)
        val newlineIndex = subSequence.lastIndexOf('\n')
        val tokenLength = end.offset - start.offset
        val newEnd = when (newlineIndex) {
            -1 -> SourcePos(start.line, start.column + tokenLength, end.offset)

            else -> SourcePos(
                start.line + subSequence.count { it == '\n' },
                tokenLength - newlineIndex - 1,
                end.offset
            )
        }
        SourceToken(current.value, start, newEnd)
    }

fun posFromOffset(offset: Int) = SourcePos(0, 0, offset)

fun getTokens(lexer: Lexer) = generateSequence {
    val sourceToken: SourceToken? = lexer.tokenType?.let { value ->
        SourceToken(
            value,
            posFromOffset(lexer.tokenStart),
            posFromOffset(lexer.tokenEnd)
        )
    }
    lexer.advance()
    sourceToken
}

class LayoutLexer(delegate: Lexer) : DelegateLexer(delegate) {

    private var tokens: List<SourceToken> = listOf()
    private var index = 0
    private val root =
        SourceToken(WHITE_SPACE, posFromOffset(0), posFromOffset(0))

    override fun start(buffer: CharSequence, start: Int, end: Int, state: Int) {
        require(start == 0) { "does not support incremental lexing: startOffset must be 0" }
        require(state == 0) { "does not support incremental lexing: initialState must be 0" }

        super.start(buffer, start, end, state)
        this.tokens = getTokens(delegate)
            .runningFold(root, correctLineAndColumn(buffer))
            .drop(1)
            .toList()
            .let { toSupers(it) }
            .let(::lex)
            .flatMap { it.tokens }
        index = 0
    }

    private val trailingTokens =
        TokenSet.create(WHITE_SPACE, MLCOMMENT, SLCOMMENT, DOC_COMMENT)

    private fun toLexemes(sourceTokens: List<SourceToken>): List<Lexeme> {
        if (sourceTokens.isEmpty()) return emptyList()
        var trailing = ArrayList<SourceToken>(4)
        val lexemes = ArrayList<Lexeme>(sourceTokens.size)
        var token: SourceToken = sourceTokens.first()
        for (t in sourceTokens.subList(1, sourceTokens.size)) when (t.value) {
            in trailingTokens -> trailing.add(t)
            else -> {
                lexemes.add(Lexeme(token, trailing))
                token = t
                trailing = ArrayList(4)
            }
        }
        lexemes.add(Lexeme(token, trailing))
        return lexemes
    }

    private fun toSupers(sourceTokens: List<SourceToken>): List<SuperToken> {
        val lexemes = toLexemes(sourceTokens)
        var qualified = mutableListOf<Lexeme>()
        var lexeme: Lexeme? = null
        val superTokens = mutableListOf<SuperToken>()
        for (l in lexemes) when {
            lexeme == null -> lexeme = l
            lexeme.value == PROPER_NAME &&
                l.value == DOT &&
                l.trailingWhitespace.isEmpty() &&
                lexeme.trailingWhitespace.isEmpty() -> {
                // <Proper Name><.>
                qualified.add(lexeme)
                qualified.add(l)
                lexeme = null
            }

            else -> {
                superTokens.add(SuperToken(qualified, lexeme))
                qualified = mutableListOf()
                lexeme = l
            }
        }
        if (lexeme != null) superTokens.add(SuperToken(qualified, lexeme))
        return superTokens
    }

    override fun advance() {
        index++
    }

    override fun getTokenType() = tokens.getOrNull(index)?.value
    override fun getTokenEnd() = tokens[index].end.offset
    override fun getTokenStart() = tokens[index].start.offset
}