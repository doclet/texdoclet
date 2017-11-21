package org.stfm.texdoclet.image;

/**
 * This class is for testing referenced images.
 * <p>
 * JPG from same package: <IMG width="100%" src="texdoclet.jpg">
 * <p>
 * JPG from parent package: <IMG width="100%" src="../texdoclet.jpg">
 * <p>
 * JPG from sub package package: <IMG width="100%" src="subpackage/texdoclet.jpg">
 *
 * @author Stefan Marx
 */
public class ImageTest {

	/** An array of objects */
	public Object[] objects;

	/**
	 * @return objects
	 */
	public Object[] getObject() {
		return this.objects;
	}

	/**
	 * @param object the object
	 */
	public void setObject(final Object[] objects) {
		this.objects = objects;
	}

}
