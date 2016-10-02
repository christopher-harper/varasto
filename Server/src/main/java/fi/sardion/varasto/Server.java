/**
 *
 */
package fi.sardion.varasto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author chris 30 Sep 2016
 */
public class Server extends Object implements Runnable {

	/**
	 * <code>Logger.getLogger(Server.class);</code>
	 */
	private static final Logger LOGGER = Logger.getLogger(Server.class);

	/**
	 * @param arguments
	 */
	public static void main(final String... arguments) {
		final ResourceBundle settings = ResourceBundle.getBundle("server"); //$NON-NLS-1$
		final int port;
		if (arguments.length > 0) {
			port = Integer.parseInt(arguments[0]);
		} else {
			port = Integer.parseInt(settings.getString("server.port")); //$NON-NLS-1$
		}
		try {
			new Thread(new Server(InetAddress.getLocalHost(), port), settings.getString("server.name")).start(); //$NON-NLS-1$
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The host to listen on.
	 */
	private final InetAddress hostAddress;

	/**
	 * The port to listen to.
	 */
	private final int port;

	/**
	 * The buffer into which we'll read data when it's available.
	 */
	private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	/**
	 * The selector that we'll be monitoring
	 */
	private final Selector selector;

	/**
	 * The channel on which we'll accept connections
	 */
	private ServerSocketChannel serverChannel;

	/**
	 * @param hostAddress
	 *            The address of the host running the Server app.
	 * @param port
	 *            the port that the app will listen to.
	 * @throws IOException
	 *             if the selector can't be initialized.
	 *
	 */
	public Server(final InetAddress hostAddress, final int port) throws IOException {
		super();
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = initSelector();
	}

	/**
	 * Wait for an event one of the registered channels. Iterate over the set of
	 * keys for which events are available. Check what event is available and
	 * deal with it.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			try {
				this.selector.select();
				final Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					final SelectionKey key = selectedKeys.next();
					selectedKeys.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable()) {
						read(key);
					}
				}
			} catch (final Exception ex) {
				Server.LOGGER.error("Failed to handle selector event.", ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * For an accept to be pending the channel must be a server socket channel.
	 * Accept the connection and make it non-blocking. Register the new
	 * SocketChannel with our Selector, indicating we'd like to be notified when
	 * there's data waiting to be read
	 *
	 * @param key
	 *            the
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void accept(final SelectionKey key) throws IOException {
		final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		final SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.socket();
		socketChannel.configureBlocking(false);
		socketChannel.register(this.selector, SelectionKey.OP_READ);
	}

	/**
	 * Create a new selector. Create a new non-blocking server socket channel.
	 * Bind the server socket to the specified address and port. Register the
	 * server socket channel, indicating an interest in accepting new
	 * connections.
	 *
	 * @return an initialized selector.
	 * @throws IOException
	 *             if the selector can't be initialized.
	 */
	private Selector initSelector() throws IOException {
		final Selector socketSelector = SelectorProvider.provider().openSelector();
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		final InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		this.serverChannel.socket().bind(isa);
		this.serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		return socketSelector;
	}

	/**
	 * @param key
	 *            holding the channel.
	 * @throws IOException
	 */
	private void read(final SelectionKey key) throws IOException {
		key.channel();

		/*
		 * Waiting for an responce to question
		 * http://stackoverflow.com/questions/39816914/java-nio-ssl-non-blocking
		 * -file-transfer-example
		 */

		// FileChannel.open(Files.createTempFile("transfer_", ".tmp", null),
		// OpenOption);
		//
		// final FileOutputStream out = new FileOutputStream(.toFile());
		// final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// for (;;) {
		// // Clear out our read buffer so it's ready for new data
		// this.readBuffer.clear();
		// // Attempt to read off the channel
		// int numRead;
		// try {
		//
		// numRead = socketChannel.read(this.readBuffer);
		// } catch (final IOException e) {
		// // The remote forcibly closed the connection, cancel
		// // the selection key and close the channel.
		// key.cancel();
		// socketChannel.close();
		// return;
		// }
		// if (numRead == -1) {
		// // Remote entity shut the socket down cleanly. Do the
		// // same from our end and cancel the channel.
		// key.channel().close();
		// key.cancel();
		// break;
		// }else{
		//
		// }
		// }
		// // Hand the data off to our worker thread
		// this.worker.processData(this, socketChannel, baos.toByteArray());
	}

}
