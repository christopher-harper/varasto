/**
 * 
 */
package fi.sardion.varasto.storage;

import java.io.IOException;

import org.w3c.dom.Element;

/**
 * @author chris 25 Sep 2016
 */
public class TeeFileSystem extends SingleFileSystem {

	/**
	 * @param config
	 * @throws IOException 
	 */
	public TeeFileSystem(final Element aConfig) throws IOException {
		super(aConfig);
	}

}
