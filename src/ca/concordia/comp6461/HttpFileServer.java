package ca.concordia.comp6461;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * References 
 * 	https://github.com/lakshayverma629/COMP6461LA2/blob/master/src/com/COMP6461/server/SocketConnectionFromServer.java
 * https://stackoverflow.com/questions/6142901/how-to-create-a-file-in-a-directory-in-java
 * https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd
 * 
 */

public class HttpFileServer {

	private static int port = 8080;

	private static boolean verbose = false;

	private static String currentDir = "";

	private static Logger LOGGER = Logger.getLogger(HttpFileServer.class.getName());
	
	private Socket socket;

	public static void main(String[] args) {

		try {

			// Start the server
			
			Scanner scan = new Scanner(System.in);

			String command = scan.nextLine();

			String[] splitCommand = command.split("\\s+");
			
			LOGGER.setLevel(Level.OFF);

			if (command.contains("-v")) {

				verbose = true;

				LOGGER.setLevel(Level.ALL);

				LOGGER.info("Verbose ennabled");

			}

			if (command.contains("-p")) {

				for (int i = 0; i < splitCommand.length; i++) {

					String s = splitCommand[i];

					if (s.contains("-p")) {

						port = Integer.parseInt(splitCommand[i + 1]);

					}
				}

			}

			if (command.contains("-d")) {

				for (int i = 0; i < splitCommand.length; i++) {

					String s = splitCommand[i];

					if (s.contains("-d")) {

						currentDir = splitCommand[i + 1];

					}
				}

			}

			LOGGER.info("Current Dir" + currentDir);

			ServerSocket server = new ServerSocket(port);
			
			LOGGER.info("Server is listening at port:" + port);

			while (true) {

				Socket client = server.accept();

				BufferedReader buffreader = new BufferedReader(new InputStreamReader(client.getInputStream()));

				StringBuffer sb = new StringBuffer();

				String line = buffreader.readLine();

				sb.append(line);

				String nextLine = "";

				Integer postDataCheck = -1;

				while ((nextLine = buffreader.readLine()) != null && (nextLine.length()) != 0) {

					if (nextLine.contains("Content-Length:")) {

						postDataCheck = new Integer(
								nextLine.substring(nextLine.indexOf("Content-Length:") + 16, nextLine.length()))
										.intValue();

					}

					sb.append(nextLine);

				}

				String[] parsedString = line.split("\\s+");

				String method = parsedString[0];

				String fileName = parsedString[1];

				String post = "";

				if (postDataCheck > 0) {

					char[] charArray = new char[postDataCheck];

					buffreader.read(charArray, 0, postDataCheck);

					post = new String(charArray);

				}

				System.out.println(post);

				PrintWriter printWriterOut = new PrintWriter(client.getOutputStream());

				BufferedOutputStream buffOutStream = new BufferedOutputStream(client.getOutputStream());

				if (method.equalsIgnoreCase("GET") && fileName.length() > 1) {

					int checkFileExists = getFileLength(fileName);

					boolean check = checkIfFileExists(fileName);

					if (checkFileExists > 0) {

						printWriterOut.println("HTTP/1.1 200 OK");

						printWriterOut.println("Content-length: " + checkFileExists);

					} else {

						System.out.println("File Not found");

						printWriterOut.println("HTTP/1.1 404 FILE NOT FOUND");

						String fileNotFound = "HTTP 404 :File Requested Not found";

						printWriterOut.println("Content-length: " + fileNotFound.length());

					}

					printWriterOut.println("Server: httpfs");

					printWriterOut.println("Date: " + new Date());

					printWriterOut.println("Content-type: " + "text/plain");

					printWriterOut.println();

					printWriterOut.flush();

					buffOutStream.write(readContentFromFile(fileName));

					buffOutStream.flush();

				} else if (method.equalsIgnoreCase("GET")) {

					LOGGER.info("Invoking GET to list all the files in current directory");

					StringBuffer fileList = listAllFiles();

					LOGGER.info(fileList.toString());

					printWriterOut.println("HTTP/1.1 200 OK");

					printWriterOut.println("Content-length: " + fileList.length());

					printWriterOut.println("Server: httpfs");

					printWriterOut.println("Date: " + new Date());

					printWriterOut.println("Content-type: " + "text/plain");

					printWriterOut.println();

					printWriterOut.flush();

					buffOutStream.write(fileList.toString().getBytes());

					buffOutStream.flush();

				}

				if (method.equalsIgnoreCase("POST")) {

					LOGGER.info("POST method invoked");

					int size = createNewFile(fileName, post);

					printWriterOut.println("HTTP/1.1 200 OK");

					printWriterOut.println("Content-length: " + "Successfuly created".length());

					printWriterOut.println("Server: httpfs");

					printWriterOut.println("Date: " + new Date());

					printWriterOut.println("Content-type: " + "text/plain");

					printWriterOut.println();

					printWriterOut.flush();

					buffOutStream.write("Successfuly created \n".getBytes());

					buffOutStream.flush();

				}

			}

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static StringBuffer listAllFiles() {

		StringBuffer filesList = new StringBuffer();

		Path path = Paths.get("");

		String s = path.toAbsolutePath().toString();

		File file = new File(s + currentDir);

		File[] filesInDir = file.listFiles();

		for (File f : filesInDir) {

			filesList.append(f.toString());

			filesList.append("\n");

		}

		return filesList;

	}

	public static boolean checkIfFileExists(String fileName) {

		Path path = Paths.get("");

		String s = path.toAbsolutePath().toString();

		String completePath = s + currentDir + fileName;

		File file = new File(completePath);

		if (file.exists()) {

			return true;

		}

		return false;

	}

	public static int createNewFile(String url, String postData) throws IOException {

		String path = Paths.get(".").toAbsolutePath().toString();

		String newFilePath = path + currentDir + url;

		File file = new File(newFilePath);

		if (!file.exists()) {

			boolean fileCreated = file.createNewFile();

			LOGGER.info("File created" + fileCreated);

			Files.write(Paths.get(newFilePath), postData.getBytes());

		} else {

			LOGGER.info("File is already present updating content");

			Files.write(Paths.get(newFilePath), postData.getBytes());

		}

		return (int) file.length();

	}

	public static int getFileLength(String fileName) {

		Path path = Paths.get("");

		String s = path.toAbsolutePath().toString();

		String completePath = s + fileName;

		if (s != null) {

			File file = new File(completePath);

			return (int) file.length();

		} else {

			return 0;
		}

	}

	public static byte[] readContentFromFile(String url) throws IOException {

		String path = Paths.get(".").toAbsolutePath().toString();

		String newFilePath = path + url;

		byte[] content = null;

		File file = new File(newFilePath);

		if (file.exists()) {

			content = Files.readAllBytes(Paths.get(newFilePath));

		} else {

			content = "HTTP 404 :File Requested Not found ".getBytes();

		}

		return content;

	}

}
