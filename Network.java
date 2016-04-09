package part2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network implements Runnable {
	public List <InetAddress> addrBuffer;
	public List <List <InetAddress> > totalAddrBuffer;
	public List <List <Integer> > totalCostBuffer;
	
	
	Network() {
		addrBuffer = new ArrayList <>();
		totalAddrBuffer = new ArrayList <>();
		totalCostBuffer = new ArrayList <>();
	}
	
	public boolean sendDistVector(InetAddress src, 
								  InetAddress dest,
								  List <InetAddress> totalAddr,
								  List <Integer> totalCost) {
		
		try (Socket socket = new Socket(dest, 8888)) {
			
			StringBuilder build = new StringBuilder();
			build.append(src.toString().substring(1));
			build.append(' ');
			
			for (int i = 0; i < totalAddr.size(); i++) {
				InetAddress addr = totalAddr.get(i);
				int cost = totalCost.get(i);
				build.append(addr.toString().substring(1));
				build.append(' ');
				build.append(cost);
				
				if (i + 1 < totalAddr.size()) {
					build.append(' ');
				}
			}
			
			String msg = build.toString();
			OutputStream ostream = socket.getOutputStream();
			
                        ostream.write(msg.getBytes());
			return true;
		} catch (IOException ex) {
			return false;
		}
		
	}
	
	private void parseAndAdd(String msg) throws Exception {
		String[] parts = msg.split(" ");
		InetAddress src = InetAddress.getByName(parts[0]);
		int pos = 1;
		
		List <InetAddress> addr = new ArrayList <>();
		List <Integer> cost = new ArrayList <>();
		
		while (pos < parts.length) {
			addr.add(InetAddress.getByName(parts[pos]));
			cost.add(Integer.parseInt(parts[pos + 1]));
			pos += 2;
		}
		
		addrBuffer.add(src);
		totalAddrBuffer.add(addr);
		totalCostBuffer.add(cost);
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[4096];
		int buflen = 0;
		
		try (ServerSocket serverSocket = new ServerSocket(8888)) {
			while (true) {
				try (Socket socket = serverSocket.accept()) {
					InputStream istream = socket.getInputStream();
                                        
					if ((buflen = istream.read(buffer)) != -1) {
						String msg = new String(buffer, 0, buflen);
                                                try {
							parseAndAdd(msg);
						} catch (Exception ex) {
							System.out.println("Error in pasing: " + ex.getMessage());
						}
					}
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
	}
}
