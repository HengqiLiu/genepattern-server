package org.genepattern.server.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;

import org.genepattern.server.genepattern.GenePatternAnalysisTask;

/**
 * install a set of zipped GenePattern tasks into the local GenePattern server.
 * create a new task database if necessary
 * 
 * @author Jim Lerner
 */

public class CommandLineAction {

	boolean hadToStartDB = false;

	Connection conn = null;

	GenePatternAnalysisTask gp = new GenePatternAnalysisTask();

	protected boolean DEBUG = false;

	Properties props = null;

	public void preRun(String[] args) {

		DEBUG = (System.getProperty("DEBUG") != null);
		try {
			String resourcesDir = new File(System.getProperty("resources"))
					.getCanonicalPath();
			if (DEBUG)
				System.out.println("resourcesDir="
						+ new File(resourcesDir).getCanonicalPath());
			Properties props = loadProps(resourcesDir);
			if (DEBUG)
				System.out.println(props);
			System.setProperty("genepattern.properties", resourcesDir);
			System.setProperty("omnigene.conf", resourcesDir);
			//		System.setProperty("user.dir", new
			// File(props.getProperty("Tomcat")).getCanonicalPath());
			//		System.setProperty("user.dir", resourcesDir);
			hadToStartDB = connectDatabase(resourcesDir, props);

		} catch (Throwable e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void postRun(String[] args) throws Exception {
		if (hadToStartDB) {
			System.out.println("stopping database because I started it");
			Connection conn = getConnection(props);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("SHUTDOWN COMPACT");
			stmt.close();
			//conn.close();
		}

	}

	boolean connectDatabase(String resourceDir, Properties props)
			throws ClassNotFoundException, SQLException, IOException, Exception {
		for (int i = 0; conn == null && i < 4; i++) {
			try {
				conn = getConnection(props);
			} catch (SQLException se) {
				// sleep 1 second and try again
				Thread.currentThread().sleep(1000);
			}
		}
		if (conn == null) {
			hadToStartDB = startDB(props);
			conn = getConnection(props);
		}
		if (!checkSchema(resourceDir, props)) {
			createSchema(resourceDir, props);
			if (!checkSchema(resourceDir, props)) {
				throw new IOException(
						"unable to successfully create task_master table.  Other tables also suspect.");
			}
		}
		return hadToStartDB;
	}

	//Returns Connection from connection pool based on DAO settings
	private Connection getConnection(Properties props)
			throws ClassNotFoundException, SQLException {
		String driver = props.getProperty("DB.driver");
		String url = props.getProperty("DB.url");
		String username = props.getProperty("DB.username");
		String password = props.getProperty("DB.password");
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	private boolean startDB(Properties props) throws Exception {
		String taskName = null;
		String className = null;
		String classPath = null;
		try {
			taskName = props.getProperty("DB.taskName");
			className = props.getProperty(taskName + ".class");
			classPath = props.getProperty(taskName + ".classPath", "");
			StringTokenizer classPathElements = new StringTokenizer(classPath,
					";");
			URL[] classPathURLs = new URL[classPathElements.countTokens()];
			int i = 0;
			while (classPathElements.hasMoreTokens()) {
				URL url = new File(classPathElements.nextToken()).toURL();
				classPathURLs[i++] = url;
			}
			String args = props.getProperty(taskName + ".args", "");
			StringTokenizer stArgs = new StringTokenizer(args, " ");
			String[] argsArray = new String[stArgs.countTokens()];
			i = 0;
			while (stArgs.hasMoreTokens()) {
				argsArray[i++] = stArgs.nextToken();
			}

			//		String userDir = props.getProperty(taskName + ".dir",
			// System.getProperty("user.dir"));
			//		System.setProperty("user.dir", userDir);

			URLClassLoader classLoader = new URLClassLoader(classPathURLs, null);
			Class theClass = Class.forName(className, true, classLoader);
			if (theClass == null) {
				throw new ClassNotFoundException("unable to find class "
						+ className + " using classpath " + classPathElements);
			}
			Method main = theClass.getMethod("main",
					new Class[] { String[].class });
			CLAInstallThread thread = new CLAInstallThread(taskName, main,
					argsArray);
			thread.setDaemon(true);

			// start the new thread and let it run until it is idle (ie. inited)

			thread.setPriority(Thread.currentThread().getPriority() + 1);
			thread.start();

			// just in case, give other threads a chance
			Thread.currentThread().yield();
			System.out.println(taskName + " thread running");
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException ie) {
			}
			return true;
		} catch (SecurityException se) {
			System.err.println("unable to launch " + taskName + ": " + se);
		} catch (MalformedURLException mue) {
			System.err.println("Bad URL: " + mue);
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Can't find class " + className
					+ " using classpath " + classPath + ": " + cnfe);
		} catch (NoSuchMethodException nsme) {
			System.err.println("Can't find main in " + className + ": " + nsme);
		} catch (Throwable e) {
			System.err.println(e.getMessage() + " while launching " + taskName);
		}
		return false;
	}

	private Properties loadProps(String propsDir) throws IOException {
		File propFile = new File(propsDir, "genepattern.properties");
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			fis = new FileInputStream(propFile);
			props.load(fis);
		} catch (IOException ioe) {
			throw new IOException("InstallTasks.loadProps: "
					+ propFile.getCanonicalPath() + " cannot be loaded.  "
					+ ioe.getMessage());
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ioe) {
			}
		}
		return props;
	}

	private boolean checkSchema(String resourceDir, Properties props)
			throws SQLException, IOException {
		if (DEBUG)
			System.out.print("checking schema: ");
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getTables(null, null, null,
				new String[] { "TABLE" });
		boolean hasTaskMaster = false;
		String tableName = null;
		while (rs.next()) {
			tableName = rs.getString("table_name");
			if (DEBUG)
				System.out.print(tableName + " ");
			if (tableName.equalsIgnoreCase("TASK_MASTER"))
				hasTaskMaster = true;
		}
		rs.close();
		if (DEBUG)
			System.out.println("");
		return hasTaskMaster;
	}

	private void createSchema(String resourceDir, Properties props)
			throws SQLException, IOException {
		String schemaFilename = resourceDir + File.separator
				+ props.getProperty("DB.schema");
		File schemaFile = new File(schemaFilename);
		System.out.println("rebuilding database from schema "
				+ schemaFile.getCanonicalPath());
		String all = readFile(schemaFile);
		Statement stmt = conn.createStatement();
		while (!all.equals("")) {
			int i = all.indexOf(';');
			String sql;
			if (i != -1) {
				sql = all.substring(0, i);
				all = all.substring(i + 1);
			} else {
				sql = all;
				all = "";
			}
			if (sql.startsWith("--")) {
				continue;
			}
			try {
				if (DEBUG)
					System.out.println("-> " + sql);
				stmt.executeUpdate(sql);
			} catch (SQLException se) {
				System.err.println("SQLException " + se + " while executing "
						+ sql);
				//throw se;
			}
		}
		stmt.close();
	}

	String readFile(File file) throws IOException {
		FileReader reader = null;
		StringBuffer b = new StringBuffer();
		try {
			reader = new FileReader(file);
			char buffer[] = new char[1024];
			while (true) {
				int i = reader.read(buffer, 0, buffer.length);
				if (i == -1) {
					break;
				}
				b.append(buffer, 0, i);
			}
		} catch (IOException ioe) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
				}
			}
		}
		return b.toString();
	}

}

class CLAInstallThread extends Thread {
	Method mainMethod;

	String[] args;

	protected boolean DEBUG = false;

	public CLAInstallThread(String taskName, Method mainMethod, String[] args) {
		super(taskName);
		this.mainMethod = mainMethod;
		this.args = args;

		this.DEBUG = (System.getProperty("DEBUG") != null);
	}

	public void run() {
		try {
			if (DEBUG)
				System.out.println("invoking "
						+ mainMethod.getDeclaringClass().getName() + "."
						+ mainMethod.getName());
			mainMethod.invoke(null, new Object[] { args });
			if (DEBUG)
				System.out.println(getName() + " "
						+ mainMethod.getDeclaringClass().getName() + "."
						+ mainMethod.getName() + " returned from execution");
		} catch (IllegalAccessException iae) {
			System.err
					.println("Can't invoke main in " + getName() + ": " + iae);
		} catch (IllegalArgumentException iae2) {
			System.err.println("Bad args for " + getName() + ": " + iae2);
		} catch (InvocationTargetException ite) {
			System.err.println("InvocationTargetException for " + getName()
					+ ": " + ite.getTargetException());
		} catch (Exception e) {
			System.err.println("Exception for " + getName() + ": " + e);
		}
	}
}

