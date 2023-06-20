package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getValueDeclarationGroupByName

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
        val xScope = Scope(mutableMapOf(), mutableMapOf())
        val fScope = Scope(mutableMapOf(), mutableMapOf())
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f a = a
                | 
                | x = f 1
                | 
                | int :: Int -> Int
                | int x = x
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f")
        val x = Main.getValueDeclarationGroupByName("x")
        val int = Main.getValueDeclarationGroupByName("int")
        TestCase.assertEquals(
            Type.function(fScope.lookup("a"), fScope.lookup("a")),
            f.infer(fScope)
        )
        TestCase.assertEquals(
            Type.function(Type.Int, Type.Int),
            int.infer(Scope(mutableMapOf(), mutableMapOf()))
        )
        val xValue = x.valueDeclarations.single().value!!
        TestCase.assertEquals(Type.Int, xValue.infer(xScope))
    }
    fun `test primitives`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | int = 42
                | number = 42.0
                | string = "Hello World"
                | boolean = True
                | record =
                |  { int: 42
                |  , number: 42.0
                |  , string: "Hello World"
                |  , boolean: True
                |  }
            """.trimMargin()
        )
        val int = Main.getValueDeclarationGroupByName("int").infer(Scope.new())
        val number = Main.getValueDeclarationGroupByName("number").infer(Scope.new())
        val string = Main.getValueDeclarationGroupByName("string").infer(Scope.new())
        val boolean = Main.getValueDeclarationGroupByName("boolean").infer(Scope.new())
        val record = Main.getValueDeclarationGroupByName("record").infer(Scope.new())
        
        TestCase.assertEquals("Int", int.toString())
        TestCase.assertEquals("Number", number.toString())
        TestCase.assertEquals("String", string.toString())
        TestCase.assertEquals("Boolean", boolean.toString())
        TestCase.assertEquals(
            "{ int::Int, number::Number, string::String, boolean::Boolean }",
            record.toString()
        )
    }
}