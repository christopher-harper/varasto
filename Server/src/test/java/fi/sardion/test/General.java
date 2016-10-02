package fi.sardion.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fi.sardion.varasto.storage.SingleFileSystem;

/**
 * 
 * @author chris 27 Sep 2016
 */
public class General extends Object {
	/**
	 * 
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		File storageRoot = new File("/Volumes/pannu/Storage");
		SingleFileSystem sfs = new SingleFileSystem(getConfig());
		createSubFolders(new File(sfs.getLocation()), 0, 0);
	}

	private static long createSubFolders(final File parent, final int depth, long count) {
		for (int index = 0; index < SingleFileSystem.MAX_ITEMS; index++) {
			File child = new File(parent, StringUtils.leftPad(Integer.toString(index, 23), 2, '0'));
			count++;
			if (depth == 3) {
				child.mkdirs();
				System.out.println(child.getAbsolutePath() + " = " + count);
			} else {
				count = createSubFolders(child, depth + 1, count);
			}
		}
		return count;
	}
	static Element getConfig() throws SAXException, IOException, ParserConfigurationException{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/Users/chris/git/varasto/Server/src/test/java/fi/sardion/test/storage.xml")).getDocumentElement();
	}
}
