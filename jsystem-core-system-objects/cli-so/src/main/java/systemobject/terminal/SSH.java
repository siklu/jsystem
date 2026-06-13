/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package systemobject.terminal;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;

import java.security.Security;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;

/**
 * A terminal used for SSH Connection
 */
public class SSH extends Terminal {

	protected String hostname;

	protected String username;

	protected String password;

	protected SSHClient conn = null;

	protected Session sess = null;

	protected Session.Shell shell = null;

	//ssh port forwarding
	protected LocalPortForwarder lpf = null;

	protected ServerSocket lpfSocket = null;

	protected int sourcePort = -1;

	protected int destinationPort = -1;

	protected boolean xtermTerminal = true;

	/**
	 * Is enabled the sudo terminal
	 */
	protected boolean enableSudoTerminal = true;

	public SSH(String hostnameP, String usernameP, String passwordP, boolean enableSudoTerminal) {
		this(hostnameP, usernameP, passwordP, -1, -1, true, enableSudoTerminal);
	}


	/**
	 * Constructor with Destination port
	 * if destinationPort specified in this constructor no LocalPortForwarder will be used
	 *
	 * @param  hostnameP IP or Hostname of the destination machine
	 * @param  usernameP username for SSH auth
	 * @param  passwordP Password for SSH auth
	 * @param  destinationPort custom destination Port for ssh connection
	 */
	public SSH(String hostnameP, String usernameP, String passwordP, int destinationPort, boolean enableSudoTerminal) {
		this(hostnameP, usernameP, passwordP, -1, destinationPort, true, enableSudoTerminal);
	}


	public SSH(String hostnameP, String usernameP, String passwordP, int sourceTunnelPort, int destinationTunnelPort, boolean enableSudoTerminal) {
		this(hostnameP, usernameP, passwordP, sourceTunnelPort, destinationTunnelPort, true, enableSudoTerminal);
	}

	public SSH(String hostnameP, String usernameP, String passwordP, int sourceTunnelPort, int destinationTunnelPort, boolean _xtermTerminal, boolean enableSudoTerminal) {
		super();
		hostname = hostnameP;
		username = usernameP;
		password = passwordP;
		sourcePort = sourceTunnelPort;
		destinationPort =destinationTunnelPort;
		xtermTerminal = _xtermTerminal;
		this.enableSudoTerminal = enableSudoTerminal;
	}

	@Override
	public void connect() throws IOException {
		Security.removeProvider("BC");
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		conn = new SSHClient();
		conn.addHostKeyVerifier(new PromiscuousVerifier());

		/* Now connect */
		if (destinationPort > -1) {
			conn.connect(hostname, destinationPort);
		}
		else {
			conn.connect(hostname);
		}

		boolean isAuthenticated = false;

		/* Try password authentication */
		try {
			conn.authPassword(username, password);
			isAuthenticated = true;
		} catch (UserAuthException e) {
			isAuthenticated = false;
		}

		/* Fall back to keyboard-interactive */
		if (!isAuthenticated) {
			conn.auth(username, new AuthKeyboardInteractive(new InteractiveLogic()));
		}

		if (sourcePort > -1 && destinationPort > -1) {
			lpfSocket = new ServerSocket(sourcePort);
			Parameters params = new Parameters(
					"127.0.0.1", sourcePort, "localhost", destinationPort);
			lpf = conn.newLocalPortForwarder(params, lpfSocket);
			Thread lpfThread = new Thread(() -> {
				try {
					lpf.listen();
				} catch (IOException e) {
					// port forwarder closed
				}
			});
			lpfThread.setDaemon(true);
			lpfThread.start();
		}

		/* Create a session */
		sess = conn.startSession();
		if(enableSudoTerminal) {
			if (xtermTerminal) {
				sess.allocatePTY("xterm", 80, 24, 640, 480, Collections.emptyMap());
			} else {
				sess.allocatePTY("dumb", 200, 50, 0, 0, Collections.emptyMap());
			}
		}

		shell = sess.startShell();
		in = shell.getInputStream();
		out = shell.getOutputStream();
	}

	@Override
	public void disconnect() throws IOException {
		if (lpf != null) {
			try {
				lpf.close();
			} catch (IOException e) {
			}
		}
		if (sess != null) {
			try {
				sess.close();
			} catch (IOException e) {
			}
		}
		if (conn != null) {
			conn.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public String getConnectionName() {
		return "SSH";
	}

	/**
	 * The logic that one has to implement if "keyboard-interactive"
	 * authentication shall be supported.
	 */
	@SuppressWarnings("rawtypes")
	class InteractiveLogic implements ChallengeResponseProvider {

		
		@Override
		public List<String> getSubmethods() {
			return Collections.emptyList();
		}

		@Override
		public void init(Resource resource, String name, String instruction) {
			// no-op
		}

		@Override
		public char[] getResponse(String prompt, boolean echo) {
			if (prompt.toLowerCase().startsWith("password:")) {
				return password.toCharArray();
			}
			System.out.print("SSH client - Unknown prompt type returned (" + prompt + ")\n");
			return new char[0];
		}

		@Override
		public boolean shouldRetry() {
			return false;
		}
	}

	protected String getHostname() {
		return hostname;
	}

	protected void setHostname(String hostname) {
		this.hostname = hostname;
	}

	protected String getUsername() {
		return username;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	protected String getPassword() {
		return password;
	}

	protected void setPassword(String password) {
		this.password = password;
	}

	protected SSHClient getConn() {
		return conn;
	}

	protected void setConn(SSHClient conn) {
		this.conn = conn;
	}

	protected int getSourcePort() {
		return sourcePort;
	}

	protected void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	protected int getDestinationPort() {
		return destinationPort;
	}

	protected void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}

}
