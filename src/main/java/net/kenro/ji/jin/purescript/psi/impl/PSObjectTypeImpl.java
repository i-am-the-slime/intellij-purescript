package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSObjectType;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSObjectTypeImpl extends PSPsiElement implements PSObjectType {

    public PSObjectTypeImpl(final ASTNode node) {
        super(node);
    }

}
