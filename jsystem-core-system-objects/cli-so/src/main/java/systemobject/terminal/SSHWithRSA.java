/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package systemobject.terminal;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;

/**
 * A terminal used for SSH Connection
 */
public class SSHWithRSA extends SSH {

	private File privateKeyFile;

	public SSHWithRSA(String hostnameP, String usernameP, String passwordP,
			File privateKey,boolean enablePt) {
		super(hostnameP, usernameP, passwordP,-1, -1, false, enablePt);
		privateKeyFile = privateKey;

	}

	@Override
	public void connect() throws IOException {
		System.out.println("Connect to Host with SSH and RSA private key");
		conn = new SSHClient();
		conn.addHostKeyVerifier(new PromiscuousVerifier());
		conn.connect(hostname);

		boolean isAuthenticated = false;

		/* Try public key authentication first */
		if (privateKeyFile != null && privateKeyFile.isFile()) {
			try {
				conn.authPublickey(username, conn.loadKeys(privateKeyFile.getPath(), (char[]) null));
				isAuthenticated = true;
			} catch (UserAuthException e) {
				System.out.println("Public key authentication failed: " + e.getMessage());
				isAuthenticated = false;
			}
		} else {
			System.out.println("Auth Error - The privateKeyFile should be initialized with a valid path to a private key");
		}

		/* Fall back to password authentication */
		if (!isAuthenticated) {
			try {
				conn.authPassword(username, password);
				isAuthenticated = true;
			} catch (UserAuthException e) {
				isAuthenticated = false;
			}
		}

		/* Fall back to keyboard-interactive */
		if (!isAuthenticated) {
			conn.auth(username, new AuthKeyboardInteractive(new InteractiveLogic()));
		}

		if (sourcePort > -1 && destinationPort > -1) {
			lpfSocket = new ServerSocket(sourcePort);
			LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters(
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
		if (xtermTerminal) {
			sess.allocatePTY("xterm", 80, 24, 640, 480, Collections.emptyMap());
		} else {
			sess.allocatePTY("dumb", 200, 50, 0, 0, Collections.emptyMap());
		}

		shell = sess.startShell();
		in = shell.getInputStream();
		out = shell.getOutputStream();
	}

	@Override
	public void disconnect() throws IOException {
		super.disconnect();
	}

	@Override
	public boolean isConnected() {
		return super.isConnected();
	}

	@Override
	public String getConnectionName() {
		return "SSH_RSA";
	}

	public File getPrivateKeyFile() {
		return privateKeyFile;
	}

	public void setPrivateKeyFile(File privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

}
