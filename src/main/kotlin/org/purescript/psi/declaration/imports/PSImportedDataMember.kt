package org.purescript.psi.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSProperName

class PSImportedDataMember(node: ASTNode) : PSPsiElement(node) {
    val dataDeclarationImport = parentOfType<PSImportedData>()
    val importDeclaration get() = dataDeclarationImport?.importDeclaration
    val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
    override fun getReference() = ImportedDataMemberReference(this)
}
