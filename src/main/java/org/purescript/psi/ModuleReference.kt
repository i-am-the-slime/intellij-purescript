package org.purescript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import org.purescript.file.ModuleNameIndex.Companion.fileContainingModule
import org.purescript.file.ModuleNameIndex.Companion.getAllModuleNames
import org.purescript.psi.imports.PSImportDeclarationImpl

class ModuleReference(element: PSImportDeclarationImpl) : PsiReferenceBase<PSImportDeclarationImpl>(
    element,
    element.importName?.textRangeInParent ?: TextRange.allOf(element.text.trim()),
    false
) {
    override fun getVariants(): Array<String> {
        return getAllModuleNames(element.project).toTypedArray()
    }

    override fun resolve(): PSModule? {
        val moduleName = element.importName?.name ?: return null
        return fileContainingModule(element.project, moduleName)?.module
    }
}
