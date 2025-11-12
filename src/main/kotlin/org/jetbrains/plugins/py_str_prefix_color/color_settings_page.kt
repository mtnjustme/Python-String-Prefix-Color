package org.jetbrains.plugins.py_str_prefix_color

import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

import javax.swing.Icon

val category_name = "User Defined"
val category_entry_name = "Python: String Prefix"

fun tag_text(tag: String, text: String): String
{
    return "<$tag>$text</$tag>"
}

class color_settings_page : ColorSettingsPage
{
    private companion object
    {
        val str_attrs_key =
            TextAttributesKey.createTextAttributesKey(
                "py_str_prefix_color_str_attrs"
                , DefaultLanguageHighlighterColors.STRING
            )
    }

    override fun getDisplayName() = category_name
    override fun getIcon(): Icon? = null

    override fun getHighlighter() =
        SyntaxHighlighterFactory.getSyntaxHighlighter(PlainTextFileType.INSTANCE, null, null)
        ?: PlainSyntaxHighlighter()

    override fun getAttributeDescriptors() =
        arrayOf(
            AttributesDescriptor(
                category_entry_name
                , annotator.str_prefix_attrs_key
            )
        )

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
    override fun getDemoText() = (
        tag_text("str_prefix", "f") + tag_text("str", "\"text1\"")
        + ' ' + tag_text("str_prefix", "r") + tag_text("str", "'text2'")
        + ' ' + tag_text("str_prefix", "rb") + tag_text("str", "\"text3\"")
    )

    override fun getAdditionalHighlightingTagToDescriptorMap() =
        mutableMapOf(
            "str" to str_attrs_key
            , "str_prefix" to annotator.str_prefix_attrs_key
        )
}