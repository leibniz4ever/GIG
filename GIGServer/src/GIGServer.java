//package edu.utah.fv.gig.server;
//
//import java.io.*;
//import java.net.ServerSocket;
//
//import javax.net.ssl.*;
//
//public class GIGServer {
//
//	public static void main(String[] args) {
//		int port = 8883;
//		
//		ServerSocket s;
//		
////		String key, value;
////		/*
////		 * -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=123456
////		 */
////		key = "javax.net.ssl.keyStore";
////		value = "mySrvKeystore";
////		System.setProperty(key, value);
////		key = "javax.net.ssl.keyStorePassword";
////		value = "123456";
////		System.setProperty(key, value);
//		
//		try {
//		    SSLServerSocketFactory sslSrvFact =
//		        (SSLServerSocketFactory)
//		        SSLServerSocketFactory.getDefault();
//		    s =(SSLServerSocket)sslSrvFact.createServerSocket(port);
//		
//		    SSLSocket c = (SSLSocket)s.accept();
//		
//		    OutputStream out = c.getOutputStream();
//		    InputStream in = c.getInputStream();
//		    System.out.println("Hello");
//
//		    // Send messages to the client through
//		    // the OutputStream
//		    // Receive messages from the client
//		    // through the InputStream
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Bye");
//		}
//	}
//}





import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GIGServer {

	private static ServerSocket serverSocket;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		while(true) {
			try {
				startServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static void startServer() throws IOException {
		System.out.println("Starting Server");
		int port = 8883;
		//serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
		serverSocket = new ServerSocket(port);
		while(true) {
			try {
				System.out.println("Waiting for Connection");
				Socket socket = serverSocket.accept();
				System.out.println("Connection received");
				ServerThread serverThread = new ServerThread(socket);
				serverThread.start();
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}

}
