package com.semlex.blockchainmanager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class Main {
    public static void main(String[] args) {
    	
        if (args != null && (args.length == 2 || args.length == 3)) {
            try {
                int httpPort = Integer.valueOf(args[0]);
                int p2pPort = Integer.valueOf(args[1]);
                BlockService blockService = new BlockService("localhost:27017");
                P2PService p2pService = new P2PService(blockService);
                p2pService.initP2PServer(p2pPort);
                if (args.length == 3 && args[2] != null) {
                    p2pService.connectToPeer(args[2]);
                }
                HTTPService httpService = new HTTPService(blockService, p2pService);
                httpService.initHTTPServer(httpPort);
            } catch (Exception e) {
                System.out.println("startup is error:" + e.getMessage());
            }
            
        } else {
        	
            System.out.println("usage: java -jar naivechain.jar 8080 6001");
            String configPath="config.properties";
   		 	Properties properties=new Properties();
   		 	try {
   		 		FileInputStream in =new FileInputStream(configPath);
   		 		properties.load(in);
   		 in.close();
   		 } catch (IOException e) {
   		 System.out.println("Unable to load config file.");
   		 }
   		 
   		 //let's do the magic
   		 int httpPort = Integer.parseInt(properties.getProperty("httpPort"));
		 int p2pPort = Integer.parseInt(properties.getProperty("p2pPort"));
		 String Peer = properties.getProperty("peers"); 
		 BlockService blockService = new BlockService("localhost:27017");
		 //blockService.getLastIndexCertificateur("SEMLEX");
		 Block t =blockService.generateNextBlock("publishcandidat", "BOB", blockService.moi.getClepubliqueToString());
		
		// blockService.addBlock(t);
		 System.out.println(blockService.listAllCertificateurs());
         P2PService p2pService = new P2PService(blockService);
         p2pService.initP2PServer(p2pPort);
         HTTPService httpService = new HTTPService(blockService, p2pService);
         httpService.initHTTPServer(httpPort);
   		 
        }
    }
}
