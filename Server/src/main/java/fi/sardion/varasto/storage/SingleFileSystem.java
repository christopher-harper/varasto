package fi.sardion.varasto.storage;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * 
 * @author chris 25 Sep 2016
 */
public class SingleFileSystem extends AbstractSystem {
	/**
	 * <code>LOGGER = Logger.getLogger(SingleFileSystem.class);</code>
	 */
	private static final Logger LOGGER = Logger.getLogger(SingleFileSystem.class);
	/**
	 * Current directory.
	 */
	private static volatile File LOCATION;
	private static volatile byte currentIndex;

	/**
	 * 
	 * @param config
	 */
	public SingleFileSystem(final Document config) {
		super(config);
		LOGGER.debug("Initializing SingleFileSystem"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @see fi.sardion.varasto.storage.AbstractSystem#initialize()
	 */
	@Override
	void initialize() {
		final File storageRoot = new File(getLocation());
		if (!storageRoot.exists()) {
			ExceptionInInitializerError eiie = new ExceptionInInitializerError(
					String.format("Storage root folder '%s' doesn't exist.", storageRoot.getAbsolutePath())); //$NON-NLS-1$
			LOGGER.error(eiie.getMessage(), eiie);
			throw eiie;
		}
		if (storageRoot.isFile()) {
			ExceptionInInitializerError eiie = new ExceptionInInitializerError(
					String.format("Storage root '%s' is a file.", storageRoot.getAbsolutePath())); //$NON-NLS-1$
			LOGGER.error(eiie.getMessage(), eiie);
			throw eiie;
		}
		getMax(storageRoot, 0);
	}

	/**
	 * 
	 * @param parent
	 * @param level
	 */
	private static void getMax(final File parent, final int level) {
		if (level < 4) {
			byte index = Byte.MAX_VALUE;
			String name;
			for (; index > 0; index--) {
				name = StringUtils.leftPad(Integer.toString(index, 16),  2, "0"); //$NON-NLS-1$
				final File child = new File(parent, name);
				if (child.exists() && child.isDirectory()) {
					LOGGER.debug(String.format("Found directory '%s' in the '%s' storage.", child.getAbsolutePath())); //$NON-NLS-1$
					getMax(child, level + 1);
					break;
				}
				LOGGER.debug(String.format("The directory '%s' doesn't exist.", child.getAbsolutePath())); //$NON-NLS-1$
			}
			if(index==0){
				
			}
		} else if (level == 4) {

		} else {

		}
	}
}
