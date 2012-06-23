TeXDoclet
=========

TeXDoclet is a Java doclet implementation that generates a LaTeX file from you Java code documentation.

The doclet is based on the doclet originally created by Greg Wonderly of
[C2 technologies Inc.](http://www.c2-tech.com>) and its revision by Soeren Caspersen of
[XO Software](http://www.xosoftware.dk). The project of Greg Wonderly is available here: 
<http://java.net/projects/texdoclet>.

See <http://doclet.github.com> for more information.

Build
-----

    mvn clean install

Run
---

You have to specify that *javadoc* has to use another doclet than the default doclet by giving the following parameters to the *javadoc* call :

	javadoc -docletpath <path to>TeXDoclet.jar \
		-doclet org.stfm.texdoclet.TeXDoclet \
		<TeXDoclet parameters>

Example :

	javadoc -docletpath target/TeXDoclet.jar \
		-doclet org.stfm.texdoclet.TeXDoclet \
		-noindex \
		-tree \
		-hyperref \
		-texinit texdoclet_include/preamble.tex \
		-imagespath ".." \
		-output texdoclet_output/TeXDoclet.tex \
		-title "TeXDoclet Java Documentation" \
		-author "Greg Wonderly \and S\"oren Caspersen \and Stefan Marx" \
		-sourcepath src/main/java \
		-subpackages org

See `createDocs.sh` scripts in `/examples` subdirectory for more examples.

Print help :

	javadoc -docletpath target/TeXDoclet.jar -doclet org.stfm.texdoclet.TeXDoclet

TeXDoclet usage :

	-title <title>            A title to use for the generated output document.
	-subtitle <title>         A subtitle for the output document. No -title will result in no title page.
	-output <outfile>         Specifies the output file to write to. If none specified, the default is docs.tex in the current directory.
	-docclass <class>         LaTeX2e document class, `report' is the default.
	-doctype <type>           LaTeX2e document style, `headings' is the default.
	-classfilter <name>       The name of a class implementing the ClassFilter interface.
	-date <date string>       The value to use for the document date.
	-author <author>          Specifies string to use for document Author.
	-texinit <file>           LaTeX2e statements included before \begin{document}.
	-texsetup <file>          LaTeX2e statements included after \begin{document} \maketitle (if title was specified).
	-texintro <file>          LaTeX2e statements included after table of contents
	-texfinish <file>         LaTeX2e statements included before \end{document}.
	-texpackage <file>        LaTeX2e statements included before packages' \chapter.
	-setup <file>             A setup file included before \begin{document}.
	-twosided                 Print twosided.
	-serial                   Do print Serializable information.
	-nosummaries              Do print summaries of fiels, constructors and methods.
	-nofieldsummary           Do not print field summaries
	-noconstructorsummary     Do not print constructor summaries
	-noinherited              Do not include inherited API information in output.
	-shortinherited           Prints a short inheritance, only the member name (not the whole signature)
	-noindex                  Do not create index.
	-tree                     Create a class tree.
	-treeindent <float>       Indent <float>cm i the class tree. Default is 1cm.
	-hyperref                 Use the hyperref package.
	-pdfhyperref              Use the hyperref package with pdf. Overrides -hypertex 
	-version                  Includes version-tags 
	-hr                       Prints horizontal rows in the output (to get a better? view)
	-include                  Creates output without latex initiation (writes it in initdocsinclude.tex), titlepage, contents 
	-sectionlevel <level>     Specifies the highest level of sections (either "subsection", "section" or "chapter")
	-imagespath               Path to the texdoclet_images dir (absolute or relative to the output document .tex file).



