package ca.concordia.comp6461;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class FileUtils {
	
	private static Logger LOGGER = Logger.getLogger(HttpFileServer.class.getName());


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
	
	public static boolean checkIfFileExists(String fileName, String currentDir) {

		Path path = Paths.get("");

		String s = path.toAbsolutePath().toString();

		String completePath = s + currentDir + fileName;

		File file = new File(completePath);

		if (file.exists()) {

			return true;

		}

		return false;

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
	
	
	public static int createNewFile(String url, String postData, String currentDir) throws IOException {

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
	
	
	public static StringBuffer listAllFiles(String currentDir) {

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


}
