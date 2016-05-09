package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TransactionsHandler implements HttpHandler {

	private List<String> transactionsLog = new ArrayList<String>();

	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().toString();
		System.out.println("URI: " + path);
		System.out.println("Method: " + exchange.getRequestMethod());

		switch (exchange.getRequestMethod()) {
		case "GET":
			parseGetRequest(exchange, path);
			break;
		case "PUT":
			parsePutRequest(exchange, path);
			break;
		}

	}

	private void parseGetRequest(HttpExchange exchange, String path) throws IOException {

	}

	private void parsePutRequest(HttpExchange exchange, String path) throws IOException {
		if (path.contains("/transactions")) {
			System.out.println("Entered transactions");
			InputStream inputStream = exchange.getRequestBody();
			byte[] buffer = new byte[Server.MAX_PUT_SIZE];
			int offset = 0;
			while (offset < Server.MAX_PUT_SIZE) {
				int bytesRead = inputStream.read(buffer, offset, Server.MAX_PUT_SIZE - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			byte[] data = new byte[offset];
			System.arraycopy(buffer, 0, data, 0, offset);

			String transactionsString = new String(data, "UTF-8");
			if (!transactionsString.equals("")) {
				System.out.println("Transaction not empty!");
				String[] splittedTransactions = transactionsString.split(";;;");
				for (String transaction : splittedTransactions) {
					System.out.println("Transaction: " + transaction);
					String[] splittedTransaction = transaction.split("#");
					String hash = new String("transation");
					if (!transactionsLog.contains(new String(splittedTransaction[6]))) {
						transactionsLog.add(splittedTransaction[6]);
						System.out.println("Log does not contain this hash: " + splittedTransaction[6]);
						if (splittedTransaction[2].equals("_")) {
							System.out.println("Points won by biking.");
							UsersHandler.users.get(splittedTransaction[3])
									.setPoints(UsersHandler.users.get(splittedTransaction[3]).getPoints()
											+ Integer.valueOf(splittedTransaction[4]));
						} else {
							if ((UsersHandler.users.get(splittedTransaction[2]).getPoints()
									- Integer.valueOf(splittedTransaction[4])) >= 0) {
								System.out.println(
										"Sender points: " + UsersHandler.users.get(splittedTransaction[2]).getPoints());
								UsersHandler.users.get(splittedTransaction[2])
										.setPoints(UsersHandler.users.get(splittedTransaction[2]).getPoints()
												- Integer.valueOf(splittedTransaction[4]));
								System.out.println("Sender points final: "
										+ UsersHandler.users.get(splittedTransaction[2]).getPoints());
								System.out.println("Receiver points: "
										+ UsersHandler.users.get(splittedTransaction[3]).getPoints());
								UsersHandler.users.get(splittedTransaction[3])
										.setPoints(UsersHandler.users.get(splittedTransaction[3]).getPoints()
												+ Integer.valueOf(splittedTransaction[4]));
								System.out.println("Receiver points final: "
										+ UsersHandler.users.get(splittedTransaction[3]).getPoints());
							}
						}
					} else {
						System.out.println("Already contains hash");
					}
				}
			}
		}
	}
}
