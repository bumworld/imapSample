package imap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;

public class IMAPEmailSample {

	private static String host = "imap.daum.net";
//	private static String host = "imap.kakao.com";
	private static String userEmail = "";
	private static String password = "";

	private static int MAX_COUNT = 5;

	public static void main(String[] args) throws Exception {
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		System.out.println("-- " + currentTime + "IMAP Emal 가져오기: Start\n\n");

		String emlDirectoryPath = Paths.get("").toAbsolutePath().toString() + "/src/main/java/imap/eml";

		IMAPMailService mailService = new IMAPMailService(host, userEmail, password);
		Message[] msgArray = mailService.getMessages(false);
		int messageCount = mailService.getMessageCount();
		if (messageCount > MAX_COUNT) {
			messageCount = MAX_COUNT;
		}

		for (int i = 0; i < messageCount; i++) {
			System.out.println(ConsoleColor.MAGENTA_BOLD_BRIGHT
					+ "============================================================================="
					+ ConsoleColor.RESET + System.lineSeparator());
			Message msg = msgArray[i];

			String messageId = msg.getHeader("Message-ID")[0];
			String fileName = new String(Base64.getEncoder().encode(messageId.getBytes()), "utf-8");

			System.out.println(
					ConsoleColor.MAGENTA_BOLD_BRIGHT + "[ Header 조회 ]" + ConsoleColor.RESET + System.lineSeparator());
			StringBuilder logBuilder = new StringBuilder();
			Enumeration<Header> headerEnum = msg.getAllHeaders();
			while (headerEnum.hasMoreElements()) {
				Header hdr = headerEnum.nextElement();
				logBuilder.append(hdr.getName() + " : " + hdr.getValue()).append(System.lineSeparator());
			}
			// System.out.println(logBuilder);

			System.out.println(ConsoleColor.MAGENTA_BOLD_BRIGHT + "[ 제목 ]" + ConsoleColor.RESET);
			System.out.println(ConsoleColor.MAGENTA_BOLD_BRIGHT + "제목 : " + ConsoleColor.RESET + msg.getSubject());

			System.out.println(ConsoleColor.MAGENTA_BOLD_BRIGHT + "[ 내용 ]" + ConsoleColor.RESET);
			System.out.println(ConsoleColor.MAGENTA_BOLD_BRIGHT + "내용 : " + ConsoleColor.RESET
					+ mailService.getTextFromMessage(msg));

			try (FileOutputStream outputStream = new FileOutputStream(
					new File(emlDirectoryPath + "/" + currentTime + "-" + fileName + ".eml"));
					BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
				msg.writeTo(bos);
			} catch (IOException | MessagingException e) {
				e.printStackTrace();
			}
		}
		mailService.logout(); // 로그아웃
		System.out.println("\n\n-- IMAP Emal 가져오기: 종료");
	}
}