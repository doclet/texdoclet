package org.stfm.texdoclet;

import java.util.Properties;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;

/**
 * This class provides support for converting HTML tables into <TEX
 * txt="\LaTeX{}">LaTeX</TEX> tables. Some of the things <b>NOT</b> implemented
 * include the following:
 * <ul>
 * <li>valign attributes are not processed, but align= is.
 * <li>rowspan attributes are not processed, but colspan= is.
 * <li>the argument to border= in the table tag is not used to control line size
 * </ul>
 * <br>
 * Here is an example table.
 * <p>
 * <table border bgcolor="#AAAAAA">
 * <tr>
 * <th>Column 1 Heading
 * <th>Column two heading
 * <th>Column three heading
 * <tr>
 * <td>data
 * <td colspan=2>Span two columns
 * <tr>
 * <td><i>more data</i>
 * <td align=right>right
 * <td align=left>left
 * <tr>
 * <td colspan=3>
 * <table border=5 bgcolor="#CCCCCC">
 * <tr>
 * <th colspan=3>A nested table example
 * <tr>
 * <th>Column 1 Heading</th>
 * <th>Coludliadfuapfd a fia fopia foipapio dupoau foapoifd pdpfiu apsd
 * oipoioaofiduaopiufopiiiiiiiiiimn two heading</th>
 * <th>Column three heading</th>
 * <tr>
 * <td>data</td>
 * <td colspan=2>Span two columns</td>
 * <tr>
 * <td><i>more data</i></td>
 * <td align=right>right</td>
 * <td align=left>left</td>
 * <tr>
 * <td>
 * 
 * <pre>
 *    1
 *  2
 *  3
 *  4
 * </pre>
 * 
 * </td>
 * <td>
 * 
 * <pre>
 *    first line
 *  second line
 *  third line
 *  fourth line
 * </pre>
 * 
 * </td>
 * </table>
 * </table>
 * 
 * @version $Revision: 1.2 $
 * @author Gregg Wonderly - C2 Technologies Inc.
 */
public class TableInfo {

	private StringBuffer originalBuffer;
	private StringBuffer ret;

	private int colcnt = 0;
	private int rowcnt = 0;
	private int totalcolcnt = 0;
	private boolean border = false;
	private Properties props;
	private int bordwid;
	private boolean parboxed;
	private double red = -1.0;
	private double blue = -1.0;
	private double green = -1.0;
	static int tblcnt;
	int tblno;
	String tc;

	HTML.Tag lastTag;

	int hasNumAttr(HTML.Attribute attr, MutableAttributeSet attrSet) {
		String val = (String) attrSet.getAttribute(attr);
		if (val == null) {
			return -1;
		}
		try {
			return Integer.parseInt(val);
		} catch (Exception ex) {
			return -1;
		}
	}

	/**
	 * Constructs a new table object and starts processing of the table by
	 * scanning the <code>&lt;table&gt;</code> passed to count columns.
	 * 
	 * @param p
	 *            properties found on the <code>&lt;table&gt;</code> tag
	 * @param ret
	 *            the result buffer that will contain the output
	 * @param table
	 *            the input string that has the entire table definition in it.
	 * @param off
	 *            the offset into <code>&lt;table&gt;</code> where scanning
	 *            should start
	 */
	public StringBuffer startTable(StringBuffer org, MutableAttributeSet attrSet) {
		originalBuffer = org;
		ret = new StringBuffer();

		tblno = tblcnt++;
		tc = "" + (char) ('a' + (tblno / (26 * 26)))
				+ (char) ((tblno / 26) + 'a') + (char) ((tblno % 26) + 'a');

		String val = (String) attrSet.getAttribute(HTML.Attribute.BORDER);
		border = false;
		if (val != null) {
			border = true;
			bordwid = 2;
			if (val.equals("") == false) {
				try {
					bordwid = Integer.parseInt(val);
				} catch (Exception ex) {
				}
				if (bordwid == 0) {
					border = false;
				}
			}
		}
		String bgcolor = (String) attrSet.getAttribute(HTML.Attribute.BGCOLOR);
		if (bgcolor != null) {
			try {
				if (bgcolor.length() != 7 && bgcolor.charAt(0) == '#') {
					throw new NumberFormatException();
				}
				red = Integer.decode("#" + bgcolor.substring(1, 3))
						.doubleValue();
				blue = Integer.decode("#" + bgcolor.substring(3, 5))
						.doubleValue();
				green = Integer.decode("#" + bgcolor.substring(5, 7))
						.doubleValue();
				red /= 255.0;
				blue /= 255.0;
				green /= 255.0;
			} catch (NumberFormatException e) {
				red = 1.0;
				blue = 1.0;
				green = 1.0;
			}
		}

		return ret;
	}

	/**
	 * Ends the table, closing the last row as needed
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeXe{}">LaTeX2e</TEX>
	 *            into.
	 */
	public StringBuffer endTable() {
		originalBuffer.append("\n% Table #" + tblno + "\n");
		int col = totalcolcnt;
		if (col == 0) {
			col = 1;
		}
		for (int i = 0; i < col; ++i) {
			String cc = "" + (char) ('a' + (i / (26 * 26)))
					+ (char) ((i / 26) + 'a') + (char) ((i % 26) + 'a');
			originalBuffer.append("\\newlength{\\tbl" + tc + "c" + cc + "w}\n");
			// originalBuffer.append("\\setlength{\\tbl"+tc+"c"+cc+"w}{"+(1.0/col)+"\\hsize}\n");
			originalBuffer.append("\\setlength{\\tbl" + tc + "c" + cc + "w}{"
					+ (1.0 / col) + "\\linewidth}\n");
		}
		if (red != -1.0 && green != -1.0 && blue != -1.0) {
			originalBuffer.append("\\colorbox[rgb]{" + Double.toString(red)
					+ "," + Double.toString(blue) + ","
					+ Double.toString(green) + "}{");
		}
		originalBuffer.append("\\begin{tabular}{");
		if (border) {
			originalBuffer.append("|");
		}
		for (int i = 0; i < col; ++i) {
			String cc = "" + (char) ('a' + (i / (26 * 26)))
					+ (char) ((i / 26) + 'a') + (char) ((i % 26) + 'a');
			originalBuffer.append("p{\\tbl" + tc + "c" + cc + "w}");
			if (border) {
				originalBuffer.append("|");
			}
		}
		originalBuffer.append("}\n");

		// Append the cached table
		originalBuffer.append(ret);

		originalBuffer.append("\\end{tabular}\n");
		if (red != -1.0 && green != -1.0 && blue != -1.0) {
			originalBuffer.append("}\n");
		}

		return originalBuffer;
	}

	/**
	 * Starts a new column, possibly closing the current column if needed
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeXe{}">LaTeX2e</TEX>
	 *            into.
	 * @param p
	 *            the properties from the <code>&lt;td&gt;</code> tag
	 */
	public void startCol(MutableAttributeSet attrSet) {
		int span = hasNumAttr(HTML.Attribute.COLSPAN, attrSet);
		if (colcnt > 0) {
			ret.append(" & ");
		}
		String align = (String) attrSet.getAttribute(HTML.Attribute.ALIGN);
		if (align != null && span < 0) {
			span = 1;
		}
		if (span > 0) {
			ret.append("\\multicolumn{" + span + "}{");
			if (border && colcnt == 0) {
				ret.append("|");
			}
			String cc = "" + (char) ('a' + (colcnt / (26 * 26)))
					+ (char) ((colcnt / 26) + 'a')
					+ (char) ((colcnt % 26) + 'a');
			if (align != null) {
				String h = align.substring(0, 1);
				if ("rR".indexOf(h) >= 0) {
					ret.append("r");
				} else if ("lL".indexOf(h) >= 0) {
					ret.append("p{\\tbl" + tc + "c" + cc + "w}");
				} else if ("cC".indexOf(h) >= 0) {
					ret.append("p{\\tbl" + tc + "c" + cc + "w}");
				}
			} else {
				ret.append("p{\\tbl" + tc + "c" + cc + "w}");
			}
			if (border) {
				ret.append("|");
			}
			ret.append("}");
		}
		String wid = (String) attrSet.getAttribute("texwidth");
		ret.append("{");
		if (wid != null) {
			ret.append("\\parbox{" + wid + "}{\\vskip 1ex ");
			parboxed = true;
		}
		colcnt++;
		totalcolcnt = totalcolcnt > colcnt ? totalcolcnt : colcnt;
	}

	/**
	 * Starts a new Heading column, possibly closing the current column if
	 * needed. A Heading column has a Bold Face font directive around it.
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeXe{}">LaTeX2e</TEX>
	 *            into.
	 * @param p
	 *            The properties from the <code>&lt;th&gt;</code> tag
	 */
	public void startHeadCol(MutableAttributeSet attrSet) {
		startCol(attrSet);
		ret.append("\\bf ");
	}

	/**
	 * Ends the current column.
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeXe{}">LaTeX2e</TEX>
	 *            into.
	 */
	public void endCol() {
		if (parboxed) {
			ret.append("\\vskip 1ex}");
		}
		parboxed = false;
		ret.append("}");
	}

	/**
	 * Starts a new row, possibly closing the current row if needed
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeX{}">LaTeX</TEX> into.
	 * @param p
	 *            The properties from the <code>&lt;tr&gt;</code> tag
	 */
	public void startRow(MutableAttributeSet attrSet) {
		if (rowcnt == 0) {
			if (border) {
				ret.append(" \\hline ");
			}
		}
		colcnt = 0;
		++rowcnt;
	}

	/**
	 * Ends the current row.
	 * 
	 * @param ret
	 *            The output buffer to put <TEX txt="\LaTeXe{}">LaTeX2e</TEX>
	 *            into.
	 */
	public void endRow() {
		ret.append(" \\\\");
		if (border) {
			ret.append(" \\hline");
		}
		ret.append("\n");
	}

}
