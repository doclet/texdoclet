package org.stfm.texdoclet;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import com.github.rjeschke.txtmark.Processor;
import com.keypoint.PngEncoder;

/**
 * <p>
 * This class implements a <CODE>ParserCallback</CODE> that translates HTML to
 * the corresponding <TEX txt="\LaTeX{}">LaTeX</TEX>. Not all tags a processed
 * but the most common are.
 * <p>
 * HTML links to files located in the doc-files directory (<a
 * href="doc-files/appendix_a.html">appendix_a.html</a>, <a
 * href="doc-files/appendix_b.txt">appendix_b.txt</a>) are transformed to
 * references to the appendix, whereby the referenced files itself are included
 * in the appendix.
 * 
 * @see javax.swing.text.html.parser.ParserDelegator
 * @author Soeren Caspersen
 */
public class HTMLtoLaTeXBackEnd extends HTMLEditorKit.ParserCallback {

	private static final String MARKDOWN1 = "md";
	private static final String MARKDOWN2 = "markdown";

	private static final String IMAGES_DIR = "texdoclet_images";

	/**
	 * Buffer containing the translated HTML.
	 */
	StringBuffer ret;

	Stack<TableInfo> tblstk = new Stack<TableInfo>();
	TableInfo tblinfo;
	int verbat = 0;
	int colIdx = 0;
	Hashtable<String, String> colors = new Hashtable<String, String>(10);
	String block = "";
	String refurl = null;
	String doPrintURL = null;
	String refname = null;
	String refimg = null;
	boolean notex = false;
	int imageindex = 0;
	boolean inPreMarkdown = false;

	/**
	 * Constructs a new instance.
	 * 
	 * @param stringBuffer
	 *            The <CODE>StringBuffer</CODE> where the translated HTML is
	 *            appended.
	 */
	public HTMLtoLaTeXBackEnd(StringBuffer stringBuffer) {
		this.ret = stringBuffer;
	}

	/**
	 * This method handles simple HTML tags (e.g. <CODE>&lt;HR&gt;</CODE>-tags).
	 * It is called by the parser whenever such a tag is encountered.
	 */
	@Override
	public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attrSet,
			int pos) {
		if (tag.toString().equalsIgnoreCase("tex")) {
			if (attrSet.containsAttribute(HTML.Attribute.ENDTAG, "true")) {
				notex = false;
			} else {
				String tex = (String) attrSet.getAttribute("txt");
				ret.append(tex);
				notex = true;
			}
		} else if (notex) {
			return;
		} else if (tag == HTML.Tag.META) {
		} else if (tag == HTML.Tag.HR) {
			String sz = (String) attrSet.getAttribute(HTML.Attribute.SIZE);
			int size = 1;
			if (sz != null) {
				size = Integer.parseInt(sz);
			}
			ret.append("\\mbox{}\\newline\\rule[2mm]{\\hsize}{"
					+ (1 * size * .5) + "mm}\\newline\n");
		} else if (tag == HTML.Tag.BR) {
			ret.append("\\mbox{}\\newline ");
		} else if (tag == HTML.Tag.IMG) {
			String refimg = (String) attrSet.getAttribute(HTML.Attribute.SRC);

			if (refimg.indexOf("://") != -1) {

				// if (refimg.indexOf("http://") == 0) {
				// make link
				ret.append("(see image at "
						+ fixText("<a href=\"" + refimg + "\">" + refimg
								+ "</a>") + ")");
				// } else {
				// skip it
				// }

			} else {

				new File(IMAGES_DIR).mkdir();

				double scale = 1.0;
				System.out.println("Package dir: " + TeXDoclet.packageDir);
				System.out.println("Referenced image: " + refimg);
				File imgF = new File(TeXDoclet.packageDir, refimg);
				if (!imgF.exists()) {
					System.err.println("Image not found: " + TeXDoclet.packageDir + "/" + refimg);
					ret.append("(image file not found)");
					return;
				}

				String imgfile = new File(TeXDoclet.packageDir, refimg)
						.getAbsolutePath();

				ImageIcon icn = new ImageIcon(imgfile);

				int width = icn.getIconWidth();
				int height = icn.getIconHeight();

				String sw = (String) attrSet.getAttribute(HTML.Attribute.WIDTH);
				String sh = (String) attrSet
						.getAttribute(HTML.Attribute.HEIGHT);

				try {
					if (sw != null) {
						scale = NumberFormat.getPercentInstance(Locale.ENGLISH).parse(sw)
								.doubleValue();
					} else if (sh != null) {
						scale = NumberFormat.getPercentInstance().parse(sh)
								.doubleValue();
					}
				} catch (ParseException er) {
					er.printStackTrace();
				}

				Image img = icn.getImage();

				PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height,
						true);
				try {
					pg.grabPixels();
				} catch (InterruptedException e) {
					throw new RuntimeException(
							"interrupted waiting for pixels!");
				}
				int[] pixels = (int[]) pg.getPixels();
				img = Toolkit.getDefaultToolkit().createImage(
						new MemoryImageSource(width, height, pixels, 0, width));
				byte[] pngbytes;
				PngEncoder png = new PngEncoder(img, true);

				String filnavn = IMAGES_DIR + "/pngimage" + imageindex++
						+ ".png";
				try {
					FileOutputStream outfile = new FileOutputStream(filnavn);
					pngbytes = png.pngEncode();
					if (pngbytes != null) {
						outfile.write(pngbytes);
					}
					outfile.flush();
					outfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (width * scale <= 800) {
					width *= scale * 0.5;
					height *= scale * 0.5;
				} else {
					scale = width * scale / 800;
					width *= 1.0 / scale * 0.5;
					height *= 1.0 / scale * 0.5;
				}

				String fs = System.getProperty("file.separator");
				String filnavnFinal = (TeXDoclet.imagesPath == null ? ""
						: TeXDoclet.imagesPath
								+ (TeXDoclet.imagesPath.endsWith(fs) ? "" : fs))
						+ filnavn;

				ret.append("\\mbox{\\includegraphics[width=" + width
						+ "pt, height=" + height + "pt]{" + filnavnFinal + "}}");

			}

		}
	}

	/**
	 * This method handles HTML tags that mark a beginning (e.g.
	 * <CODE>&lt;P&gt;</CODE>-tags). It is called by the parser whenever such a
	 * tag is encountered.
	 */
	@Override
	public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrSet,
			int pos) {
		if (notex) {
			return;
		} else if (tag == HTML.Tag.PRE) {
			if (attrSet.containsAttribute("format", MARKDOWN1)
					|| attrSet.containsAttribute("format", MARKDOWN2)) {
				inPreMarkdown = true;
			} else {
				ret.append(TeXDoclet.TRUETYPE + "\\small\n\\mbox{}\\newline ");
				verbat++;
			}
		} else if (tag == HTML.Tag.H1) {
			ret.append("\\chapter*{");
		} else if (tag == HTML.Tag.H2) {
			ret.append("\\section*{");
		} else if (tag == HTML.Tag.H3) {
			ret.append("\\subsection*{");
		} else if (tag == HTML.Tag.H4) {
			ret.append("\\subsubsection*{");
		} else if (tag == HTML.Tag.H5) {
			ret.append("\\subsubsection*{");
		} else if (tag == HTML.Tag.H6) {
			ret.append("\\subsubsection*{");
		} else if (tag == HTML.Tag.SUB) {
			ret.append("$_{");
		} else if (tag == HTML.Tag.SUP) {
			ret.append("$^{");
			// } else if (tag == HTML.Tag.HTML) {
		} else if (tag == HTML.Tag.HEAD) {
		} else if (tag == HTML.Tag.CENTER) {
			ret.append("\\makebox[\\hsize]{ ");
		} else if (tag == HTML.Tag.TITLE) {
			ret.append("\\chapter{");
		} else if (tag == HTML.Tag.FORM) {
		} else if (tag == HTML.Tag.INPUT) {
		} else if (tag == HTML.Tag.BODY) {
		} else if (tag == HTML.Tag.CODE) {
			ret.append(TeXDoclet.TRUETYPE + "\\small ");
		} else if (tag == HTML.Tag.TT) {
			ret.append(TeXDoclet.TRUETYPE + " ");
		} else if (tag == HTML.Tag.P) {
			ret.append("\n\n");
		} else if (tag == HTML.Tag.B) {
			ret.append("{\\bf ");
		} else if (tag == HTML.Tag.STRONG) {
			ret.append("{\\bf ");
		} else if (tag == HTML.Tag.A) {
			refurl = (String) attrSet.getAttribute(HTML.Attribute.HREF);
			doPrintURL = (String) attrSet.getAttribute("doprinturl");
			if (refurl != null) {
				if (TeXDoclet.hyperref) {
					if (refurl.toLowerCase().startsWith("doc-files")) {
						File file = new File(TeXDoclet.packageDir, refurl);
						if (file.exists()) {
							if (TeXDoclet.appendencies.contains(file.getPath())) {
								refurl = TeXDoclet.appendencies.get(file
										.getPath());
							} else {
								refurl = "appendix"
										+ new Integer(
												TeXDoclet.appendencies.size() + 1);
								TeXDoclet.appendencies.put(file.getPath(),
										refurl);
							}
							ret.append("{");
							return;
						}
					} else {

						String sharp = "";
						if (refurl.indexOf("#") >= 0) {
							sharp = refurl.substring(refurl.indexOf("#") + 1,
									refurl.length());
							if (sharp.indexOf("%") >= 0) {
								sharp = ""; // Don't know what to do with '%'
							}
							refurl = refurl.substring(0, refurl.indexOf("#"));
						}
						ret.append("\\hyperref{" + refurl + "}{" + sharp
								+ "}{}{");
						// ret.append("\\href{" + refurl + "}{");
					}
				} else {
					ret.append("{\\bf ");
				}
			} else {
				refname = (String) attrSet.getAttribute(HTML.Attribute.NAME);
				if (refname != null && TeXDoclet.hyperref) {
					ret.append("\\hyperdef{" + refname + "}{");
				}
			}

		} else if (tag == HTML.Tag.OL) {
			ret.append("\n\\begin{enumerate}");
		} else if (tag == HTML.Tag.DL) {
			ret.append("\n\\begin{itemize}");
		} else if (tag == HTML.Tag.LI) {
			ret.append("\n\\item{\\vskip -.8ex ");
		} else if (tag == HTML.Tag.DT) {
			ret.append("\\item[");
		} else if (tag == HTML.Tag.DD) {
			ret.append("{");
		} else if (tag == HTML.Tag.UL) {
			ret.append("\\begin{itemize}");
		} else if (tag == HTML.Tag.I) {
			ret.append(TeXDoclet.ITALIC + " ");
		} else if (tag == HTML.Tag.EM) {
			ret.append(TeXDoclet.ITALIC + " ");
		} else if (tag == HTML.Tag.TABLE) {
			tblstk.push(tblinfo);
			tblinfo = new TableInfo();
			ret = tblinfo.startTable(ret, attrSet);
		} else if (tag == HTML.Tag.TH) {
			tblinfo.startHeadCol(attrSet);
		} else if (tag == HTML.Tag.TD) {
			tblinfo.startCol(attrSet);
		} else if (tag == HTML.Tag.TR) {
			tblinfo.startRow(attrSet);
		} else if (tag == HTML.Tag.FONT) {
			// String sz = (String) attrSet.getAttribute(HTML.Attribute.SIZE);
			String col = (String) attrSet.getAttribute(HTML.Attribute.COLOR);
			ret.append("{");
			if (col != null) {
				if ("redgreenbluewhiteyellowblackcyanmagenta".indexOf(col) != -1) {
					ret.append("\\color{" + col + "}");
				} else {
					if ("abcdefABCDEF0123456789".indexOf(col.charAt(0)) != -1) {
						Color cc = new Color((int) Long.parseLong(col, 16));
						String name = colors.get("color" + cc.getRGB());
						if (name == null) {
							ret.append("\\definecolor{color" + colIdx
									+ "}[rgb]{" + (cc.getRed() / 255.0) + ","
									+ (cc.getBlue() / 255.0) + ","
									+ (cc.getGreen() / 255.0) + "}");
							name = "color" + colIdx;
							colIdx++;
							colors.put("color" + cc.getRGB(), name);
						}
						ret.append("\\color{" + name + "}");
						++colIdx;
					}
				}
			}
		}
	}

	/**
	 * This method handles HTML tags that mark an ending (e.g.
	 * <CODE>&lt;/P&gt;</CODE>-tags). It is called by the parser whenever such a
	 * tag is encountered.
	 */
	@Override
	public void handleEndTag(HTML.Tag tag, int pos) {
		if (notex) {
			return;
		} else if (tag == HTML.Tag.PRE) {
			if (!inPreMarkdown) {
				verbat--;
				ret.append("}\n");
			} else {
				inPreMarkdown = false;
			}
		} else if (tag == HTML.Tag.H1) {
			ret.append("}");
		} else if (tag == HTML.Tag.H2) {
			ret.append("}");
		} else if (tag == HTML.Tag.H3) {
			ret.append("}");
		} else if (tag == HTML.Tag.H4) {
			ret.append("}");
		} else if (tag == HTML.Tag.H5) {
			ret.append("}");
		} else if (tag == HTML.Tag.H6) {
			ret.append("}");
		} else if (tag == HTML.Tag.SUB) {
			ret.append("}$");
		} else if (tag == HTML.Tag.SUP) {
			ret.append("}$");
			// } else if (tag == HTML.Tag.HTML) {
		} else if (tag == HTML.Tag.HEAD) {
		} else if (tag == HTML.Tag.CENTER) {
			ret.append("}");
		} else if (tag == HTML.Tag.TITLE) {
			ret.append("}{");
		} else if (tag == HTML.Tag.FORM) {
		} else if (tag == HTML.Tag.INPUT) {
		} else if (tag == HTML.Tag.BODY) {
		} else if (tag == HTML.Tag.CODE) {
			ret.append("}");
		} else if (tag == HTML.Tag.TT) {
			ret.append("}");
		} else if (tag == HTML.Tag.P) {
		} else if (tag == HTML.Tag.B) {
			ret.append("}");
		} else if (tag == HTML.Tag.STRONG) {
			ret.append("}");
		} else if (tag == HTML.Tag.A) {
			if (refurl != null) {

				if (refurl.startsWith("appendix")) {

					ret.append("\\refdefined{" + refurl + "}");
					ret.append("}");

					return;

				}
				ret.append("}");
				if (doPrintURL != null) {
					if (!refurl.equals("")) {
						ret.append("(at ");
						ret.append(fixText(refurl));
						ret.append(")");
					}
				}

			} else if (refname != null) {
				ret.append("}");
			}

		} else if (tag == HTML.Tag.LI) {
			ret.append("}");
		} else if (tag == HTML.Tag.DT) {
			ret.append("]");
		} else if (tag == HTML.Tag.DD) {
			ret.append("}");
		} else if (tag == HTML.Tag.DL) {// /
			ret.append("\n\\end{itemize}\n");
		} else if (tag == HTML.Tag.OL) {
			ret.append("\n\\end{enumerate}\n");
		} else if (tag == HTML.Tag.UL) {
			ret.append("\n\\end{itemize}\n");
		} else if (tag == HTML.Tag.I) {
			ret.append("}");
		} else if (tag == HTML.Tag.EM) {
			ret.append("}");
		} else if (tag == HTML.Tag.TABLE) {
			ret = tblinfo.endTable();
			tblinfo = tblstk.pop();
		} else if (tag == HTML.Tag.TH) {
			tblinfo.endCol();
		} else if (tag == HTML.Tag.TD) {
			tblinfo.endCol();
		} else if (tag == HTML.Tag.TR) {
			tblinfo.endRow();
		} else if (tag == HTML.Tag.FONT) {
			ret.append("}");
		}
	}

	/**
	 * This method handles all other text.
	 */
	@Override
	public void handleText(char[] data, int pos) {
		String str = new String(data);

		if (inPreMarkdown) {

			String html = "";

			// usually java documentation has a leading space character in each
			// line
			// that is to remove for markdown processing !
			if (str.startsWith(" ")) {
				str = removeLeadingSpaces(str);
			}

			// test some Markdown processors here :

			// 1. MarkdownJ

			// MarkdownProcessor m = new MarkdownProcessor();
			// html = m.markdown(str);

			// 2. PegDown

			// PegDownProcessor pp = new PegDownProcessor();
			// html = pp.markdownToHtml(str);

			// 3. MarkdownPapers

			// Markdown md = new Markdown();
			// StringWriter sw = new StringWriter();
			// try {
			// md.transform(new StringReader(str), sw);
			// } catch (org.tautua.markdownpapers.parser.ParseException e) {
			// e.printStackTrace();
			// }
			// html = sw.toString();

			// 4. Txtmark

			html = Processor.process(str);

			String toAppend = HTMLtoLaTeXBackEnd.fixText(html);
			ret.append(toAppend);
			return;
		}

		for (int i = 0; i < str.length(); ++i) {
			int c = str.charAt(i);
			if (notex) {
				continue;
			}
			switch (c) {
			case 160: // &nbsp;
				ret.append("\\phantom{ }");
				break;
			case ' ':
				if (verbat > 0) {
					ret.append("\\phantom{ }");
				} else {
					ret.append(' ');
				}
				break;
			case '[':
				if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
					ret.append("\\lbrack\\ ");
					i++;
				} else {
					ret.append("\\lbrack ");
				}
				break;
			case ']':
				if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
					ret.append("\\rbrack\\ ");
					i++;
				} else {
					ret.append("\\rbrack ");
				}
				break;
			case '_':
			case '%':
			case '$':
			case '#':
			case '}':
			case '{':
			case '&':
				ret.append('\\');
				ret.append((char) c);
				if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
					ret.append("\\ ");
					i++;
				}
				break;
			// case 0xc38a:
			case 0xc3a6:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\ae\\ ");
						i++;
					} else {
						ret.append("\\ae ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			case 0xc386:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\AE\\ ");
						i++;
					} else {
						ret.append("\\AE ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			// case 0xc382:
			case 0xc3a5:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\aa\\ ");
						i++;
					} else {
						ret.append("\\aa ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			case 0xc385:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\AA\\ ");
						i++;
					} else {
						ret.append("\\AA ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			// case 0xc2af:
			case 0xc3b8:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\o\\ ");
						i++;
					} else {
						ret.append("\\o ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			// case 0xc3bf:
			case 0xc398:
				if (Charset.defaultCharset().name().equals("UTF-8")) {
					if (i < str.length() - 1 && str.charAt(i + 1) == ' ') {
						ret.append("\\O\\ ");
						i++;
					} else {
						ret.append("\\O ");
					}
				} else {
					ret.append((char) c);
				}
				break;
			case '^':
				ret.append("$\\wedge$");
				break;
			case '<':
				ret.append("\\textless ");
				break;
			case '\r':
			case '\n':
				if (tblstk.size() > 0) {
					// Swallow new lines while tables are in progress,
					// <tr> controls new line emission.
					if (verbat > 0) {
						ret.append("}\\mbox{}\\newline\n" + TeXDoclet.TRUETYPE
								+ "\\small ");
					} else {
						ret.append(" ");
					}
				} else {
					if (verbat > 0) {
						ret.append("}\\mbox{}\\newline\n" + TeXDoclet.TRUETYPE
								+ "\\small ");
					} else if ((i + 1) < str.length()
							&& str.charAt(i + 1) == 10) {
						ret.append("\\bl ");
						++i;
					} else {
						ret.append((char) c);
					}
				}
				break;
			case '/':
				ret.append("/");
				break;
			case '>':
				ret.append("\\textgreater ");
				break;
			case '\\':
				ret.append("\\textbackslash ");
				break;
			default:
				ret.append((char) c);
				break;
			}
		}
	}

	/**
	 * Converts a HTML string into <TEX txt="\LaTeX{}">LaTeX</TEX> using an
	 * instance of <CODE>HTMLtoLaTeXBackEnd</CODE>.
	 * 
	 * @return The converted string.
	 */
	public static String fixText(String str) {
		// System.out.println("fixText: " + str);
		StringBuffer result = new StringBuffer(str.length());
		HTMLtoLaTeXBackEnd b = new HTMLtoLaTeXBackEnd(result);
		Reader reader = new StringReader(str);
		try {
			new ParserDelegator().parse(reader, b, false);
		} catch (IOException e) {
			System.err.println("Caught exception when converting HTML text to TEX: " + e.getMessage());
		}
		return new String(result);
	}

	private String removeLeadingSpaces(String str) {
		StringBuffer sb = new StringBuffer();
		try (Scanner scanner = new Scanner(str)) {
			while (scanner.hasNextLine()) {
				String l = scanner.nextLine();
				if (l.startsWith(" ")) {
					sb.append(l.substring(1) + System.getProperty("line.separator"));
				} else {
					sb.append(l + System.getProperty("line.separator"));
				}
			}
			return sb.toString();
		}
	}

}
