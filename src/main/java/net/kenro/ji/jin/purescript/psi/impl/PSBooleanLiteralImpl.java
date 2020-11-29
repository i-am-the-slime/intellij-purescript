package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSBooleanLiteral;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSBooleanLiteralImpl extends PSPsiElement implements PSBooleanLiteral {

    public PSBooleanLiteralImpl(final ASTNode node) {
        super(node);
    }

}
