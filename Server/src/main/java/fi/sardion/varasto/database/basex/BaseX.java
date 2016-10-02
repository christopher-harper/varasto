package fi.sardion.varasto.database.basex;

import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.basex.BaseXServer;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

import fi.sardion.varasto.database.Database;

/**
 * @author chris 2 Oct 2016
 */
public class BaseX extends Database {

	/**
	 * <code>Logger.getLogger(BaseX.class);</code>
	 */
	static final Logger LOGGER = Logger.getLogger(BaseX.class);
	/**
	 * The server instance if running on localhost.
	 */
	static BaseXServer SERVER;
	/**
	 * The current workers database session.
	 */
	private ClientSession session;

	/**
	 * Sole public constructor without arguments to enable reflection
	 * Instantiation.
	 */
	public BaseX() {
		super();
	}

	/**
	 *
	 * @see fi.sardion.varasto.database.Database#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		if ("localhost".equals(getHost())) { //$NON-NLS-1$
			try {
				BaseX.SERVER = new BaseXServer(String.format("-p %s", String.valueOf(getPort()))); //$NON-NLS-1$
				Runtime.getRuntime().addShutdownHook(new Thread() {

					/**
					 * Entry point when the JVM shuts down.
					 *
					 * @see java.lang.Thread#run()
					 */
					@Override
					public void run() {
						super.run();
						try {
							BaseX.SERVER.stop();
						} catch (final IOException ioex) {
							BaseX.LOGGER.error("Failed to close down internal BaseX server.", ioex); //$NON-NLS-1$
						}
					}
				});
			} catch (final IOException ioex) {
				BaseX.LOGGER.error(String.format("Failed to startup an internal BaseX server on %s port .", //$NON-NLS-1$
						String.valueOf(getPort())), ioex);
				throw ioex;
			}
		}
	}

	/**
	 * @param xQuery
	 *            the XQuery to execure.
	 * @return the query executed with the given credentials.
	 * @throws Exception
	 *             if the query fails after two attempts.
	 */
	public ClientQuery query(final String xQuery) throws Exception {
		try {
			try {
				return new ClientQuery(xQuery, getSession(), getSession().getOutputStream());
			} catch (final SocketException sexSwallow) {
				final String message = String.format("Session disconnected with message %s. Re-trying once more.", //$NON-NLS-1$
						sexSwallow.getMessage());
				BaseX.LOGGER.warn(message, sexSwallow);
				this.session = null;
				return new ClientQuery(xQuery, getSession(), getSession().getOutputStream());
			}
		} catch (final Exception ex) {
			final String message = String.format("Failed to execute query:\n %s", xQuery); //$NON-NLS-1$
			BaseX.LOGGER.error(message, ex);
			throw ex;
		}
	}

	/**
	 * If this argument is given as 'localhost' the database instance and
	 * connection is made to an in memory local database. If the database is
	 * running on localhost in a separate process use <b>127.0.0.1</b>.
	 *
	 * @param aHost
	 *            the name of the database host.
	 * @see fi.sardion.varasto.database.Database#setHost(String)
	 */
	@Override
	public void setHost(final String aHost) {
		super.setHost(aHost);
	}

	/**
	 * @return a connected BaseX session.
	 * @throws IOException
	 *             if a session opening fails.
	 */
	ClientSession getSession() throws IOException {
		if (this.session == null) {
			try {
				this.session = new ClientSession(getHost(), getPort(), getUser(), getPassword());
			} catch (final IOException ioex) {
				BaseX.LOGGER.error(String.format("Failed to open a session to %s:%s for user %s.", getHost(), //$NON-NLS-1$
						String.valueOf(getPort()), getUser()));
				throw ioex;
			}
		}
		return this.session;
	}
}
