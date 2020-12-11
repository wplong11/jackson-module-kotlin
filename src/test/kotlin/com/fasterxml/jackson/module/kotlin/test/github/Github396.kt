package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Test
import javax.xml.stream.XMLInputFactory
import kotlin.test.assertEquals

class Github396 {
    /**
     * Succeeds in Jackson 2.11.x, but fails in Jackson 2.12.0
     * See https://github.com/FasterXML/jackson-module-kotlin/issues/396
     */
    @Test
    fun testMissingConstructor() {
        val factory = XmlFactory(XMLInputFactory.newInstance())
        val mapper = XmlMapper(factory, JacksonXmlModule()).registerKotlinModule()

        val xml = "<product><stuff></stuff></product>"
        val product: Product = mapper.readValue(xml, Product::class.java)

        assertEquals(Product(null), product)
    }

    private data class Stuff(val str: String?)
    private data class Product(val stuff: Stuff?)
}
