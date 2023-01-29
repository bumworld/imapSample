package imap;


import java.util.Properties;

public class TestEmailProperties {

	public static boolean debug = false;

	static {
		// 메일에 대한 여러 가지 속성 지정해두기 - static이기 때문에 가장 먼저 진행되는 작업이다.
		// MimeMessage에서 parseHeader를 통해 헤더를 분석하는데, 이때 strict 하게 분석하도록 하지 않으려고 false로 설정
		// (다양한 메시지를 받기 위해)
		System.setProperty("mail.mime.address.strict", "false");
		System.setProperty("mail.mime.decodetext.strict", "false");
		System.setProperty("mail.mime.ignoreunknownencoding", "true");

		// BASE64Decoder: Error in encoded stream: needed at least 2 valid base64
		// characters
		System.setProperty("mail.mime.base64.ignoreerrors", "true");
	}

	/**
	 * 서버와 관련된 프로퍼티 가져오기
	 *
	 * @return
	 */
	public static Properties getServerProperties() {
		Properties properties = new Properties();

		// properties 객체에 키와 값을 저장해둘 수 있다.
		// SSL setting
		properties.setProperty(String.format("mail.%s.socketFactory.class", "imap"), "javax.net.ssl.SSLSocketFactory");
		properties.setProperty(String.format("mail.%s.socketFactory.fallback", "imap"), "false");
		properties.setProperty(String.format("mail.%s.ssl", "imap"), "true");

		if (debug) {
			properties.put("mail.debug", "true");
		}

		/**
		 * 설정 정보는 여기에서 확인 가능 {@link com.sun.mail.imap.IMAPStore)
		 */

		// 가장 기본 옵션
		properties.setProperty("mail.store.protocol", "imaps");

		// 구글에서는 imap mailbox에 대해 15개의 커넥션이 한계. 여기서는 서버 커넥션의 최대 개수를 지정해준다.
		// 근데 이 옵션이 작동하지 않는 것 같아서 우선 꺼두었다.
//         properties.setProperty("mail.imap.connectionpoolsize", "50");

		// 커넥션에 대한 타임아웃을 걸어둔다. 기본은 45000 (45초), 여기서는 3분으로 지정.
		properties.setProperty("mail.imap.connectionpooltimeout", "180000");
		// idle time에 대해 설정한다. 기본으로는 10 milliseconds, 여기서는 10분으로 설정.
		properties.setProperty("mail.imap.minidletime", "100000");

		if (debug) {
			properties.setProperty("mail.imap.connectionpool.debug", "false");
		}

		/**
		 * Fix : JavaMail BaseEncode64 Error Certain IMAP servers do not implement the
		 * IMAP Partial FETCH functionality properly. This problem typically manifests
		 * as corrupt message attachments when downloading large messages from the IMAP
		 * server. To workaround this server bug, set the "mail.imap.partialfetch"
		 * property to false. You'll have to set this property in the Properties object
		 * that you provide to your Session. So you should just turn off partial fetch
		 * in imap session.
		 *
		 * @see <a href=
		 *      "http://stackoverflow.com/questions/1755414/javamail-baseencode64-error">JavaMail
		 *      BaseEncode64 Error</a>
		 * @see <a href=
		 *      "http://stackoverflow.com/questions/11999030/com-sun-mail-util-decodingexception-base64decoder-error-in-encoded-stream-retr">com.sun.mail.util.DecodingException:
		 *      BASE64Decoder: Error in encoded stream retreiving mail from Yahoo</a>
		 */
		properties.put("mail.imap.fetchsize", "819200"); // Partial fetch size in bytes. Defaults to 16K. (1024 * 16)
		properties.setProperty("mail.imap.partialfetch", "false");
		properties.setProperty("mail.imaps.partialfetch", "false");

		return properties;
	}
}
