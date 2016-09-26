/**
 * 
 */
package fi.sardion.varasto.storage;

import org.w3c.dom.Document;

/**
 * @author chris 25 Sep 2016
 */
public class TeeFileSystem extends SingleFileSystem {

	/**
	 * @param config
	 */
	public TeeFileSystem(Document config) {
		super(config);
	}

}
