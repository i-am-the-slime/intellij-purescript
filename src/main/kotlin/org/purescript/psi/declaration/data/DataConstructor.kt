package org.purescript.psi.declaration.data

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedData
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.exports.ExportedData
import org.purescript.psi.exports.ExportedModule
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSProperName
import org.purescript.psi.type.PSType
import org.purescript.psi.type.PSTypeAtom
import javax.swing.Icon

/**
 * A data constructor in a data declaration, e.g.
 *
 * ```
 * CatQueue (List a) (List a)
 * ```
 * in
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class DataConstructor : PSStubbedElement<DataConstructor.Stub>, PsiNameIdentifierOwner, Importable , DocCommentOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<DataConstructor>(p, Type) {
        val module get() = dataDeclaration?.parentStub as? Module.Stub
        val dataDeclaration get() = parentStub.parentStub as? DataDeclaration.Stub
        val isExported
            get() = when {
                module == null -> false
                module?.exportList == null -> true
                module?.exportList?.childrenStubs
                    ?.filterIsInstance<ExportedModule.Stub>()
                    ?.find { it.name == module?.name } != null -> true

                else -> module!!.exportList!!.childrenStubs
                    .filterIsInstance<ExportedData.Stub>()
                    .any { exportedData ->
                        exportedData.name == name &&
                                exportedData.dataMembers.run { isEmpty() || any { it.name == name } }
                    }
            }
    }

    object Type : WithPsiAndStub<Stub, DataConstructor>("DataConstructor") {
        override fun createPsi(node: ASTNode) = DataConstructor(node)
        override fun createPsi(stub: Stub) = DataConstructor(stub, this)
        override fun createStub(psi: DataConstructor, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableIndex.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override fun asImport(): ImportDeclaration? {
        val items = ImportedData(
            dataDeclaration.name,
            dataMembers = setOf(name)
        )
        return module?.asImport()?.withItems(items)
    }

    internal val dataDeclaration: DataDeclaration.Psi get() = parentOfType()!!

    override val type: PSType? get() = null
    override val docComments: List<PsiComment> get() = getDocComments()

    // Todo clean this up
    override fun toString(): String = "PSDataConstructor($elementType)"

    /**
     * @return the [PSProperName] identifying this constructor
     */
    internal val identifier: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSTypeAtom] elements in this constructor
     */
    internal val typeAtoms: Array<PSTypeAtom> get() = findChildrenByClass(PSTypeAtom::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PSProperName = identifier
    override fun getName(): String = identifier.name
    override fun getIcon(flags: Int): Icon = AllIcons.Nodes.Class
}

