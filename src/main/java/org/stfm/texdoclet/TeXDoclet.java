package org.stfm.texdoclet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;

/**
 * This class provides a Java <code>javadoc</code> Doclet which generates a <TEX
 * txt="\LaTeXe{}">LaTeX2e</TEX> document out of the java classes that it is
 * used on. This is convenient for creating printable documentation complete
 * with cross reference information. <H3>Supported HTML tags</H3>
 * <dl>
 * <dt>&lt;a&gt;
 * <dd>including an additional attribut "doprinturl". Since the output of the
 * doclet should be printable, the href attribut of <A> tags is printed in
 * parentheses following the link if attribut "doprinturl" is set. Sometimes
 * this is undesirable, and omitting "doprinturl" attribut will prevent this.
 * <dt>&lt;dl&gt;
 * <dd>with the associated &lt;dt&gt;&lt;dd&gt;&lt;/dl&gt; tags
 * <dt>&lt;p&gt;
 * <dd>but not align=center...yet
 * <dt>&lt;br&gt;
 * <dd>but not clear=xxx
 * <dt>&lt;table&gt;
 * <dd>including all the associated
 * &lt;td&gt;&lt;th&gt;&lt;tr&gt;&lt;/td&gt;&lt;/th&gt;&lt;/tr&gt;
 * <dt>&lt;ol&gt;
 * <dd>ordered lists
 * <dt>&lt;ul&gt;
 * <dd>unordered lists
 * <dt>&lt;font&gt;
 * <dd>font coloring
 * <dt>&lt;pre&gt;
 * <dd>preformatted text
 * <dt>&lt;code&gt;
 * <dd>fixed point fonts
 * <dt>&lt;i&gt;
 * <dd>italized fonts
 * <dt>&lt;b&gt;
 * <dd>bold fonts
 * <dt>&lt;sub&gt;
 * <dd>subscript
 * <dt>&lt;sup&gt;
 * <dd>superscript
 * <dt>&lt;center&gt;
 * <dd>center
 * <dt>&lt;img&gt;
 * <dd>image located in java sources (&lt;img src="package path/image name"&gt;)
 * <dl>
 * <dt>1. example
 * <dd>converted from JPG: <IMG width="100%" src="doc-files/texdoclet.jpg">
 * <dt>2. example
 * <dd>converted from GIF: <IMG width="100%" src="doc-files/texdoclet.gif">
 * </dl>
 * <dt>&lt;img&gt;
 * <dd>image located in the www: <IMG
 * src="http://upload.wikimedia.org/wikipedia/commons/9/92/LaTeX_logo.svg">
 * </dl>
 * 
 * <H3>Extra tags</H3> A new tag is defined: <CODE>&lt;TEX&gt;</CODE>. This tag
 * is useful for passing <TEX txt="\TeX{}">TeX</TEX> code directly to the <TEX
 * txt="\TeX{}">TeX</TEX> compiler. The following code:
 * 
 * <PRE>
 * 
 *  &lt;TEX txt="\[ F\left( x \right) = \int_{ - \infty }^x {\frac{1}{{\sqrt {2\pi }
 *               }}e^{ - \frac{{z^2 }}{2}} dz} \]"&gt;
 *  &lt;BR&gt;&lt;BR&gt;&lt;B&gt;This alternative text will appear if the javadoc/HTML is parsed
 *  by any other doclet/browser&lt;/B&gt;&lt;BR&gt;&lt;BR&gt;&lt;/TEX&gt;
 * </PRE>
 * <P>
 * will produce the following result: <TEX txt="\[ F\left( x \right) = \int_{ -
 * \infty }^x {\frac{1}{{\sqrt {2\pi } }}e^{ - \frac{{z^2 }}{2}} dz} \]"> <BR>
 * <BR>
 * <B>This alternative text will appear if the javadoc/HTML is parsed by any
 * other doclet/browser</B><BR>
 * <BR>
 * </TEX> The "alternative" text is ignored by the TeXDoclet, but useful if you
 * want to use both the TeXDoclet and a regular HTML based doclet.
 * 
 * @see HTMLtoLaTeXBackEnd
 * @see #start(RootDoc) start
 * @version $Revision: 1.8 $
 * @author Gregg Wonderly - C2 Technologies Inc.
 * @author Soeren Caspersen - XO Software.
 * @author Stefan Marx
 */
public class TeXDoclet extends Doclet {

	private static final String OUT_FILENAME_DOCS_TEX = "docs.tex";
	private static final String OUT_PREAMBLE_SUFFIX = "_preamble";

	// (S.M. modification)
	public static final String SECTION_LEVEL = "section";
	public static final String CHAPTER_LEVEL = "chapter";
	public static final String SUBSECTION_LEVEL = "subsection";

	public static final String BOLD = "{\\bf ";
	// no bold AND truetype support if using textbf !
	// public static final String BOLD = "\\textbf{";
	// public static final String TRUETYPE = "{\\tt ";
	public static final String TRUETYPE = "\\texttt{";
	// public static final String ITALIC = "{\\it ";
	public static final String ITALIC = "\\textit{";

	/** Writer for writing to output file */
	public static PrintWriter os = null;
	static boolean inherited = true;
	static Hashtable map2;
	static Vector map;
	static ClassFilter clsFilt;
	static RootDoc theroot;
	static String docclass = "report";
	static String style = "headings";
	static String title;
	static String date;
	static String author;
	static boolean verbose = false;
	static boolean index = true;
	static boolean classtree = false;
	static boolean serial = false;
	static boolean twosided = false;
	static double overviewindent = 1.0;
	static boolean hyperref = false;
	static boolean pdfhyperref = false;
	static boolean versioninfo = false;
	static boolean summaries = true;
	static String outfile = OUT_FILENAME_DOCS_TEX;
	static String setupFile = "docsetup.tex";
	static String finishFile = "docfinish.tex";
	static String packageFile = "docpackage.tex";
	static String preambleFile = "docinit.tex";
	static File packageDir = null;
	// static Hashtable appendencies = new Hashtable();
	static Hashtable refs = new Hashtable();
	static Hashtable externalrefs = new Hashtable();

	// ------ (S.M. modification)
	static boolean includeTexOutputInOtherTexFile = false;
	static String sectionLevelMax = null;
	static String[] sectionLevels = new String[3];
	static boolean useFieldSummary = true;
	static boolean useConstructorSummary = true;
	static boolean useHr = false;
	static boolean shortInheritance = false;
	/**
	 * print writer for extra init file
	 */
	static PrintWriter osPreamble = null;
	static String outfilePreamble = null;
	static String imagesPath = null;
	static String subtitle = null;
	static String introFile = null;

	// ------ (S.M. modification end)

	/*
	 * Testing entry point for testing tables
	 */
	public static void main(String args[]) {
		init();
		os.println("\\documentstyle{book}");
		os.println("\\begin{document}");
		os.println(HTMLtoLaTeXBackEnd.fixText("<table border> "
				+ "<tr><th>Heading 1<th>Heading 2<th>Heading 3"
				+ "<tr><td>Column 1<td colspan=2 align=right>2 columns here"
				+ "<tr><td colspan=2>two here as well<td>just 1"
				+ "<tr><td align=left>1<td>2<td align=right>3" + "</table>"));
		os.println("\\end{document}");
	}

	/**
	 * Returns how many arguments would be consumed if <code>option</code> is a
	 * recognized option.
	 * 
	 * @param option
	 *            the option to check
	 */
	public static int optionLength(String option) {
		if (option.equals("-title")) {
			return 2;
		} else if (option.equals("-date")) {
			return 2;
		} else if (option.equals("-docclass")) {
			return 2;
		} else if (option.equals("-doctype")) {
			return 2;
		} else if (option.equals("-author")) {
			return 2;
		} else if (option.equals("-texsetup")) {
			return 2;
		} else if (option.equals("-texinit")) {
			return 2;
		} else if (option.equals("-texfinish")) {
			return 2;
		} else if (option.equals("-texpackage")) {
			return 2;
		} else if (option.equals("-classfilter")) {
			return 2;
		} else if (option.equals("-noinherited")) {
			return 1;
		} else if (option.equals("-serial")) {
			return 1;
		} else if (option.equals("-nosummaries")) {
			return 1;
		} else if (option.equals("-noindex")) {
			return 1;
		} else if (option.equals("-tree")) {
			return 1;
		} else if (option.equals("-hyperref")) {
			return 1;
		} else if (option.equals("-twosided")) {
			return 1;
		} else if (option.equals("-pdfhyperref")) {
			return 1;
		} else if (option.equals("-version")) {
			return 1;
		} else if (option.equals("-treeindent")) {
			return 2;
		} else if (option.equals("-link")) {
			return 3;
		} else if (option.equals("-nofieldsummary")) {
			return 1;
		} else if (option.equals("-noconstructorsummary")) {
			return 1;
		} else if (option.equals("-include")) {
			return 1;
		} else if (option.equals("-sectionlevel")) {
			return 2;
		} else if (option.equals("-hr")) {
			return 1;
		} else if (option.equals("-shortinherited")) {
			return 1;
		} else if (option.equals("-help")) {
			HelpOutput.printHelp();
			return 1;
		} else if (option.equals("-output")) {
			return 2;
		} else if (option.equals("-imagespath")) {
			return 2;
		} else if (option.equals("-subtitle")) {
			return 2;
		} else if (option.equals("-texintro")) {
			return 2;
		}
		System.out.println("unknown option " + option);
		return Doclet.optionLength(option);
	}

	/**
	 * Checks the passed options and their arguments for validity.
	 * 
	 * @param args
	 *            the arguments to check
	 * @param err
	 *            the interface to use for reporting errors
	 */
	static public boolean validOptions(String[][] args, DocErrorReporter err) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i][0].equals("-output")) {
				outfile = args[i][1];
			} else if (args[i][0].equals("-date")) {
				date = args[i][1];
			} else if (args[i][0].equals("-title")) {
				title = args[i][1];
			} else if (args[i][0].equals("-author")) {
				author = args[i][1];
			} else if (args[i][0].equals("-verbose")) {
				verbose = true;
			} else if (args[i][0].equals("-docclass")) {
				docclass = args[i][1];
			} else if (args[i][0].equals("-classfilter")) {
				String fcl = args[i][1];
				try {
					clsFilt = (ClassFilter) Class.forName(fcl).newInstance();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(2);
				}
			} else if (args[i][0].equals("-doctype")) {
				style = args[i][1];
			} else if (args[i][0].equals("-texsetup")) {
				setupFile = args[i][1];
			} else if (args[i][0].equals("-texinit")) {
				preambleFile = args[i][1];
			} else if (args[i][0].equals("-texfinish")) {
				finishFile = args[i][1];
			} else if (args[i][0].equals("-texpackage")) {
				packageFile = args[i][1];
			} else if (args[i][0].equals("-noinherited")) {
				inherited = false;
			} else if (args[i][0].equals("-serial")) {
				serial = true;
			} else if (args[i][0].equals("-nosummaries")) {
				summaries = false;
			} else if (args[i][0].equals("-noindex")) {
				index = false;
			} else if (args[i][0].equals("-twosided")) {
				twosided = true;
				// (S.M. modification)
				// } else if ( args[i][0].equals( "-notree" ) ) {
				// overview = false;
			} else if (args[i][0].equals("-tree")) {
				classtree = true;
			} else if (args[i][0].equals("-hyperref")) {
				hyperref = true;
			} else if (args[i][0].equals("-pdfhyperref")) {
				pdfhyperref = true;
				hyperref = true;
			} else if (args[i][0].equals("-version")) {
				versioninfo = true;
			} else if (args[i][0].equals("-treeindent")) {
				overviewindent = Double.parseDouble(args[i][1]);
			} else if (args[i][0].equals("-link")) {
				try {
					FileInputStream in = new FileInputStream(args[i][2]
							+ ".map");
					ObjectInputStream p = new ObjectInputStream(in);
					Hashtable exref = (Hashtable) p.readObject();
					externalrefs.put(args[i][1], exref);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ----------- (S.M. modification)
			} else if (args[i][0].equals("-nofieldsummary")) {
				useFieldSummary = false;
			} else if (args[i][0].equals("-noconstructorsummary")) {
				useConstructorSummary = false;
			} else if (args[i][0].equals("-include")) {
				includeTexOutputInOtherTexFile = true;
			} else if (args[i][0].equals("-sectionlevel")) {
				sectionLevelMax = args[i][1];
			} else if (args[i][0].equals("-hr")) {
				useHr = true;
			} else if (args[i][0].equals("-shortinherited")) {
				shortInheritance = true;
			} else if (args[i][0].equals("-help")) {
				// done in optionsLength()
				// HelpOutput.printHelp();
			} else if (args[i][0].equals("-imagespath")) {
				imagesPath = args[i][1];
			} else if (args[i][0].equals("-subtitle")) {
				subtitle = args[i][1];
			} else if (args[i][0].equals("-texintro")) {
				introFile = args[i][1];
			}

			if (sectionLevelMax != null
					&& !sectionLevelMax.equals(CHAPTER_LEVEL)
					&& !sectionLevelMax.equals(SECTION_LEVEL)
					&& !sectionLevelMax.equals(SUBSECTION_LEVEL)) {
				System.err.println("Invalid option -sectionlevel"
						+ " (use \"subsection\",\"section\" or \"chapter\")");
				return false;
			}
			// ----------- (S.M. modification end)

		}
		System.out.println("args parsed");
		return true;
	}

	public static void init() {

		map2 = new Hashtable();
		map = new Vector();
		try {
			os = new PrintWriter(new FileWriter(outfile));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// (S.M. modification)
		try {
			if (includeTexOutputInOtherTexFile) {
				outfilePreamble = outfile
						.substring(0, outfile.lastIndexOf("."))
						+ OUT_PREAMBLE_SUFFIX + ".tex";
				osPreamble = new PrintWriter(new FileWriter(outfilePreamble));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// (S.M. modification)
		initSections();
	}

	public static void initSections() {
		if (sectionLevelMax == null) {
			if (!includeTexOutputInOtherTexFile) {
				if (docclass.equals("scrreprt") || docclass.equals("report")) {
					sectionLevelMax = CHAPTER_LEVEL;
				} else if (docclass.equals("scrartcl")
						|| docclass.equals("article")) {
					sectionLevelMax = SECTION_LEVEL;
				} else {
					sectionLevelMax = SECTION_LEVEL;
				}
			} else {
				sectionLevelMax = SECTION_LEVEL;
			}
		}

		if (sectionLevelMax.equals(CHAPTER_LEVEL)) {
			sectionLevels[0] = "chapter";
			sectionLevels[1] = "section";
			sectionLevels[2] = "subsection";
		} else if (sectionLevelMax.equals(SECTION_LEVEL)) {
			sectionLevels[0] = "section";
			sectionLevels[1] = "subsection";
			sectionLevels[2] = "subsubsection";
		} else if (sectionLevelMax.equals(SUBSECTION_LEVEL)) {
			sectionLevels[0] = "subsection";
			sectionLevels[1] = "subsubsection";
			sectionLevels[2] = "subsubsection";
		}
	}

	public static void finish() {
		if (os != null) {
			try {
				os.close();
			} catch (Exception ex) {
			}
		}
		// (S.M. modification)
		if (osPreamble != null) {
			try {
				osPreamble.close();
			} catch (Exception ex) {
			}
		}
		try {
			FileOutputStream ostream = new FileOutputStream(outfile + ".map");
			ObjectOutputStream p = new ObjectOutputStream(ostream);
			p.writeObject(refs);
			p.flush();
			ostream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Called by the framework to format the entire document
	 * 
	 * @param root
	 *            the root of the starting document
	 */
	public static boolean start(RootDoc root) {

		theroot = root;

		init();

		// (S.M. modification)
		if (!includeTexOutputInOtherTexFile) {

			printPreamble(os);

			os.println("\\begin{document}");

			if (title != null) {
				os.println("\\maketitle");
			}

			addFile(os, setupFile, false);

			os.println("\\sloppy");

			os.println("\\addtocontents{toc}{\\protect\\markboth{Contents}{Contents}}");
			os.println("\\tableofcontents");

		} else {

			printPreamble(osPreamble);

			addFile(osPreamble, setupFile, false);

		}

		addFile(os, introFile, false);

		if (root.inlineTags().length > 0) {

			// (S.M. modification)
			os.println("\\" + sectionLevels[0] + "*{Introduction}{");
			os.println(" \\addcontentsline{toc}{" + sectionLevels[0]
					+ "}{Introduction}");

			os.println("\\thispagestyle{empty}");
			os.println("\\markboth{Introduction}{Introduction}");
			printTags(root.inlineTags());
			os.println("}");

		}

		ClassDoc[] cls = root.classes();

		ExecutableMemberDoc[] mems;

		PackageDoc[] specifiedPackages = root.specifiedPackages();
		System.out.println("specifiedPackages : " + specifiedPackages.length);
		for (int i = 0; i < specifiedPackages.length; i++) {
			Package P = new Package(specifiedPackages[i].name(),
					specifiedPackages[i]);
			System.out.println(i + ". " + specifiedPackages[i].name());
			map.add(P);
			map2.put(specifiedPackages[i].name(), P);
		}

		if (clsFilt != null) {
			System.out.println("...Filter Classes with: " + clsFilt);
		}
		for (int i = 0; i < cls.length; ++i) {
			ClassDoc cd = cls[i];

			if (clsFilt != null && clsFilt.includeClass(cd) == false) {
				System.out.println("...Filtering out Class: "
						+ cd.qualifiedName());
				continue;
			}

			Package v;
			PackageDoc pkgDoc = cd.containingPackage();
			String pkg = pkgDoc.name();
			if ((v = (Package) map2.get(pkg)) == null) {
				v = new Package(pkg, pkgDoc);
				map2.put(pkg, v);
				map.add(v);
			}
			v.addElement(cd);
		}

		// Sorting
		Enumeration h = map.elements();
		while (h.hasMoreElements()) {
			final Package pkg = (Package) h.nextElement();
			pkg.sort();
		}

		// Class hierachy
		if (classtree) {

			printClassHierarchy(root);

		}

		// os.println("\\part{" +
		// HTMLtoLaTeXBackEnd.fixText((String)groupHeader.get(0)) + "}");
		// os.println(HTMLtoLaTeXBackEnd.fixText((String)groupPackages.get(0)));

		// Packages

		// os.println( "\\gdef\\packagename{}" );
		// os.println( "\\gdef\\classname{}" );
		Enumeration e = map.elements();
		while (e.hasMoreElements()) {
			final Package pkg = (Package) e.nextElement();

			// System.out.println("processing Package: " + pkg.pkg);

			// (S.M. modification)
			// os.println( "\\newpage" );
			// os.println(
			// "\\def\\packagename{"+HTMLtoLaTeXBackEnd.fixText(pkg.pkg)+"}" );

			addFile(os, packageFile, false);

			// (S.M. modification)
			// os.println(
			// "\\section"+BOLD+" Package "+HTMLtoLaTeXBackEnd.fixText(pkg.pkg)
			// +"}{" );
			os.println("\\" + sectionLevels[0] + "{Package "
					+ HTMLtoLaTeXBackEnd.fixText(pkg.pkg) + "}{");

			// (S.M. modification)
			// os.println( "\\thispagestyle{empty}" );

			os.print("\\label{" + refName(makeRefKey(pkg.pkg)) + "}");
			if (hyperref) {
				os.println("\\hypertarget{" + refName(makeRefKey(pkg.pkg))
						+ "}{}");
			}

			// (S.M. modification)
			// os.println(
			// "\\markboth{\\protect\\packagename}{\\protect\\packagename}" );
			// os.println(
			// "\\markboth{\\protect\\packagename \\hspace{.02in} -- \\protect\\classname}{\\protect\\packagename \\hspace{.02in} -- \\protect\\classname}"
			// );

			// (S.M. modification)
			if (ITALIC.indexOf("textit") != -1) {
				os.println("\\hskip -.05in");
			}
			os.println("\\hbox to \\hsize{" + ITALIC
					+ " Package Contents\\hfil Page}}");
			if (useHr) {
				os.println("\\rule{\\hsize}{.7mm}");
			}
			tocForClasses("Interfaces", pkg.interfaces);
			tocForClasses("Classes", pkg.classes);
			os.println("\\vskip .1in");
			if (useHr) {
				os.println("\\rule{\\hsize}{.7mm}");
			}
			os.println("\\vskip .1in");

			// The path relative to which <IMG> will be resolved.
			packageDir = findPackageDir(pkg.pkg, root);

			// Package comments
			if (pkg.pkgDoc.inlineTags().length > 0) {
				printTags(pkg.pkgDoc.inlineTags());

				// (S.M. modification)
				if (useHr) {
					os.println("\\mbox{}\\\\ \\rule{\\hsize}{.7mm}");
				}
				if (useHr) {
					os.println("\\vskip .1in");
				}
			}

			// (S.M. modification)
			// os.println( "\\newpage");
			// os.println(
			// "\\markboth{\\protect\\packagename \\hspace{.02in} -- \\protect\\classname}{\\protect\\packagename \\hspace{.02in} -- \\protect\\classname}"
			// );

			layoutClasses("Interfaces", pkg.interfaces);
			layoutClasses("Classes", pkg.classes);
			layoutClasses("Exceptions", pkg.exceptions);
			layoutClasses("Error", pkg.errors);

			os.println("}");
		}
		/*
		 * if (appendencies.size() > 0) { os.println("\\appendix"); Iterator it
		 * = appendencies.keySet().iterator(); while (it.hasNext()) {
		 * //os.println
		 * (" \\chapter{"+(char)('A'+1)+"}\n\\hyperdef{appendix1}{}sdafafdasd");
		 * os.println(" \\hyperdef{appendix1}{}sdafafdasd"); String sfa =
		 * (String) it.next(); System.out.println(sfa); //addFile(sfa, true); }
		 * }
		 */

		/*
		 * os.println("\\markboth{}{}"); if (index) { os.println( "\\printindex"
		 * ); }
		 */

		addFile(os, finishFile, false);

		if (!includeTexOutputInOtherTexFile) {
			os.println("\\end{document}");
		}

		finish();

		return true;
	}

	static void printPreamble(PrintWriter os) {

		// this is not our job if including in other document
		if (!includeTexOutputInOtherTexFile) {

			// (S.M. modification) koma script support (scrreprt, scrartcl)
			if (docclass.equals("scrreprt") || docclass.equals("scrartcl")) {
				os.println("\\documentclass[11pt,a4paper,titlepage,smallheadings,"
						+ "headinclude,headsepline,DIV13,BCOR12.5mm");
			} else {
				os.print("\\documentclass[11pt,a4paper");
			}

			if (twosided) {
				os.print(",twoside,openright");
			}

			os.println("]{" + docclass + "}");

		}

		printPreambleUsePackages(os);

		printPreambleIfPfd(os);

		printPreambleNewCommands(os);

		// os.println("\\definecolor{Light}{gray}{.90}");

		printPreambleTitle(os);

		// already set in \\usepackage{fullpage} option
		// os.println("\\pagestyle{" + style + "}");

		// (S.M. modification) some stuff uncommented
		// os.println("\\addtocontents{toc}{\\protect\\thispagestyle{empty}}");
		// os.println("\\addtocontents{toc}{\\protect\\def\\protect\\packagename{}}");
		// os.println("\\addtocontents{toc}{\\protect\\def\\protect\\classname{}}");

		// os.println("\\oddsidemargin 0in");
		// os.println("\\evensidemargin 0in");
		// os.println("\\topmargin -.3in");
		os.println("\\chardef\\textbackslash=`\\\\");
		// os.println("\\textheight 9.4in");
		// os.println("\\textwidth 6.5in");

		if (index) {
			os.println("\\makeindex");
		}

		addFile(os, preambleFile, false);

	}

	static void printPreambleUsePackages(PrintWriter os) {
		os.println("\\usepackage{color}");
		// (S.M. modification)
		// os.println("\\usepackage{graphicx}");
		// os.println("\\usepackage{amsfonts}");
		// os.println("\\usepackage{amsmath}");
		os.println("\\usepackage{ifthen}");
		if (index) {
			os.println("\\usepackage{makeidx}");
		}
		// (S.M. modification)
		os.println("\\usepackage{ifpdf}");
		os.println("\\usepackage[" + style + "]{fullpage}");
	}

	static void printPreambleTitle(PrintWriter os) {

		if (date == null && !includeTexOutputInOtherTexFile) {
			os.println("\\date{\\today}");
		} else {
			os.println("\\date{" + date + "}");
		}

		if (title != null) {
			if (subtitle == null) {
				os.println("\\title{" + title + "}");
			} else {
				os.println("\\title{" + title + "\\bigskip\\\\ \\Large "
						+ subtitle + "}");
			}
		}
		if (author != null) {
			os.println("\\author{" + author + "}");
		}

	}

	static void printPreambleIfPfd(PrintWriter os) {
		os.println("\\ifpdf \\usepackage[pdftex, pdfpagemode={UseOutlines},"
				+ "bookmarks,colorlinks,linkcolor={blue},plainpages=false,pdfpagelabels,"
				+ "citecolor={red},breaklinks=true]{hyperref}");
		os.println("  \\usepackage[pdftex]{graphicx}");
		os.println("  \\pdfcompresslevel=9");
		os.println("  \\DeclareGraphicsRule{*}{mps}{*}{}");
		os.println("\\else");
		os.println("  \\usepackage[dvips]{graphicx}");
		os.println("\\fi\n");
	}

	static void printPreambleNewCommands(PrintWriter os) {
		os.println("\\newcommand{\\entityintro}[3]{%");
		os.println("  \\hbox to \\hsize{%");
		os.println("    \\vbox{%");
		os.println("      \\hbox to .2in{}%");
		os.println("    }%");
		os.println("    " + BOLD + " #1}%");
		os.println("    \\dotfill\\pageref{#2}%");
		os.println("  }");
		os.println("  \\makebox[\\hsize]{%");
		os.println("    \\parbox{.4in}{}%");
		os.println("    \\parbox[l]{5in}{%");
		// (S.M. modification) no italic for first sentence of class
		// description
		// os.println("      \\vspace{1mm}\\it%");
		os.println("      \\vspace{1mm}%");
		os.println("      #3%");
		os.println("      \\vspace{1mm}%");
		os.println("    }%");
		os.println("  }%");
		os.println("}");

		// (S.M. modification)

		// os.println("\\newcommand{\\isep}[0]{%");
		// os.println("\\setlength{\\itemsep}{-.4ex}");
		// os.println("}");
		//
		// os.println("\\newcommand{\\sld}[0]{%");
		// os.println("\\setlength{\\topsep}{0em}");
		// os.println("\\setlength{\\partopsep}{0em}");
		// os.println("\\setlength{\\parskip}{0em}");
		// os.println("\\setlength{\\parsep}{-1em}");
		// os.println("}");

		// os.println("\\newcommand{\\membername}[1]{{\\it #1}\\linebreak}");

		if (useHr) {
			os.println("\\newcommand{\\divideents}[1]{\\vskip -1em\\indent\\rule{2in}{.5mm}}");
		}

		os.println("\\newcommand{\\refdefined}[1]{");
		os.println("\\expandafter\\ifx\\csname r@#1\\endcsname\\relax");
		os.println("\\relax\\else");
		// (S.M. modification)
		// os.println("{$($in \\ref{#1}, page \\pageref{#1}$)$}");
		// os.println("\\fi}" );
		os.println("{$($in \\ref{#1}, page \\pageref{#1}$)$}\\fi}");
	}

	/**
	 * Adds an entire file (HTML or LaTeX).
	 * 
	 * @param fixText
	 *            Should be true if the file contains HTML.
	 */
	static boolean addFile(PrintWriter os, String name, boolean fixText) {

		if (name == null) {
			return true;
		}

		try {
			File f = new File(name);
			if (f.exists() == false) {
				if (verbose) {
					System.out.println("Can not open: " + f);
				}
				return false;
			}
			BufferedReader rd = new BufferedReader(new FileReader(f));
			try {
				StringBuffer buf = new StringBuffer((int) f.length());
				String str;
				while ((str = rd.readLine()) != null) {
					buf.append(str + "\n");
				}
				if (fixText) {
					os.println(HTMLtoLaTeXBackEnd.fixText(buf.toString()));
				} else {
					os.println(buf.toString());
				}
			} finally {
				rd.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Produces a table-of-contents for classes.
	 */
	static void tocForClasses(String title, Vector v) {
		if (v.size() > 0) {
			os.println("\\vskip .13in");

			// os.println("\\hbox" + BOLD + " "
			// + HTMLtoLaTeXBackEnd.fixText(title) + "}");

			os.println("\\hbox{" + BOLD + " "
					+ HTMLtoLaTeXBackEnd.fixText(title) + "}}");

			for (int i = 0; i < v.size(); ++i) {
				ClassDoc cd = (ClassDoc) v.elementAt(i);
				os.print("\\entityintro{"
						+ HTMLtoLaTeXBackEnd.fixText(cd.name()) + "}" + "{"
						+ refName(makeRefKey(cd.qualifiedName())) + "}" + "{");
				printTags(cd.firstSentenceTags());
				os.println("}");
			}
		}
	}

	/**
	 * Produces a LaTeX referencekey from a Java identifier.
	 */
	static String refName(String key) {
		return key;
	}

	static void printClassHierarchy(RootDoc root) {

		// (S.M. modification)
		os.println("\\" + sectionLevels[0] + "*{Class Hierarchy}{");

		os.println("\\thispagestyle{empty}");
		os.println("\\markboth{Class Hierarchy}{Class Hierarchy}");

		os.println("\\addcontentsline{toc}{" + sectionLevels[0]
				+ "}{Class Hierarchy}");

		// Classes
		ClassHierachy classHierachy = new ClassHierachy();
		Enumeration f = map.elements();
		while (f.hasMoreElements()) {
			final Package pkg = (Package) f.nextElement();
			for (int i = 0; i < pkg.classes.size(); i++) {
				classHierachy.add((ClassDoc) pkg.classes.get(i));
			}
		}
		if (classHierachy.root.size() != 0) {

			// (S.M. modification)
			os.println("\\" + sectionLevels[1] + "*{Classes}");

			// (S.M. modification) raggedright needed here for correct indent
			os.println("{\\raggedright");

			classHierachy.printTree(root, overviewindent);

			os.println("}");
		}

		// Interfaces
		InterfaceHierachy interfaceHierachy = new InterfaceHierachy();
		f = map.elements();
		while (f.hasMoreElements()) {
			final Package pkg = (Package) f.nextElement();
			for (int i = 0; i < pkg.interfaces.size(); i++) {
				interfaceHierachy.add((ClassDoc) pkg.interfaces.get(i));
			}
		}
		if (interfaceHierachy.root.size() != 0) {
			// (S.M. modification)
			// os.println( "\\section*{Interfaces}");
			os.println("\\" + sectionLevels[1] + "*{Interfaces}");

			interfaceHierachy.printTree(root, overviewindent);
		}

		// Exceptions
		ClassHierachy exceptionHierachy = new ClassHierachy();
		f = map.elements();
		while (f.hasMoreElements()) {
			final Package pkg = (Package) f.nextElement();
			for (int i = 0; i < pkg.exceptions.size(); i++) {
				exceptionHierachy.add((ClassDoc) pkg.exceptions.get(i));
			}
		}
		if (exceptionHierachy.root.size() != 0) {

			// (S.M. modification)
			// os.println( "\\section*{Exceptions}");
			os.println("\\" + sectionLevels[1] + "*{Exceptions}");

			exceptionHierachy.printTree(root, overviewindent);
		}

		// Errors
		ClassHierachy errorHierachy = new ClassHierachy();
		f = map.elements();
		while (f.hasMoreElements()) {
			final Package pkg = (Package) f.nextElement();
			for (int i = 0; i < pkg.errors.size(); i++) {
				errorHierachy.add((ClassDoc) pkg.errors.get(i));
			}
		}
		if (errorHierachy.root.size() != 0) {

			// (S.M. modification)
			// os.println( "\\section*{Errors}");
			os.println("\\" + sectionLevels[1] + "*{Errors}");

			errorHierachy.printTree(root, overviewindent);
		}

		os.println("}");

	}

	/**
	 * Lays out a list of classes.
	 * 
	 * @param type
	 *            Title of the section, e.g. "Interfaces", "Exceptions" etc.
	 * @param classes
	 *            Vector of the classes to be laid out.
	 */
	static void layoutClasses(String type, Vector classes) {
		// (S.M. modification)
		// if ( classes.size() > 0 ) {
		// os.print("\\section{" );
		// os.print( type );
		// os.println( "}{" );
		// }
		for (int i = 0; i < classes.size(); ++i) {
			ClassDoc cd = (ClassDoc) classes.elementAt(i);
			// os.println(
			// "\\gdef\\classname{"+HTMLtoLaTeXBackEnd.fixText(cd.name())+"}" );

			// (S.M. modification)
			os.print("\\" + sectionLevels[1] + "{");

			String mtype = "Class";
			if (type.equals("Interfaces")) {
				mtype = "Interface";
			}
			if (type.equals("Exceptions")) {
				mtype = "Exception";
			}
			os.print("\\label{" + refName(makeRefKey(cd.qualifiedName())) + "}");
			if (index) {
				if (cd.isInterface()) {
					os.print("\\index{" + HTMLtoLaTeXBackEnd.fixText(cd.name())
							+ "@" + ITALIC + " "
							+ HTMLtoLaTeXBackEnd.fixText(cd.name()) + "}}");
				} else {
					os.print("\\index{" + HTMLtoLaTeXBackEnd.fixText(cd.name())
							+ "}");
				}
			}
			// os.println(
			// ""+BOLD+" {\\it "+mtype+"} "+HTMLtoLaTeXBackEnd.fixText(cd.name())+"}}{");
			os.println(mtype + " " + HTMLtoLaTeXBackEnd.fixText(cd.name())
					+ "}{");
			if (useHr) {
				os.println("\\rule[1em]{\\hsize}{4pt}\\vskip -1em");
			}
			if (hyperref) {
				os.print("\\hypertarget{"
						+ refName(makeRefKey(cd.qualifiedName())) + "}{}");
			}

			os.println("\\vskip .1in ");
			if (cd.inlineTags().length > 0) {
				printTags(cd.inlineTags());
				os.println("\\vskip .1in ");
			}

			SeeTag[] sees = cd.seeTags();
			if (sees.length > 0) {
				os.println("\\" + sectionLevels[2] + "{See also}{}\n");
				os.println("  \\begin{list}{-- }{\\setlength{\\itemsep}{0cm}\\setlength{\\parsep}{0cm}}");
				for (int j = 0; j < sees.length; ++j) {
					os.print("\\item{ ");
					printSeesTag(sees[j], cd.containingPackage());
					os.println("} ");
				}
				os.println("  \\end{list}");
			}

			// os.println( "\\"+sectionLevels[2]+"{Declaration}{" );
			// os.println(
			// "\\fcolorbox{black}{Light}{\\parbox[s]{.98\\hsize}{");
			// os.print(
			// "\\small "+HTMLtoLaTeXBackEnd.fixText(cd.modifiers())+" " );
			os.println("\\" + sectionLevels[2] + "{Declaration}{");
			// os.println(
			// "\\fcolorbox{black}{Light}{\\parbox[s]{.98\\hsize}{");
			os.print("\\small " + HTMLtoLaTeXBackEnd.fixText(cd.modifiers())
					+ " ");
			if (cd.isInterface() == false) {
				os.print("class ");
			}
			os.println(HTMLtoLaTeXBackEnd.fixText(cd.name()));
			ClassDoc sc = cd.superclass();
			if (sc != null) {
				os.println("\\\\ " + BOLD + " extends} "
						+ HTMLtoLaTeXBackEnd.fixText(sc.qualifiedName()));
				printRef(sc.containingPackage(), sc.name(), null);
			}

			ClassDoc intf[] = cd.interfaces();
			if (intf.length > 0) {
				// os.println( "\\\\ "+BOLD+" implements} " );
				// ----- (S.M. modification) Bug fixed !
				// interfaces never implement something !
				if (cd.isInterface() == false) {
					os.println("\\\\ " + BOLD + " implements} ");
				} else {
					os.println("\\\\ " + BOLD + " extends} ");
				}
				// ----- (S.M. modification end)
				for (int j = 0; j < intf.length; ++j) {
					ClassDoc in = intf[j];
					String nm;
					if (in.containingPackage().name()
							.equals(cd.containingPackage().name())) {
						nm = in.name();
					} else {
						nm = in.qualifiedName();
					}
					if (j > 0) {
						os.print(", ");
					}
					os.print(HTMLtoLaTeXBackEnd.fixText(nm));
				}
			}
			// os.println("}}}");
			os.println("}");
			ExecutableMemberDoc[] mems;
			FieldDoc[] flds;

			Tag[] verTags = cd.tags("version");
			if (versioninfo && verTags.length > 0) {
				os.println("\\" + sectionLevels[2] + "{Version}{"
						+ HTMLtoLaTeXBackEnd.fixText(verTags[0].text()) + "}");
			}

			String subclasses = "";
			for (int index = 0; index < theroot.classes().length; index++) {
				ClassDoc cls = theroot.classes()[index];
				if (cls.subclassOf(cd) && !cls.equals(cd)) {
					if (!subclasses.equals("")) {
						subclasses += ", ";
					}
					subclasses += HTMLtoLaTeXBackEnd.fixText(cls.name());
					subclasses += "\\small{\\refdefined{"
							+ refName(makeRefKey(cls.qualifiedName())) + "}}";
				}
			}

			// if (!subclasses.equals("")) {
			// os.println("\\"+sectionLevels[2]+"{All known subclasses}{" +
			// subclasses +
			// "}");
			// }
			// ----- (S.M. modification) Bug fixed !
			// interfaces never have subclasses, they have subinterfaces !
			if (cd.isInterface()) {
				if (!subclasses.equals("")) {
					os.println("\\" + sectionLevels[2]
							+ "{All known subinterfaces}{" + subclasses + "}");
				}
			} else {
				if (!subclasses.equals("")) {
					os.println("\\" + sectionLevels[2]
							+ "{All known subclasses}{" + subclasses + "}");
				}
			}
			// ----- (S.M. modification end)

			String subintf = "";
			String implclasses = "";
			if (cd.isInterface()) {
				for (int index = 0; index < theroot.classes().length; index++) {
					ClassDoc cls = theroot.classes()[index];
					boolean impls = false;
					for (int w = 0; w < cls.interfaces().length; w++) {
						ClassDoc intfDoc = cls.interfaces()[w];
						if (intfDoc.equals(cd)) {
							impls = true;
						}
					}
					if (impls) {
						if (cls.isInterface()) {
							if (!subintf.equals("")) {
								subintf += ", ";
							}
							subintf += cls.name();
							subintf += "\\small{\\refdefined{"
									+ refName(makeRefKey(cls.qualifiedName()))
									+ "}}";
						} else {
							if (!implclasses.equals("")) {
								implclasses += ", ";
							}
							implclasses += cls.name();
							implclasses += "\\small{\\refdefined{"
									+ refName(makeRefKey(cls.qualifiedName()))
									+ "}}";
						}
					}
				}

				// (S.M. modification)
				// Because of the correction above, the subinterfaces are
				// already printed !
				// if (!subintf.equals(""))
				// os.println("\\"+sectionLevels[2]+"{All known subinterfaces}{"
				// +
				// subintf + "}");

				if (!implclasses.equals("")) {
					os.println("\\" + sectionLevels[2]
							+ "{All classes known to implement interface}{"
							+ implclasses + "}");
				}
			}

			if (summaries) {
				flds = cd.fields();
				if (flds.length > 0) {
					printFieldSummary(flds, "Field summary");
				}

				if (useConstructorSummary) {
					mems = cd.constructors();
					if (mems.length > 0) {
						printMethodSummary(mems, "Constructor summary");
					}
				}

				if (useFieldSummary) {
					mems = cd.methods();
					if (mems.length > 0) {
						printMethodSummary(mems, "Method summary");
					}
				}
			}

			flds = cd.serializableFields();
			if (flds.length > 0 && serial) {
				printFields(cd, flds, "Serializable Fields", false);
			}
			flds = cd.fields();
			if (flds.length > 0) {
				printFields(cd, flds, "Fields", true);
			}
			mems = cd.constructors();
			if (mems.length > 0) {
				os.println("\\" + sectionLevels[2] + "{Constructors}{");
				printMembers(cd, mems, true);
				os.println("}");
			}
			mems = cd.methods();
			if (mems.length > 0) {
				os.println("\\" + sectionLevels[2] + "{Methods}{");
				printMembers(cd, mems, true);
				os.println("}");
			}
			// (S.M. modification)
			//
			// if (inherited == false) {
			// os.println( "\\hide{inherited}{" );
			// }
			if (inherited == true) {

				boolean yet = false;

				// (S.M. modification) cd.superclass() will always return null
				// for interfaces
				// ??!
				// so we handle it specially
				if (!cd.isInterface()) {

					ClassDoc par = cd.superclass();

					while (par != null
							&& par.qualifiedName().equals("java.lang.Object") == false) {

						printInherited(par);

						// boolean members = false;
						//
						// MemberDoc[] inheritedmembers = new MemberDoc[par
						// .fields().length + par.methods().length];
						// for (int k = 0; k < par.fields().length; k++) {
						// inheritedmembers[k] = par.fields()[k];
						// members = true;
						// }
						// for (int k = 0; k < par.methods().length; k++) {
						// inheritedmembers[k + par.fields().length] = par
						// .methods()[k];
						// members = true;
						// }
						//
						// // (S.M. modification) print only if members
						// available (
						// // for instance in case of a not correct set
						// classpath
						// // members are missed)
						// if (members) {
						// os.print("\\" + sectionLevels[2] + "{");
						// // (S.M. modification) do not user true type and do
						// // not print full name here
						// // in the section heading
						// // this produces an ugly view in the Contents ...
						// //
						// os.print("Members inherited from class "+TRUETYPE+""
						// os.print("Members inherited from class {"
						// + HTMLtoLaTeXBackEnd.fixText(
						// // par.qualifiedName()
						// par.name())
						// + "} \\textnormal{\\small");
						// printRef(par.containingPackage(), par.name(), null);
						// os.println("}}{");
						// os.println("{\\small ");
						// // (S.M. modification)
						// // ... but print here the full name including the
						// // package
						// os.println("(Fully qualified name: "
						// + par.qualifiedName() + ")");
						//
						// printinheritedMembers(par, inheritedmembers, false);
						//
						// os.println("}}");
						// }
						par = par.superclass();
					}

				} else {

					List superclasses = new Vector();
					while (getSuperClass(cd, superclasses) != null) {
					}

					superclasses = sortSuperclasses(superclasses);

					for (int m = superclasses.size() - 1; m >= 0; m--) {

						ClassDoc par = (ClassDoc) superclasses.get(m);

						printInherited(par);

						// boolean members = false;
						//
						// MemberDoc[] inheritedmembers = new MemberDoc[par
						// .fields().length + par.methods().length];
						// for (int k = 0; k < par.fields().length; k++) {
						// inheritedmembers[k] = par.fields()[k];
						// members = true;
						// }
						// for (int k = 0; k < par.methods().length; k++) {
						// inheritedmembers[k + par.fields().length] = par
						// .methods()[k];
						// members = true;
						// }
						//
						// // (S.M. modification) print only if members
						// available (
						// // if class not found because classpath not correctly
						// // set, they would be missed)
						// if (members) {
						// os.print("\\" + sectionLevels[2] + "{");
						// //
						// os.println("Members inherited from class "+TRUETYPE+""+
						// //
						// HTMLtoLaTeXBackEnd.fixText(par.qualifiedName())+"}}{");
						// // os.println("\\par{\\small " );
						// // printRef( par.containingPackage(), par.name(),
						// // null );
						// os.print("Members inherited from class "+TRUETYPE+""
						// + HTMLtoLaTeXBackEnd.fixText(par
						// .qualifiedName())
						// + "} \\textnormal{\\small");
						// printRef(par.containingPackage(), par.name(), null);
						// os.println("}}{");
						// os.println("{\\small ");
						//
						// printinheritedMembers(par, inheritedmembers, false);
						//
						// os.println("}}");
						// }
					}

				}

			}
			// (S.M. modification)
			//
			// if ( inherited == false ) {
			// os.println("}");
			// }

			os.println("}");
			// (S.M. modification)
			// os.print("\\newpage" );
		}
		// (S.M. modification)
		// if ( classes.size() > 0 ) {
		// os.println("}");
		// }
	}

	/**
	 * searches in all classes of the root doc for a superclass of the given
	 * subsclass, that is not already in the list of superclasses adn adds it to
	 * the list if found
	 */
	static ClassDoc getSuperClass(ClassDoc subclass, List superclasses) {
		ClassDoc[] cls = theroot.classes();
		for (int n = 0; n < cls.length; ++n) {
			ClassDoc cd2 = cls[n];
			if (subclass.subclassOf(cd2)) {
				if (subclass != cd2 && !superclasses.contains(cd2)) {
					superclasses.add(cd2);
					return cd2;
				}
			}
		}
		return null;
	}

	static List sortSuperclasses(List superclasses) {
		List result = new Vector();
		int count = superclasses.size();

		for (int k = 0; k < count; k++) {
			for (int i = 0; i < superclasses.size(); i++) {
				ClassDoc cd = (ClassDoc) superclasses.get(i);
				boolean isSubInterface = false;
				for (int j = 0; j < superclasses.size(); j++) {
					ClassDoc cd2 = (ClassDoc) superclasses.get(j);
					if (cd.subclassOf(cd2) && cd != cd2) {
						isSubInterface = true;
						break;
					}

				}
				if (!isSubInterface) {
					result.add(cd);
					superclasses.remove(cd);
					break;
				}
			}

		}
		return result;
	}

	/**
	 * Enumerates the fields passed and formats them using Tex statements.
	 * 
	 * @param flds
	 *            the fields to format
	 */
	static void printFields(ClassDoc cd, FieldDoc[] flds, String title,
			boolean labels) {
		boolean yet = false;
		for (int i = 0; i < flds.length; ++i) {
			FieldDoc f = flds[i];
			if (!yet) {
				os.println("\\" + sectionLevels[2] + "{" + title + "}{");
				if (useHr) {
					os.println("\\rule[1em]{\\hsize}{2pt}");
				}
				os.println("\\begin{itemize}");
				yet = true;
			}
			os.println("\\item{");
			os.println("\\index{" + HTMLtoLaTeXBackEnd.fixText(f.name()) + "}");
			if (labels) {
				os.print("\\label{" + refName(makeRefKey(f.qualifiedName()))
						+ "}");
				if (hyperref) {
					os.print("\\hypertarget{"
							+ refName(makeRefKey(f.qualifiedName())) + "}{");
				}
			}
			if (!cd.isInterface()) {
				os.print(HTMLtoLaTeXBackEnd.fixText(f.modifiers()) + " ");
			}
			os.print(HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(
					f.containingPackage(), f.type().qualifiedTypeName()))
					+ " ");
			os.print("" + BOLD + " " + HTMLtoLaTeXBackEnd.fixText(f.name())
					+ "}");
			if (labels && hyperref) {
				os.println("}");
			}

			if (f.inlineTags().length > 0 || f.seeTags().length > 0) {
				os.println("\\begin{itemize}");
				if (f.inlineTags().length > 0) {
					os.println("\\item{\\vskip -.9ex ");
					printTags(f.inlineTags());
					os.println("}");
				}

				// See tags
				SeeTag[] sees = f.seeTags();
				if (sees.length > 0) {
					os.println("\\item{{ See also}");
					os.println("  \\begin{itemize}");
					for (int j = 0; j < sees.length; ++j) {
						os.print("\\item{ ");
						printSeesTag(sees[j], f.containingPackage());
						os.println("}");
					}
					os.println("  \\end{itemize}");
					os.println("}%end item");
				}

				os.println("\\end{itemize}");
			}
			os.println("}");
		}
		if (yet) {
			os.println("\\end{itemize}");
			os.println("}");
		}
	}

	/**
	 * Produces a constructor/method summary.
	 * 
	 * @param dmems
	 *            The fields to be summarized.
	 * @param title
	 *            The title of the section.
	 */
	static void printMethodSummary(ExecutableMemberDoc[] dmems, String title) {
		if (dmems.length == 0) {
			return;
		}
		os.println("\\" + sectionLevels[2] + "{" + title + "}{");
		os.println("\\begin{verse}");
		boolean first = true;
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		for (int i = 0; itr.hasNext(); ++i) {
			ExecutableMemberDoc mem = (ExecutableMemberDoc) itr.next();
			if (hyperref) {
				os.print("\\hyperlink{"
						+ refName(makeRefKey(mem.qualifiedName()
								+ mem.signature())) + "}{");
			}
			os.print(BOLD
					+ HTMLtoLaTeXBackEnd.fixText(mem.name()
							+ mem.flatSignature()) + "}");
			if (hyperref) {
				os.print("}");
			}
			os.print(" ");
			printTags(mem.firstSentenceTags());
			os.println("\\\\");
		}
		os.println("\\end{verse}");
		os.println("}");
	}

	/**
	 * Produces a field summary.
	 * 
	 * @param dmems
	 *            The fields to be summarized.
	 * @param title
	 *            The title of the section.
	 */
	static void printFieldSummary(FieldDoc[] dmems, String title) {
		if (dmems.length == 0) {
			return;
		}
		os.println("\\" + sectionLevels[2] + "{" + title + "}{");
		os.println("\\begin{verse}");
		boolean first = true;
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		for (int i = 0; itr.hasNext(); ++i) {
			FieldDoc mem = (FieldDoc) itr.next();
			if (hyperref) {
				os.print("\\hyperlink{"
						+ refName(makeRefKey(mem.qualifiedName())) + "}{");
			}
			os.print(BOLD + HTMLtoLaTeXBackEnd.fixText(mem.name()) + "}");
			if (hyperref) {
				os.print("}");
			}
			os.print(" ");
			printTags(mem.firstSentenceTags());
			os.println("\\\\");
		}
		os.println("\\end{verse}");
		os.println("}");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMembers(ClassDoc cd, ExecutableMemberDoc[] dmems,
			boolean labels) {
		if (dmems.length == 0) {
			return;
		}
		if (useHr) {
			os.println("\\rule[1em]{\\hsize}{2pt}\\vskip -2em");
		}
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		os.println("\\vskip -2em");
		os.println("\\begin{itemize}");
		for (int i = 0; itr.hasNext(); ++i) {
			ExecutableMemberDoc mem = (ExecutableMemberDoc) itr.next();

			if (i > 0) {
				if (useHr) {
					os.println("\\divideents{"
							+ HTMLtoLaTeXBackEnd.fixText(mem.name()) + "}");
				}
			}

			printMember(mem);
		}
		os.println("\\end{itemize}");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(ExecutableMemberDoc mem) {
		printMember(mem, null);
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(ExecutableMemberDoc mem,
			ExecutableMemberDoc copiedTo) {
		PackageDoc pac = copiedTo == null ? mem.containingPackage() : copiedTo
				.containingPackage();
		if (mem instanceof MethodDoc) {
			MethodDoc method = (MethodDoc) mem;
			if (method.commentText() == "" && method.seeTags().length == 0
					&& method.throwsTags().length == 0
					&& method.paramTags().length == 0) {

				// No javadoc available for this method. Recurse through
				// superclasses
				// and implemented interfaces to find javadoc of overridden
				// methods.

				boolean found = false;

				ClassDoc doc = method.overriddenClass();
				if (doc != null) {
					for (int i = 0; !found && i < doc.methods().length; ++i) {
						if (doc.methods()[i].name().equals(mem.name())
								&& doc.methods()[i].signature().equals(
										mem.signature())) {
							printMember(doc.methods()[i],
									copiedTo == null ? mem : copiedTo);
							found = true;
						}
					}
				}
				doc = method.containingClass();
				for (int j = 0; !found && j < doc.interfaces().length; j++) {
					ClassDoc inf = doc.interfaces()[j];
					for (int i = 0; !found && i < inf.methods().length; ++i) {
						if (inf.methods()[i].name().equals(mem.name())
								&& inf.methods()[i].signature().equals(
										mem.signature())) {
							printMember(inf.methods()[i],
									copiedTo == null ? mem : copiedTo);
							found = true;
						}
					}
				}
				if (found) {
					return;
				}
			}
		}

		ParamTag[] params = mem.paramTags();

		// Some index and hyperref stuff
		// os.println("\\item{\\vskip -1.9ex " );
		os.println("\\item{ ");
		os.println("\\index{"
				+ HTMLtoLaTeXBackEnd.fixText(mem.name() + mem.flatSignature())
				+ "}");

		// (S.M. modification)
		// If including the output this would result in hundreds of undefull
		// boxes ??!
		if (hyperref) {
			if (copiedTo == null) {
				os.print("\\hypertarget{"
						+ refName(makeRefKey(mem.qualifiedName()
								+ mem.signature())) + "}{");
			} else {
				os.print("\\hypertarget{"
						+ refName(makeRefKey(copiedTo.qualifiedName()
								+ copiedTo.signature())) + "}{");
			}
		}
		// (S.M. modification)
		// os.print("\\membername{"+HTMLtoLaTeXBackEnd.fixText(mem.name())+"}");
		os.print(BOLD + " " + HTMLtoLaTeXBackEnd.fixText(mem.name()) + "}\\\\");
		if (hyperref) {
			os.print("}");
		}
		os.println();

		// Print signature
		os.print("" + TRUETYPE + "");
		if (!mem.containingClass().isInterface()) {
			os.print(HTMLtoLaTeXBackEnd.fixText(mem.modifiers()));
		}
		if (mem instanceof MethodDoc) {
			os.print(" "
					+ HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(pac,
							((MethodDoc) mem).returnType().toString())));
		}
		os.print("\\ " + BOLD + " " + HTMLtoLaTeXBackEnd.fixText(mem.name())
				+ "}(");
		Parameter[] parms = mem.parameters();
		int p = 0;
		String qparmstr = "";
		String parmstr = "";
		for (; p < parms.length; ++p) {
			if (p > 0) {
				os.println(",");
			}
			Type t = parms[p].type();
			os.print(TRUETYPE
					+ HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(pac,
							t.qualifiedTypeName())));
			os.print(HTMLtoLaTeXBackEnd.fixText(t.dimension()) + "}");
			if (parms[p].name().equals("") == false) {
				os.print(" " + BOLD + " "
						+ HTMLtoLaTeXBackEnd.fixText(parms[p].name()) + "}");
			}
			if (qparmstr.length() != 0) {
				qparmstr += ",";
			}
			qparmstr += t.qualifiedTypeName() + t.dimension();
			if (parmstr.length() != 0) {
				parmstr += ",";
			}
			parmstr += t.typeName() + t.dimension();
		}
		os.print(")");

		// Thrown exceptions
		ClassDoc[] thrownExceptions = mem.thrownExceptions();
		if (thrownExceptions != null && thrownExceptions.length > 0) {
			os.print(" throws " + thrownExceptions[0].qualifiedName());
			for (int e = 1; e < thrownExceptions.length; e++) {
				os.print(", " + thrownExceptions[e].qualifiedName());
			}
		}
		os.println();

		if (copiedTo == null) {
			os.print("\\label{"
					+ refName(makeRefKey(mem.qualifiedName() + mem.signature()))
					+ "}");
		} else {
			os.print("\\label{"
					+ refName(makeRefKey(copiedTo.qualifiedName()
							+ copiedTo.signature())) + "}");
		}

		os.println("}%end signature");
		boolean yet = false;

		// Description
		if (mem.inlineTags().length > 0) {
			if (!yet) {
				os.println("\\begin{itemize}");
				// os.println("\\sld");
				yet = true;
			}
			os.println("\\item{");
			// os.println("\\sld");
			if (copiedTo == null) {
				// os.println(
				// ""+BOLD+" Description}\n  \\begin{itemize}\\isep\n   \\item{"
				// );
				os.println(BOLD + " Description}\n");
			} else {
				os.print(BOLD + " Description copied from ");
				String classname = mem.containingClass().qualifiedName();
				if (hyperref) {
					os.print("\\hyperlink{" + refName(makeRefKey(classname))
							+ "}{");
				}
				os.print(packageRelativIdentifier(pac, classname));
				if (hyperref) {
					os.print("}");
				}
				os.print("{\\small ");
				os.print("\\refdefined{" + refName(makeRefKey(classname)) + "}");
				// os.println( "} }\n  \\begin{itemize}\\isep\n   \\item{" );
				os.println("} }\n");
			}
			printTags(mem.inlineTags());
			// os.println( "}%end item\n  \\end{itemize}\n}" );
			os.println("\n}");
		}

		// Parameter tags
		if (params.length > 0) {
			if (!yet) {
				os.println("\\begin{itemize}");
				// os.println("\\sld");
				yet = true;
			}
			os.println("\\item{");
			// os.println("\\sld");
			os.println(BOLD + " Parameters}");
			// os.println("\\sld\\isep");
			os.println("  \\begin{itemize}");
			// os.println("\\sld\\isep");
			for (int j = 0; j < params.length; ++j) {
				os.println("   \\item{");
				// os.println("\\sld");
				os.print(TRUETYPE
						+ HTMLtoLaTeXBackEnd.fixText(params[j].parameterName())
						+ "}" + " -- ");
				printTags(params[j].inlineTags());
				os.println("}");
			}
			os.println("  \\end{itemize}");
			os.println("}%end item");
		}

		// Return tag
		if (mem instanceof MethodDoc) {
			Tag[] ret = mem.tags("return");
			if (ret.length > 0) {
				if (!yet) {
					os.println("\\begin{itemize}");
					// os.println("\\sld");
					yet = true;
				}
				os.println("\\item{" + BOLD + " Returns} -- ");
				for (int j = 0; j < ret.length; ++j) {
					printTags(ret[j].inlineTags());
					os.println(" ");
				}
				os.println("}%end item");
			}
		}

		// Throws or Exceptions tag
		if (mem instanceof ExecutableMemberDoc) {
			ThrowsTag[] excp = (mem).throwsTags();
			if (excp.length > 0) {
				if (!yet) {
					os.println("\\begin{itemize}");
					// os.println("\\sld");
					yet = true;
				}
				os.println("\\item{" + BOLD + " Throws}");
				os.println("  \\begin{itemize}");
				// os.println("\\sld");
				for (int j = 0; j < excp.length; ++j) {
					String ename = excp[j].exceptionName();
					ClassDoc cdoc = excp[j].exception();
					if (cdoc != null) {
						ename = cdoc.qualifiedName();
					}
					os.print("   \\item{\\vskip -.6ex " + TRUETYPE
							+ HTMLtoLaTeXBackEnd.fixText(ename) + "} -- ");
					printTags(excp[j].inlineTags());
					os.println("}");
				}
				os.println("  \\end{itemize}");
				os.println("}%end item");
			}
		}

		// See tags
		SeeTag[] sees = mem.seeTags();
		if (sees.length > 0) {
			if (!yet) {
				os.println("\\begin{itemize}");
				yet = true;
			}
			os.println("\\item{" + BOLD + " See also}");
			os.println("  \\begin{itemize}");
			for (int j = 0; j < sees.length; ++j) {
				os.print("\\item{ ");
				printSeesTag(sees[j], pac);
				os.println("}");
			}
			os.println("  \\end{itemize}");
			os.println("}%end item");
		}
		if (yet) {
			os.println("\\end{itemize}");
		}
		os.println("}%end item");
	}

	/**
	 * (S.M. modification)
	 */
	static void printInherited(ClassDoc par) {
		boolean members = false;

		MemberDoc[] inheritedmembers = new MemberDoc[par.fields().length
				+ par.methods().length];
		for (int k = 0; k < par.fields().length; k++) {
			inheritedmembers[k] = par.fields()[k];
			members = true;
		}
		for (int k = 0; k < par.methods().length; k++) {
			inheritedmembers[k + par.fields().length] = par.methods()[k];
			members = true;
		}

		// (S.M. modification) print only if members available (
		// for instance in case of a not correct set classpath
		// members are missed)
		if (members) {
			os.print("\\" + sectionLevels[2] + "{");
			// (S.M. modification) do not user true type and do
			// not print full name here
			// because this produces an ugly view in the Contents section
			// os.print("Members inherited from class "+TRUETYPE+""
			os.print("Members inherited from class "
					+ HTMLtoLaTeXBackEnd.fixText(
					// par.qualifiedName()
							par.name()) + " ");
			// par.name()) + "} \\textnormal{\\small");
			os.println("}{");
			// (S.M. modification)
			// ... but print here the full name including the
			// package and reference

			os.println(TRUETYPE + par.qualifiedName() + "} {\\small ");
			printRef(par.containingPackage(), par.name(), null);
			os.println("}");

			os.println("{\\small ");

			printinheritedMembers(par, inheritedmembers, false);

			os.println("}");

		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 * 
	 * @param mems
	 *            the members of this entity
	 * @see #start
	 */
	static void printinheritedMembers(ClassDoc cd, MemberDoc[] dmems,
			boolean labels) {
		if (dmems.length == 0) {
			return;
		}
		os.println();
		if (useHr) {
			os.println("\\rule[1em]{\\hsize}{2pt}\\vskip -2em");
		}
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();

		// (S.M. modification)
		if (shortInheritance) {

			for (int i = 0; itr.hasNext(); ++i) {
				MemberDoc mem = (MemberDoc) itr.next();

				// print only member names
				if (i != 0) {
					os.print(", ");
				}

				os.print(HTMLtoLaTeXBackEnd.fixText(mem.name()));

			}

		} else {

			os.println("\\vskip -2em");
			os.println("\\begin{itemize}");
			for (int i = 0; itr.hasNext(); ++i) {
				MemberDoc mem = (MemberDoc) itr.next();

				// Print signature

				// (S.M. modification)
				// os.println("\\item{\\vskip -1.9ex " );
				os.println("\\item{\\vskip -1.5ex ");
				os.print(TRUETYPE);
				os.print(HTMLtoLaTeXBackEnd.fixText(mem.modifiers()));
				if (mem instanceof MethodDoc) {
					os.print(" "
							+ HTMLtoLaTeXBackEnd.fixText(((MethodDoc) mem)
									.returnType().typeName()));
				}
				os.print(" " + BOLD + " "
						+ HTMLtoLaTeXBackEnd.fixText(mem.name()) + "}");
				if (mem instanceof MethodDoc) {
					os.print("(");
					Parameter[] parms = ((MethodDoc) mem).parameters();
					int p = 0;
					String qparmstr = "";
					String parmstr = "";
					for (; p < parms.length; ++p) {
						if (p > 0) {
							os.println(",");
						}
						Type t = parms[p].type();
						os.print(TRUETYPE

								+ HTMLtoLaTeXBackEnd
										.fixText(packageRelativIdentifier(
												mem.containingPackage(),
												t.qualifiedTypeName())));
						os.print(HTMLtoLaTeXBackEnd.fixText(t.dimension())
								+ "}");
						if (parms[p].name().equals("") == false) {
							os.print(" "
									+ BOLD
									+ " "
									+ HTMLtoLaTeXBackEnd.fixText(parms[p]
											.name()) + "}");
						}
						if (qparmstr.length() != 0) {
							qparmstr += ",";
						}
						qparmstr += t.qualifiedTypeName() + t.dimension();
						if (parmstr.length() != 0) {
							parmstr += ",";
						}
						parmstr += t.typeName() + t.dimension();
					}
					os.print(")");

					// Thrown exceptions
					if (mem instanceof ExecutableMemberDoc) {
						ClassDoc[] thrownExceptions = ((ExecutableMemberDoc) mem)
								.thrownExceptions();
						if (thrownExceptions != null
								&& thrownExceptions.length > 0) {
							os.print(" throws "
									+ packageRelativIdentifier(
											mem.containingPackage(),
											thrownExceptions[0].qualifiedName()));
							for (int e = 1; e < thrownExceptions.length; e++) {
								os.print(", "
										+ thrownExceptions[e].qualifiedName());
							}
						}
					}
					os.println();

					if (labels && qparmstr.startsWith("field") == false) {
						os.print("\\label{"
								+ refName(makeRefKey(cd.qualifiedName()
										+ "."
										+ mem.name()
										+ ((qparmstr.length() > 0) ? ("("
												+ qparmstr + ")") : ""))) + "}");
						os.print("\\label{"
								+ refName(makeRefKey(cd.name()
										+ "."
										+ mem.name()
										+ ((parmstr.length() > 0) ? ("("
												+ parmstr + ")") : ""))) + "}");
					}
				}

				os.println("}%end signature");
				boolean yet = false;

				if (yet) {
					os.println("\\end{itemize}");
				}
				os.println("}%end item");

			} // end for
			os.println("\\end{itemize}");
		} // shortInheritance
	}

	/**
	 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
	 */
	static void printTags(Tag[] tags) {
		String htmlstr = new String();

		for (int i = 0; i < tags.length; i++) {
			if (tags[i] instanceof SeeTag) {
				SeeTag link = (SeeTag) tags[i];

				String linkstr = "";
				if (link.referencedMember() != null) {
					if (link.referencedMember() instanceof ExecutableMemberDoc) {
						ExecutableMemberDoc m = (ExecutableMemberDoc) link
								.referencedMember();
						linkstr = m.qualifiedName() + m.signature();
					} else {
						linkstr = link.referencedMember().qualifiedName();
					}
				} else if (link.referencedClass() != null) {
					linkstr = link.referencedClass().qualifiedName();
				} else if (link.referencedPackage() != null) {
					linkstr = link.referencedPackage().name();
				}

				if (linkstr.equals("")) {
					htmlstr += link.text();
				} else {
					// Encapsulate the link in a "TEX" tag and let
					// HTMLtoLaTeXBackEnd.fixText handle the rest.
					htmlstr += "<TEX txt=\"";
					if (hyperref) {
						htmlstr += "\\hyperlink{"
								+ refName(makeRefKey(linkstr)) + "}{";
					}
					htmlstr += HTMLtoLaTeXBackEnd.fixText(link.label());
					if (hyperref) {
						htmlstr += "}";
					}
					htmlstr += "{\\small \n";
					htmlstr += "\\refdefined{" + refName(makeRefKey(linkstr))
							+ "}";
					htmlstr += "}";
					htmlstr += "\"></TEX>";
				}
			} else {
				htmlstr += tags[i].text();
			}
		}

		os.print(HTMLtoLaTeXBackEnd.fixText(htmlstr));
	}

	/**
	 * Prints a reference to a package, class or member.
	 */
	static void printRef(PackageDoc pd, String cls, String mem) {
		String pkg = "";
		if (pd != null) {
			pkg = pd.name() + ".";
		}

		String lbl = pkg + cls;
		if (mem != null && mem.equals("") == false) {
			lbl += "." + mem;
		}
		os.print("\\refdefined{" + refName(makeRefKey(lbl)) + "}");
	}

	/**
	 * Should be removed.
	 */
	static String makeRefKey(String key) {
		return key;
	}

	/**
	 * Returns a package relative identifier.
	 * 
	 * @param doc
	 *            The package the identifier should be relative to.
	 * @param str
	 *            The identifier to be made relative.
	 */
	static String packageRelativIdentifier(PackageDoc doc, String str) {
		if (str.startsWith(doc.name() + ".")) {
			return str.substring(doc.name().length() + 1);
		} else {
			return str;
		}
	}

	/**
	 * Prints a "see also" tag.
	 * 
	 * @param tag
	 *            The "see also" tag to print.
	 * @param relativeTo
	 *            The package to which the see tag should be relative to.
	 */
	static void printSeesTag(SeeTag tag, PackageDoc relativeTo) {
		String memName = "";
		String memText = "";
		if (tag.referencedMember() != null) {
			if (tag.referencedMember() instanceof ExecutableMemberDoc) {
				ExecutableMemberDoc m = (ExecutableMemberDoc) tag
						.referencedMember();
				memName = m.qualifiedName() + m.signature();
				memText = HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(
						relativeTo, m.qualifiedName()));
				memText += "(";
				for (int i = 0; i < m.parameters().length; i++) {
					memText += HTMLtoLaTeXBackEnd
							.fixText(packageRelativIdentifier(relativeTo,
									m.parameters()[i].typeName()));
					if (i < m.parameters().length - 1) {
						memText += ",\\allowbreak ";
					}
				}
				memText += ")";
			} else {
				memName = tag.referencedMember().qualifiedName();
				memText = HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(
						relativeTo, memName));
			}

		} else if (tag.referencedClass() != null) {
			memName = tag.referencedClass().qualifiedName();
			memText = HTMLtoLaTeXBackEnd.fixText(packageRelativIdentifier(
					relativeTo, memName));
		} else if (tag.referencedPackage() != null) {
			memName = tag.referencedPackage().name();
			memText = HTMLtoLaTeXBackEnd.fixText(memName);
		}

		if (memName.equals("") == false) {
			os.print(TRUETYPE);
			if (hyperref) {
				os.print("\\hyperlink{" + refName(makeRefKey(memName)) + "}{");
			}
			// os.print(HTMLtoLaTeXBackEnd.fixText(memText));
			// System.out.println("see also link : " + memText);
			os.print(memText);
			if (hyperref) {
				os.print("}");
			}
			os.println("} {\\small ");
			os.print("\\refdefined{" + refName(makeRefKey(memName)) + "}");
			os.println("}%end");
		} else {
			os.print(HTMLtoLaTeXBackEnd.fixText(tag.text()));
		}
	}

	/**
	 * In my opinion a PackageDoc should be able to tell where the source-files
	 * defining the package were lifted from. The standard doclet uses this
	 * directory to locate "doc-files" and copy them. Instead we have to attempt
	 * to replicate the search procedure doclet API in order to locate the
	 * appropriate directory. The following is not fool proof...
	 */
	static File findPackageDir(String pkg, RootDoc root) {
		File f = null;

		String sourcepath = null;
		for (int i = 0; i < root.options().length; i++) {
			if (root.options()[i][0].equalsIgnoreCase("-sourcepath")) {
				sourcepath = root.options()[i][1];
			}
		}

		if (sourcepath == null) {
			sourcepath = ".";
		} else if (sourcepath.startsWith("\"")) {
			sourcepath = sourcepath.substring(1);
			if (sourcepath.endsWith("\"")) {
				sourcepath = sourcepath.substring(0, sourcepath.length() - 1);
			}
		}

		StringTokenizer sourcepathToken = new StringTokenizer(sourcepath, ";");
		while (sourcepathToken.hasMoreTokens()) {
			String token = sourcepathToken.nextToken();
			if (token.equals(".")) {
				f = null;
			} else {
				f = new File(token);
			}

			if (pkg != null && !pkg.equals("")) {
				StringTokenizer st = new StringTokenizer(pkg, ".");
				while (st.hasMoreTokens()) {
					f = new File(f, st.nextToken());
				}
			}

			if (f.exists()) {
				// This test is necessary so we don't terminate prematurely.
				// It is, however, not sufficient.
				File[] javasourcefiles = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File file, String filename) {
						return filename.toLowerCase().endsWith(".java");
					}
				});
				if (javasourcefiles.length > 0) {
					return f;
				}
			}
		}

		return null;
	}
}