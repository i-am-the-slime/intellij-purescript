package org.purescript.inference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getModule
import org.purescript.getValueDeclarationGroupByName

class InferenceIntegrationTest: BasePlatformTestCase() {
    fun `test everything`() {
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
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals(InferType.function(InferType.Id(0), InferType.Id(0)).toString(), "$f")
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals(InferType.function(InferType.Int, InferType.Int), int)
        val x = Main.getValueDeclarationGroupByName("x").inferType()
        TestCase.assertEquals(InferType.Int, x)
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
                | boolean = true
            """.trimMargin()
        )
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        val number = Main.getValueDeclarationGroupByName("number").inferType()
        val string = Main.getValueDeclarationGroupByName("string").inferType()
        val boolean = Main.getValueDeclarationGroupByName("boolean").inferType()
        TestCase.assertEquals("Int", int.toString())
        TestCase.assertEquals("Number", number.toString())
        TestCase.assertEquals("String", string.toString())
        TestCase.assertEquals("Boolean", boolean.toString())
    }
    fun `test records`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | record =
                |  { int: 42
                |  , number: 42.0
                |  , string: "Hello World"
                |  , boolean: true
                |  }
                | int = record.int
                | 
                | type User = { age:: Int, name :: String }
                | 
                | mkUser :: User -> Int
                | mkUser user = 42
                | 
                | checkUser u = mkUser u
                | 
            """.trimMargin()
        )
        val record = Main.getValueDeclarationGroupByName("record").inferType()
        TestCase.assertEquals(
            "{ int::Int, number::Number, string::String, boolean::Boolean }",
            record.toString()
        )
        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", int.toString())

        val checkUserType = Main.getValueDeclarationGroupByName("checkUser").inferType()
        TestCase.assertEquals("{ age::Int, name::String } -> Int", "$checkUserType")

    }
    fun `test signature`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. a -> a
                | f x = x
                | 
                | class Eq a where
                |   eq :: a -> a -> Boolean
                | 
                | type_class :: forall a. Eq a => a -> a
                | type_class x = x
                | 
                | int = type_class 1
            """.trimMargin()
        )
        Main.getModule().unify()
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals("a -> a", pprint(f.toString()))

        val type_class = Main.getValueDeclarationGroupByName("type_class").inferType()
        TestCase.assertEquals("Eq a => a -> a",  pprint(type_class.toString()))

        val int = Main.getValueDeclarationGroupByName("int").inferType()
        TestCase.assertEquals("Int", int.toString())
    }
    fun `xtest function types`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: Int -> Int
                | f x = x
                | 
                | g :: forall f a. Functor f => (a -> a) -> f a -> f a
                | g a f = f
                | 
                | h = g intToInt
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals("Int -> Int", pprint("$f"))
        val h = Main.getValueDeclarationGroupByName("h").inferType()
        TestCase.assertEquals("Array Int -> Array Int", pprint("$h"))
        
    }
    
    fun `test union`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | f :: forall a. Union (name :: String) (age :: Int) a => Record a -> Int
                | f x = 42
                | 
            """.trimMargin()
        )
        val f = Main.getValueDeclarationGroupByName("f").inferType()
        TestCase.assertEquals(
            "Row.Union (name::String) (age::Int) (name::String, age::Int) => { name::String, age::Int } -> Int",
            f.toString()
        )
    }
    
    fun `test type synonym`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | type Name = String
                | type Box a = a
                | 
                | name :: Name
                | name = "Haskell"
                | 
                | boxName :: Box String
                | boxName = "Box"
                | 
            """.trimMargin()
        )
        val name = Main.getValueDeclarationGroupByName("name").inferType()
        TestCase.assertEquals("String", "$name")
        val boxName = Main.getValueDeclarationGroupByName("boxName").inferType()
        TestCase.assertEquals("String", "$boxName")
    }
    fun `test new type`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | newtype Box a = Box a
                | 
                | boxString :: Box String
                | boxString = Box "Box" 
                | 
                | boxNoType = Box "Box"
                | 
            """.trimMargin()
        )
        val boxString = Main.getValueDeclarationGroupByName("boxString").inferType()
        TestCase.assertEquals("Box String", "$boxString")
        val boxNoType = Main.getValueDeclarationGroupByName("boxNoType").inferType()
        TestCase.assertEquals("Box String", "$boxNoType")
    }
    fun `test data type`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | data Box a = Full a | Empty
                | 
                | fullBox = Full "Box" 
                | 
                | emptyBox = Empty
                | 
            """.trimMargin()
        )
        val fullBox = Main.getValueDeclarationGroupByName("fullBox").inferType()
        TestCase.assertEquals("Box String", "$fullBox")
        val emptyBox = Main.getValueDeclarationGroupByName("emptyBox").inferType()
        TestCase.assertEquals("Box a", pprint("$emptyBox"))
    }
    fun `xtest classes`() {
        val Main = myFixture.configureByText(
            "Main.purs",
            """
                | module Main where
                | 
                | class Functor f where
                |   map :: forall a b. (a -> b) -> f a -> f b
                | 
                | intToInt :: Int -> Int
                | intToInt x = x
                | 
                | mapInt = map intToInt
                | 
            """.trimMargin()
        )
        val mapInt = Main.getValueDeclarationGroupByName("mapInt").inferType()
        TestCase.assertEquals("Functor f => f Int -> f Int", "$mapInt")
    }
    
    fun pprint(string : String): String {
        val letters = ('a'..'z').joinToString("")
        val id = Regex("u\\d+")
        val us: Map<String, CharSequence> = id.find(string)
            ?.groupValues
            ?.sorted()
            ?.distinct()
            ?.withIndex()
            ?.associate { it.value.toString() to letters[it.index].toString() }
            ?: return string
        return string.replace(id) { us[it.value]!!}
    }
}