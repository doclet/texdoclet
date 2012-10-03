package org.stfm.texdoclet;

/**
 * This class is just for testing the Mardown processing output.
 * 
 * <PRE format="markdown">
 * ### a) Some text
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * some text some text some text some text some text some text some 
 * text some text some text some text some text some text some text 
 * some text some text some text with 2 ending spaces  
 * 
 * text some text some text some text some text some text some text 
 * some text some text some text some text some text some text some text
 * 
 * text some text some text some text some text some text some text 
 * some text some text
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * some text some text some text some text some text some text some 
 * text some text some text some text some text some text some text 
 * some text some text some text with 2 ending spaces  
 * 
 * text some text some text some text some text some text some text 
 * some text some text some text some text some text some text some text
 * 
 * text some text some text some text some text some text some text 
 * some text some text
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### b) Lists
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * unsorted :
 * 
 * - item1 
 *     - item11 
 *     - item12 
 * - item2
 * 
 * or :
 * 
 * + item1 
 *     + item12
 *         + item13
 *    
 * sorted :
 * 
 * 1. item1 
 *     1. item11 
 *     2. item12 
 * 2. item2
 *     - item21
 *     - item22
 * 3. item3
 *   
 * lists with paragraphs :
 * 
 * 1. some text some text some text some text some text some text some text some
 * text some text some text
 * 
 *     some text some text some text some text some text some text
 * 
 * 2. some text some text some text some text some text some text
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="md">
 * unsorted :
 * 
 * - item1 
 *     - item11 
 *     - item12 
 * - item2
 * 
 * or :
 * 
 * + item1 
 *     + item12
 *         + item13
 * 
 * sorted :
 * 
 * 1. item1 
 *     1. item11 
 *     2. item12 
 * 2. item2
 *     - item21
 *     - item22
 * 3. item3
 *   
 * lists with paragraphs :
 * 
 * 1. some text some text some text some text some text some text some text some
 * text some text some text
 * 
 *     some text some text some text some text some text some text
 * 
 * 2. some text some text some text some text some text some text
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### c) Blockquotes
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * some text some text some text some text some text some text
 * 
 * > some quoting text
 * > 
 * > > some nested quoting text 
 * >
 * > some quoting text 
 * > 
 * > ###### header in blockquote
 * >
 * > a list in blockquote : 
 * > 
 * > 1. item1
 * > 2. item2
 * >     1. item21
 * >     2. item22
 * > 3. item3
 * >
 * > some quoting text
 * >
 * >    code in blockquote
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * some text some text some text some text some text some text
 * 
 * > some quoting text
 * > 
 * > > some nested quoting text 
 * >
 * > some quoting text 
 * > 
 * > ###### header in blockquote
 * >
 * > a list in blockquote : 
 * > 
 * > 1. item1
 * > 2. item2
 * >     1. item21
 * >     2. item22
 * > 3. item3
 * >
 * > some quoting text
 * >
 * >    code in blockquote
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### d) Preformatted text
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * some preformatted :
 * 
 *     code line 1 
 *     code line 2
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * some preformatted :
 * 
 *     code line 1 
 *     code line 2
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### e) Horizontal rules
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * ***
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * ***
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### f) Emphasis
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * *single asterisks* (em)
 * 
 * _single underscores_ (em)
 * 
 * **double asterisks** (strong)
 * 
 * __double underscores__ (strong)
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * *single asterisks* (em)
 * 
 * _single underscores_ (em)
 * 
 * **double asterisks** (strong)
 * 
 * __double underscores__ (strong)
 * 
 * </PRE>
 * 
 * <PRE format="markdown">
 * ### h) Code
 * </PRE>
 * 
 * <b>Markdown code :</b>
 * 
 * <PRE>
 * 
 * some code : `TeXDoclet extends Doclet` and ``There is a literal backtick (`)
 * here.``
 * 
 * </PRE>
 * 
 * <b>results in :</b>
 * 
 * <PRE format="markdown">
 * 
 * some code : `TeXDoclet extends Doclet` and ``There is a literal backtick (`)
 * here.``
 * 
 * </PRE>
 * 
 * @author Stefan Marx
 * 
 */
public class MarkdownTest {

}
