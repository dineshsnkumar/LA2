package ca.concordia.comp6461;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class HttpFileServer {

	private static int port = 8080;

	private static boolean verbose = true;

	private Socket socket;

	public static void main(String[] args) {

		try {

			ServerSocket server = new ServerSocket(port);

			System.out.println("Server is listening at port:" + port);

			while (true) {

				Socket client = server.accept();

				BufferedReader buffreader = new BufferedReader(new InputStreamReader(client.getInputStream()));

				String line = buffreader.readLine();

				String[] parsedString = line.split("\\s+");

				String method = parsedString[0];

				String fileName = parsedString[1];

				if (method.equalsIgnoreCase("GET") && fileName.length() > 1) {

					PrintWriter printWriterOut = new PrintWriter(client.getOutputStream());

					BufferedOutputStream buffOutStream = new BufferedOutputStream(client.getOutputStream());

					int checkFileExists = getFileLength(fileName);

					if (checkFileExists > 0) {

						printWriterOut.println("HTTP/1.1 200 OK");
						
						printWriterOut.println("Content-length: " + checkFileExists);

					} else {

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

				}

				if (method.equalsIgnoreCase("POST")) {

				}

			}

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static int getFileLength(String fileName) {

		String path = fileName.substring(1);

		URL url = HttpFileServer.class.getResource(path);

		if (url != null) {

			File file = new File(url.getPath());

			return (int) file.length();

		} else {

			return 0;
		}

	}

	public static byte[] readContentFromFile(String path) throws IOException {

		String file = path.substring(1);

		URL url = HttpFileServer.class.getResource(file);

		byte[] content = null;

		if (url != null) {

			content = Files.readAllBytes(Paths.get(url.getPath()));

		} else {

			content = "HTTP 404 :File Requested Not found ".getBytes();

		}

		return content;

	}

}
