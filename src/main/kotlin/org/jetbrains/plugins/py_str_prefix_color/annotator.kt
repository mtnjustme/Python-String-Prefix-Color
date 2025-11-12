package org.jetbrains.plugins.py_str_prefix_color

import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

import kotlin.collections.filter

val valid_str_prefixes =
    setOf(
        "u"
        , "r"
        , "b", "f", "t"
        , "br", "fr", "tr"
    )
    .let{iterable -> iterable + iterable.map{item -> item.reversed()}}  // rf
    .let{iterable -> iterable + iterable.map{item -> item.uppercase()}}  // F
    .let{iterable -> iterable + iterable.filter{item -> item.length == 2}.map{item -> item[0] + item[1].uppercase()}}  // fR
    .let{iterable -> iterable + iterable.filter{item -> item.length == 2}.map{item -> item[0].uppercase() + item[1]}} // Fr


class annotator : Annotator
{
    companion object
    {
        val str_prefix_attrs_key =
            TextAttributesKey.createTextAttributesKey(
                "py_str_prefix_color_str_prefix_attrs"
                , DefaultLanguageHighlighterColors.KEYWORD
            )
    }

    override fun annotate(element: PsiElement, annotation_holder: AnnotationHolder)
    {
        val element_type = element.node.elementType.toString()

        if (
            element_type != "Py:STRING_LITERAL_EXPRESSION"
            // && element_type != "Py:SINGLE_QUOTED_STRING"
            // && element_type != "Py:FSTRING_NODE"
            // && element_type != "Py:FSTRING_START"
        )
            return

        val colors_scheme = EditorColorsManager.getInstance().globalScheme
        val attrs = colors_scheme.getAttributes(str_prefix_attrs_key) ?: return

        val attrs_fg_color = attrs.foregroundColor
        val attrs_bg_color = attrs.backgroundColor
        val attrs_effect_color = attrs.effectColor

        val attrs_font_type = attrs.fontType

        if (
            attrs_fg_color == null
            && attrs_bg_color == null
            && attrs_effect_color == null
            && attrs_font_type == 0
        )
            return

        val element_text = element.text
        val element_text_length = element_text.length

        if (element_text_length < 3)  // !f""
            return

        val element_first_char = element_text[0]

        if (element_first_char == '"' || element_first_char == '\'')  // "a"
            return

        val element_last_char = element_text[element_text_length - 1]

        if (element_last_char != '"' && element_last_char != '\'')  // a"a
            return

        val element_first_quote_idx = element_text.indexOf(element_last_char)

        if (element_first_quote_idx == element_text_length - 1)  // aa"
            return

        val str_prefix_length = element_first_quote_idx

        if (str_prefix_length > 2)
            return

        val str_prefix = element_text.substring(0, str_prefix_length)

        if (!valid_str_prefixes.contains(str_prefix))
            return

        val element_text_range = element.textRange
        val element_text_range_start = element_text_range.startOffset

        val str_prefix_range_start = element_text_range_start
        val str_prefix_range_stop = str_prefix_range_start + str_prefix_length
        val str_prefix_range = TextRange(str_prefix_range_start, str_prefix_range_stop)

        val attrs_effect_type = attrs.effectType

        val annotation_builder = annotation_holder.newSilentAnnotation(HighlightSeverity.INFORMATION)

        annotation_builder.range(str_prefix_range)
        annotation_builder.enforcedTextAttributes(
            TextAttributes(  // making a copy, attrs.clone() can lead to errors
                attrs_fg_color
                , attrs_bg_color
                , attrs_effect_color
                , attrs_effect_type
                , attrs_font_type
            )
        )

        annotation_builder.create()

        // println(element_type)
        // println(element_text)
        // println(str_prefix)
    }
}