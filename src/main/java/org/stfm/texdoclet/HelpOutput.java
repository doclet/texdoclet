package org.stfm.texdoclet;

public class HelpOutput {

	protected static void printHelp() {
		try {
			throw new Exception("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("TeXDoclet Usage:");
		System.err
				.println("-title <title>            A title to use for the generated output document.");
		System.err
				.println("-subtitle <title>         A subtitle for the output document. No -title will result in no title page.");
		System.err
				.println("-output <outfile>         Specifies the output file to write to. Default is docs.tex in the current directory.");
		System.err
				.println("-docclass <class>         LaTeX2e document class, `report' is the default.");
		System.err
				.println("-doctype <type>           LaTeX2e document style, `headings' is the default.");
		System.err
				.println("-classfilter <name>       The name of a class implementing the ClassFilter interface.");
		System.err
				.println("-date <date string>       The value to use for the document date.");
		System.err
				.println("-author <author>          Specifies string to use for document Author.");
		System.err
				.println("-texinit <file>           LaTeX2e statements included before \\begin{document}.");
		System.err
				.println("-texsetup <file>          LaTeX2e statements included after \\begin{document} \\maketitle (if title was specified).");
		System.err
				.println("-texintro <file>          LaTeX2e statements included after table of contents");
		System.err
				.println("-texfinish <file>         LaTeX2e statements included before \\end{document}.");
		System.err
				.println("-texpackage <file>        LaTeX2e statements included before packages' \\chapter.");
		System.err
				.println("-setup <file>             A setup file included before \\begin{document}.");
		System.err.println("-twosided                 Print twosided.");
		System.err
				.println("-serial                   Do print Serializable information.");
		System.err
				.println("-nosummaries              Do print summaries of fiels, constructors and methods.");
		System.err
				.println("-nofieldsummary           Do not print field summaries");
		System.err
				.println("-noconstructorsummary     Do not print constructor summaries");
		System.err
				.println("-noinherited              Do not include inherited API information in output.");
		System.err
				.println("-shortinherited           Prints a short inheritance, only the member name (not the whole signature)");
		System.err.println("-noindex                  Do not create index.");
		System.err.println("-tree                     Create a class tree.");
		System.err
				.println("-treeindent <float>       Indent <float>cm i the class tree. Default is 1cm.");
		System.err
				.println("-hyperref                 Use the hyperref package.");
		System.err
				.println("-pdfhyperref              Use the hyperref package with pdf. Overrides -hypertex.");
		System.err.println("-version                  Includes version-tags.");
		System.err
				.println("-hr                       Prints horizontal rows in the output (to get a better? view).");
		System.err
				.println("-nopackagetoc             Do not include a table of content for each package");
		System.err
				.println("-include                  Creates output in two separated latex documents: one for the preamble part and another for the actual java documentation content.");
		System.err
				.println("-sectionlevel <level>     Specifies the highest level of sections (either \"subsection\", \"section\" or \"chapter\").");
		System.err
				.println("-imagespath <path>        Path to 'texdoclet_images' dir that is created by TeXDoclet to hold referenced images (absolute or relative to the output document .tex file).");
		System.err
				.println("-tablescale <factor>      Scale factor to specify width of tables. Default value is 0.9.");
		System.err
				.println("-createpdf                Creates .pdf file from the .tex output file by using pdflatex tool.");
	}

}
