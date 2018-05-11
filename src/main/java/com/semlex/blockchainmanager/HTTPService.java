package com.semlex.blockchainmanager;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by sunysen on 2017/7/6.
 */
public class HTTPService {
    private BlockService blockService;
    private P2PService   p2pService;

    public HTTPService(BlockService blockService, P2PService p2pService) {
        this.blockService = blockService;
        this.p2pService = p2pService;
    }

    public void initHTTPServer(int port) {
        try {
            Server server = new Server(port);
            System.out.println("listening http port on: " + port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            context.addServlet(new ServletHolder(new BlocksServlet()), "/blocks");
            context.addServlet(new ServletHolder(new MineBlockServlet()), "/mineBlock");
            context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
            context.addServlet(new ServletHolder(new AddPeerServlet()), "/addPeer");
            context.addServlet(new ServletHolder(new ListCertifierServlet()), "/certifiers");
            context.addServlet(new ServletHolder(new ListCandidateServlet()), "/candidates");
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("init http server is error:" + e.getMessage());
        }
    }

    private class BlocksServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(JSON.toJSONString(blockService.getBlockChain()));
        }
    }


    private class AddPeerServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String peer = req.getParameter("peer");
            p2pService.connectToPeer(peer);
            resp.getWriter().print("ok");
        }
    }


    private class PeersServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            for (WebSocket socket : p2pService.getSockets()) {
                InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                resp.getWriter().print(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort());
            }
        }
    }


    private class MineBlockServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String jsonString = IOUtils.toString(req.getInputStream());
            JSONObject json = new JSONObject(jsonString);
            
            Block newBlock = blockService.generateNextBlock(json.getString("action"), json.getString("param1"), json.getString("param2"));
            blockService.addBlock(newBlock);
            p2pService.broatcast(p2pService.responseLatestMsg());
            String s = JSON.toJSONString(newBlock);
            System.out.println("block added: " + s);
            resp.getWriter().print(s);
        }
    }
    private class ListCertifierServlet extends HttpServlet {
    	 @Override
    	 protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    		 resp.addHeader("Access-Control-Allow-Origin", "*");
    	        resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    	        resp.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
    	        resp.addHeader("Access-Control-Max-Age", "1728000");
    		  resp.setContentType("application/json;charset=UTF-8");
    		  ServletOutputStream out = resp.getOutputStream();
    		  Gson gson = new Gson();
    		String result=gson.toJson(blockService.listAllCertificateurs());
    	        //String output = converter.convertToJson(blockService.listAllCertificateurs());
             //resp.getWriter().println(JSON.toJSONString(blockService.listAllCertificateurs()));
    	        out.print(result);
        	
         }
    	 
    }
    
    private class ListCandidateServlet extends HttpServlet {
   	 @Override
   	 protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
   		 resp.addHeader("Access-Control-Allow-Origin", "*");
   	        resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
   	        resp.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
   	        resp.addHeader("Access-Control-Max-Age", "1728000");
   		  resp.setContentType("application/json;charset=UTF-8");
   		  ServletOutputStream out = resp.getOutputStream();
   		  Gson gson = new Gson();
   		String result=gson.toJson(blockService.listAllCandidates());
   	        //String output = converter.convertToJson(blockService.listAllCertificateurs());
            //resp.getWriter().println(JSON.toJSONString(blockService.listAllCertificateurs()));
   	        out.print(result);
       	
        }
   	 
   }
    
    
}

