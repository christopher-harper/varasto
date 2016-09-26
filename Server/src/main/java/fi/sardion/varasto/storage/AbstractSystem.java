package fi.sardion.varasto.storage;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author chris 25 Sep 2016
 */
public abstract class AbstractSystem extends Object {
	/**
	 * <code>LOGGER =Logger.getLogger(AbstractSystem.class);</code>
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractSystem.class);
	/**
	 * Element to store whether a storage is encrypted.
	 */
	private static final String STORAGE_ENCRYPTED = "storageEncrypted"; //$NON-NLS-1$
	/**
	 * Element name for the storage name.
	 */
	private static final String STORAGE_NAME = "storageName"; //$NON-NLS-1$
	/**
	 * Element name for the information of whether the storage can be written
	 * to.
	 */
	private static final String STORAGE_WRITABLE = "storageWritable"; //$NON-NLS-1$
	/**
	 * Where the storage is located for example file system path, URL, etc.
	 */
	private static final String STORAGE_LOCATION = "storageLocation"; //$NON-NLS-1$
	/**
	 * The configurations for the store.
	 */
	private Document config;

	/**
	 * @param aConfig
	 */
	public AbstractSystem(Document aConfig) {
		super();
		LOGGER.info("Initialising AbstractSystem."); //$NON-NLS-1$
		setConfig(aConfig);
		initialize();
	}

	/**
	 * @return the name of the storage from the configuration.
	 */
	public String getName() {
		return getStringContent(STORAGE_NAME);
	}

	/**
	 * 
	 * @return Whether this storage is encrypted.
	 */
	public boolean isEncrypted() {
		return Boolean.parseBoolean(getStringContent(STORAGE_ENCRYPTED));
	}

	/**
	 * @return Whether the storage can be written to.
	 */
	public boolean isWritable() {
		return Boolean.parseBoolean(getStringContent(STORAGE_WRITABLE));
	}

	/**
	 * Get the configuration object for this storage.
	 * 
	 * @return the config object.
	 */
	protected Document getConfig() {
		return this.config;
	}

	/**
	 * Get the first element with a given name.
	 * 
	 * @param aName
	 *            the name of the element.
	 * @return the first found element.
	 */
	protected Element getFirstElementByName(final String aName) {
		if (aName == null || aName.length() <= 0) {
			final IllegalArgumentException iaex = new IllegalArgumentException(
					"The name of the searched element must be given."); //$NON-NLS-1$
			LOGGER.error(iaex.getMessage(), iaex);
			throw iaex;
		}
		final NodeList elements = getConfig().getElementsByTagName(aName);
		if (elements.getLength() > 0) {
			return (Element) elements.item(0);
		}
		final IllegalArgumentException iaex = new IllegalArgumentException(
				String.format("The given element name '%s' did not return anything.", aName)); //$NON-NLS-1$
		LOGGER.error(iaex.getMessage(), iaex);
		throw iaex;
	}

	/**
	 * Get the text content of an element.
	 * 
	 * @param element
	 *            name of the element.
	 * @return the content.
	 */
	protected String getStringContent(final String element) {
		final String temp = getFirstElementByName(element).getTextContent().trim();
		LOGGER.debug(String.format("Found '%s' from %s.", temp, element)); //$NON-NLS-1$
		return temp;
	}

	/**
	 * @param aConfig
	 *            the configuration for this storage.
	 */
	protected void setConfig(Document aConfig) {
		LOGGER.debug("Setting config."); //$NON-NLS-1$
		this.config = aConfig;
	}

	/**
	 * @return the storage location.
	 */
	public String getLocation() {
		return getStringContent(STORAGE_LOCATION);
	}

	/**
	 * Initialize the store and perform checks whether it is operable.
	 */
	abstract void initialize();
}
