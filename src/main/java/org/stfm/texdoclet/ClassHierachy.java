package org.stfm.texdoclet;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Manages and prints a class hierarchy. Use <CODE>add</CODE> to add another
 * class to the hierarchy. Use <CODE>printTree</CODE> to print the corresponding
 * <TEX txt="\LaTeX{}">LaTeX</TEX>.
 * 
 * @version $Revision: 1.1 $
 * @author Soeren Caspersen - XO Software
 */
public class ClassHierachy extends java.lang.Object {

	public SortedMap root = new TreeMap();

	/**
	 * Creates new ClassHierachy
	 */
	public ClassHierachy() {
	}

	/**
	 * Adds another class to the hierachy
	 */
	protected SortedMap add(ClassDoc cls) {
		SortedMap temp;
		if (cls.superclass() != null) {
			temp = add(cls.superclass());
		} else {
			temp = root;
		}

		SortedMap result = (SortedMap) temp.get(cls.qualifiedName());
		if (result == null) {
			result = new TreeMap();
			temp.put(cls.qualifiedName(), result);
		}
		return result;
	}

	/**
	 * Prints the <TEX txt="\LaTeX{}">LaTeX</TEX> corresponding to the tree. The
	 * tree is printed using <CODE>TeXDoclet.os</CODE>.
	 */
	public void printTree(RootDoc rootDoc, double overviewindent) {
		printBranch(rootDoc, root, 0, overviewindent);
	}

	/**
	 * Prints a branch of the tree. The branch is printed using
	 * <CODE>TeXDoclet.os</CODE>.
	 */
	protected void printBranch(RootDoc rootDoc, SortedMap map, double indent,
			double overviewindent) {
		Set set = map.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String qualifName = (String) it.next();
			ClassDoc cls = rootDoc.classNamed(qualifName);
			TeXDoclet.os.print("\\hspace{" + Double.toString(indent)
					+ "cm} $\\bullet$ "
					+ HTMLtoLaTeXBackEnd.fixText(qualifName) + " {\\tiny ");
			if (cls != null) {
				TeXDoclet.printRef(cls.containingPackage(), cls.name(), "");
			}
			TeXDoclet.os.println("} \\\\");
			printBranch(rootDoc, (SortedMap) map.get(qualifName), indent
					+ overviewindent, overviewindent);
		}
	}
}
