package imap;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * imap service
 *
 * @author bumworld
 *
 */
public class IMAPMailService {
	private Session session;
	private Store store;
	private Folder folder;
	private String protocol = "imaps";
	private String file = "INBOX";

	public IMAPMailService(String host, String username, String password) throws Exception {
		URLName url = new URLName(protocol, host, 993, file, username, password);
		session = Session.getInstance(TestEmailProperties.getServerProperties(), null);
		store = session.getStore(url);
		store.connect();
		folder = store.getFolder("inbox"); // inbox는 받은 메일함을 의미
		folder.open(Folder.READ_ONLY); // 읽기 전용
	}

	public void logout() throws MessagingException {
		folder.close(false);
		store.close();
		store = null;
		session = null;
	}

	public boolean isLoggedIn() {
		return store.isConnected();
	}

	/**
	 * 메일 본문 텍스트
	 *
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public String getTextFromMessage(Message message) throws Exception {
		if (message.isMimeType("text/plain")) {
			String contentStr = (String) message.getContent();
			if (StringUtils.isBlank(contentStr)) {
				contentStr = IOUtils.toString(message.getInputStream(), "utf-8");
			}
			return contentStr;
		} else if (message.isMimeType("text/html")) {
			String contentStr = (String) message.getContent();
			if (StringUtils.isBlank(contentStr)) {
				contentStr = IOUtils.toString(message.getInputStream(), "utf-8");
			}
			return org.jsoup.Jsoup.parse(contentStr).text();
		} else if (message.isMimeType("MimeMultipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMultipart(mimeMultipart);
		} else if (message.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) message.getContent();
			return getTextFromMultipart(multipart);
		} else {
			System.out.println(ConsoleColor.RED_BOLD_BRIGHT + "getTextFromMessage 알수 없는 타입! " + message.getContentType()
					+ ConsoleColor.RESET);
		}
		return "";
	}

	private String getTextFromMultipart(Multipart multipart) throws Exception {
		String result = "";
		int count = multipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = multipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				String contentStr = (String) bodyPart.getContent();
				if (StringUtils.isBlank(contentStr)) {
					contentStr = IOUtils.toString(bodyPart.getInputStream(), "utf-8");
				}
				result += System.lineSeparator() + contentStr;
			} else if (bodyPart.isMimeType("text/html")) {
				String contentStr = (String) bodyPart.getContent();
				if (StringUtils.isBlank(contentStr)) {
					contentStr = IOUtils.toString(bodyPart.getInputStream(), "utf-8");
				}

				result += System.lineSeparator() + org.jsoup.Jsoup.parse(contentStr).text();
			} else if (bodyPart.getContent() instanceof Multipart) {
				result += getTextFromMultipart((Multipart) bodyPart.getContent());
			} else {
				System.out.println(ConsoleColor.RED_BOLD_BRIGHT + "getTextFromMimeMultipart 알수 없는 타입! "
						+ bodyPart.getContentType() + ConsoleColor.RESET);
			}
		}
		return result;
	}

	public int getMessageCount() {
		// TODO: 안 읽은 메일의 건수만 조회하는 기능 추가
		int messageCount = 0;
		try {
			messageCount = folder.getMessageCount();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
		return messageCount;
	}

	/**
	 * 이메일 리스트를 가져옴
	 *
	 * @param onlyNotRead 안읽은 메일 리스트만 가져올지 여부
	 * @return
	 * @throws MessagingException
	 */
	public Message[] getMessages(boolean onlyNotRead) throws MessagingException {
		if (onlyNotRead) {
			return folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		} else {
			return folder.getMessages();
		}
	}
}