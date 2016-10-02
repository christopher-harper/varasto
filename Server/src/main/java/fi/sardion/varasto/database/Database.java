/**
 *
 */
package fi.sardion.varasto.database;

import org.basex.server.ClientQuery;

/**
 * @author chris 2 Oct 2016
 */
public abstract class Database extends Object {

	/**
	 * The database host name(FQDN)/IP.
	 */
	private String host;
	/**
	 * The database user password.
	 */
	private String password;
	/**
	 * The database port.
	 */
	private int port;
	/**
	 * The database user.
	 */
	private String user;

	/**
	 * Public no argument constructor to enable reflection.
	 */
	public Database() {
		super();
	}

	/**
	 * Initialize the database connection.
	 *
	 * @throws Exception
	 *             if the database can't be initialized.
	 */
	public abstract void initialize() throws Exception;

	/**
	 * Execute a database query. FIXME: Don't expose the native implementation
	 * of a database vendor.
	 *
	 * @param aQuery
	 *            the query to run.
	 * @return the initialized query.
	 * @throws Exception
	 *             if the query fails.
	 */
	public abstract ClientQuery query(final String aQuery) throws Exception;

	/**
	 * @param aHost
	 *            the name of the database host.
	 */
	public void setHost(final String aHost) {
		this.host = aHost;
	}

	/**
	 * @param aPassword
	 *            a password for the database user.
	 */
	public void setPassword(final String aPassword) {
		this.password = aPassword;
	}

	/**
	 * @param aPort
	 *            that the database is listening to.
	 */
	public void setPort(final int aPort) {
		this.port = aPort;
	}

	/**
	 * @param aUserName
	 *            the database user name.
	 */
	public void setUser(final String aUserName) {
		this.user = aUserName;
	}

	/**
	 * Get the database host name. Preferably either IP or FQDN.
	 *
	 * @return the host.
	 */
	protected String getHost() {
		return this.host;
	}

	/**
	 * Get the database user password.
	 *
	 * @return the password.
	 */
	protected String getPassword() {
		return this.password;
	}

	/**
	 * Get the database port.
	 *
	 * @return the port.
	 */
	protected int getPort() {
		return this.port;
	}

	/**
	 * Get the database user.
	 *
	 * @return the user.
	 */
	protected String getUser() {
		return this.user;
	}

}
