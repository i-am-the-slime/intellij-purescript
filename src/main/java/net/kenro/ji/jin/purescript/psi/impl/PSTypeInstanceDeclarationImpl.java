package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSTypeInstanceDeclaration;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSTypeInstanceDeclarationImpl extends PSPsiElement implements PSTypeInstanceDeclaration {

    public PSTypeInstanceDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
