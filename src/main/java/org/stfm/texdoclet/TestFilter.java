package org.stfm.texdoclet;

import com.sun.javadoc.ClassDoc;

/**
 * This class filters out classes beginning with "Test" when applied to the
 * Doclet.
 * 
 * @version $Revision: 1.2 $
 */
public class TestFilter implements ClassFilter {

	/**
	 * Returns false if class name starts with "Test".
	 */
	@Override
	public boolean includeClass(ClassDoc cd) {
		return !cd.name().startsWith("Test");
	}
}
