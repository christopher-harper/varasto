package fi.sardion.varasto.storage;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * 
 * @author chris 25 Sep 2016
 */
public class SingleFileSystem extends AbstractSystem {
	/**
	 * <code>IS_POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");</code>
	 */
	public static final boolean IS_POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix"); //$NON-NLS-1$

	/**
	 * <code>RADIX = 23;</code> Base for the directory naming from an integer.
	 * The radix 23 gives
	 */
	public static final int RADIX = 23;
	/**
	 * <code>MAX_ITEMS = Integer.parseInt("mm", RADIX);</code>
	 */
	public static final int MAX_ITEMS = Integer.parseInt("mm", RADIX); //$NON-NLS-1$

	/**
	 * Grant only the owner R&W
	 * <code>PosixFilePermissions.fromString("rw-------");</code>
	 */
	private static final Set<PosixFilePermission> STORE_PERMISSIONS = PosixFilePermissions.fromString("rw-------"); //$NON-NLS-1$

	/**
	 * <code>LOGGER = Logger.getLogger(SingleFileSystem.class);</code>
	 */
	private static final Logger LOGGER = Logger.getLogger(SingleFileSystem.class);
	/**
	 * 
	 */
	private static Path STORAGE_ROOT;

	private void checkCreate(final Path aPath, final boolean isDir) throws IOException {
		if (!Files.exists(aPath)) {
			if (isDir) {
				Files.createDirectory(aPath);
			} else {
				Files.createFile(aPath);
			}
			if (IS_POSIX) {
				Files.setPosixFilePermissions(aPath, STORE_PERMISSIONS);
			}
		}
	}

	private Path getFirstFree(final Path directory, final int aLevel) throws IOException {
		LOGGER.info(String.format("Looking into directory %s to check whether it has the next free file.", directory)); //$NON-NLS-1$
		int dirLevel = aLevel;
		File[] subDirectories = directory.toFile().listFiles(new FileFilter() {
			/**
			 * @param child
			 *            found child file.
			 * @return true if the File is a directory.
			 */
			@Override
			public boolean accept(final File child) {
				return child.isDirectory();
			}
		});
		if (STRUCTURE_DEPTH > dirLevel && subDirectories.length > 0) {
			Arrays.sort(subDirectories);
			return getFirstFree(Paths.get(subDirectories[subDirectories.length - 1].getPath()), dirLevel + 1);
		} else if (STRUCTURE_DEPTH == dirLevel) {
			final File[] subFiles = directory.toFile().listFiles(new FileFilter() {
				/**
				 * @param child
				 *            a found child file.
				 * @return true if the File is file and not a directory.
				 */
				@Override
				public boolean accept(final File child) {
					return child.isFile();
				}
			});
			File parent = directory.getParent().toFile();
			int files = subFiles.length;
			if (MAX_ITEMS <= files) {
				while (MAX_ITEMS <= files) {
					subDirectories = parent.listFiles(new FileFilter() {
						/**
						 * @param child
						 *            found child file.
						 * @return true if the File is a directory.
						 */
						@Override
						public boolean accept(final File child) {
							return child.isDirectory();
						}
					});
					dirLevel--;
					parent = parent.getParentFile();
					files = subDirectories.length + 1;
					if (MAX_ITEMS > files) {
						Path path = Paths.get(parent.getPath(),
								StringUtils.leftPad(Integer.toString(files, RADIX), 2, '0'));
						checkCreate(path, true);
						dirLevel++;
						while (STRUCTURE_DEPTH >= dirLevel) {
							dirLevel++;
							path = Paths.get(path.toString(), "01"); //$NON-NLS-1$
							checkCreate(path, true);
							parent = path.toFile();
						}
					}
					files = 0;
				}
				files++;
				return Paths.get(parent.toString(), StringUtils.leftPad(Integer.toString(files, RADIX), 2, '0'));
			} else if (subFiles.length > 0) {
				Arrays.sort(subFiles);
				return Paths.get(subFiles[subFiles.length - 1].getPath());
			} else {
				return Paths.get(directory.toString(), "01"); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * How many folder levels will the storage have?
	 * <code>STRUCTURE_DEPTH = 3;</code>
	 */
	private static int STRUCTURE_DEPTH = 3;
	/**
	 * What is the next free file in this storage.
	 */
	private static volatile Path NEXT_FREE_FILE;

	/**
	 * @param nextFreeFile
	 *            pointer to the next written file.
	 */
	protected static synchronized void setCurrentFile(final Path nextFreeFile) {
		/* Make a defensive copy. */
		SingleFileSystem.NEXT_FREE_FILE = Paths.get(nextFreeFile.toString());
	}

	/**
	 * @return the current next free path that can be written to.
	 * @throws IOException
	 *             if directories can't be created.
	 */
	protected synchronized Path getCurrentFile() throws IOException {
		Path returnPath = SingleFileSystem.NEXT_FREE_FILE;
		Path workPath = SingleFileSystem.NEXT_FREE_FILE;
		int nextFile = Integer.parseInt(workPath.getFileName().toString(), RADIX);
		if (MAX_ITEMS < nextFile) {
			int levels = STRUCTURE_DEPTH;
			while (MAX_ITEMS < nextFile) {
				levels--;
				workPath = workPath.getParent();
				nextFile = Integer.parseInt(workPath.getFileName().toString(), RADIX) + 1;
			}
			workPath = Paths.get(workPath.getParent().toString(),
					StringUtils.leftPad(Integer.toString(nextFile, RADIX), 2, '0'));
			checkCreate(workPath, true);
			while (STRUCTURE_DEPTH > levels) {
				levels++;
				workPath = Paths.get(workPath.toString(), "01"); //$NON-NLS-1$
			}
		}
		return returnPath;
	}

	/**
	 * 
	 * @param config
	 * @throws IOException
	 */
	public SingleFileSystem(final Element config) throws IOException {
		super(config);
		LOGGER.debug("Loading SingleFileSystem"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @throws IOException
	 * @see fi.sardion.varasto.storage.AbstractSystem#initialize()
	 */
	@Override
	void initialize() throws IOException {
		LOGGER.debug("Initializing SingleFileSystem"); //$NON-NLS-1$
		STORAGE_ROOT = Paths.get(getLocation());
		if (!Files.exists(STORAGE_ROOT)) {
			ExceptionInInitializerError eiie = new ExceptionInInitializerError(
					String.format("Storage root folder '%s' doesn't exist.", STORAGE_ROOT.toString())); //$NON-NLS-1$
			LOGGER.error(eiie.getMessage(), eiie);
			throw eiie;
		}
		if (!Files.isDirectory(STORAGE_ROOT)) {
			ExceptionInInitializerError eiie = new ExceptionInInitializerError(
					String.format("Storage root '%s' is a file.", STORAGE_ROOT.toString())); //$NON-NLS-1$
			LOGGER.error(eiie.getMessage(), eiie);
			throw eiie;
		}
		setCurrentFile(getFirstFree(STORAGE_ROOT, 0));
	}
}
