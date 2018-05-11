package com.semlex.blockchainmanager;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;


public class BlockService {
	private List<Block> blockChain;
	public static MongoDatabase db ;
	Certificateur moi;
	private List<Certificateur> certificateurExterne;

	public BlockService(String url) {

		//Creation du lien vers la base de donnee MongoDb

		MongoClient mongoClient = new MongoClient(url);
		this.db =mongoClient.getDatabase("blockchain");
		this.blockChain = new ArrayList<Block>();
		moi = new Certificateur();

		//chargerCertificateurExterne();
		JsonWriterSettings settings = JsonWriterSettings.builder()
				.int64Converter((value, writer) -> writer.writeNumber(value.toString()))
				.build();

		// si la base  n est pas vide je charge la blockchain
		if (!(this.db.getCollection("blocks").count()==0)) {

			MongoCursor<Document>  cursor = this.db.getCollection("blocks").find().iterator();
			//cursor.sort(new BasicDBObject("index ", 1));

			while(cursor.hasNext()) {
				//addBlock(cursor.next());
				Document bl = cursor.next();
				bl.remove("_id");
				//System.out.println(bl.toJson(settings));
				Block tempBlock = new Block().changeJsontoBlock(bl.toJson(settings));
				addBlockLocal(tempBlock);
			}

			System.out.println("la base n est pas vide");

		} else {

			//si la base est vide je cree le premier block et je sauve la blockchain
			System.out.println("la base  est  vide");
			addBlock( getFirstBlock());



		}

		//this.blockChain = new ArrayList<Block>();
		//blockChain.add(this.getFristBlock());
	}

	private void chargerCertificateurExterne() {
		FindIterable<Document> results;

		results = this.db.getCollection("blocks").find(com.mongodb.client.model.Filters.eq("action", "publishcertifier") );

		if (results == null) {

		}
		else {
			for (Document document : results) {
				String identite= document.getString("param1");
				String clepublique = document.getString("param2");
				certificateurExterne.add(new Certificateur(identite,clepublique));

			}
		}
		// System.out.println(result.getInteger("indexCertificateur"));
		//return result.getInteger("indexCertificateur");

	}


	private String calculateHash(int index, String previousHash, long timestamp, String certificateur, int indexCertificateur, String indexCertificateurCrypte, String action, String param1, String param2) {
		StringBuilder builder = new StringBuilder(index);
		builder.append(previousHash).append(timestamp).append(certificateur);
		return CryptoUtil.getSHA256(builder.toString());
	}

	public Block getLatestBlock() {
		//System.out.println(blockChain.size());
		if (blockChain.size()==0){
			return getFirstBlock();

		}else
			return blockChain.get(blockChain.size() - 1);
	}

	private Block getFirstBlock() {
		return new Block(1, "0", System.currentTimeMillis(), "ADMIN", 1, "tutu", "publishcertifier","ADMIN" , "clepublique", "0");
	}

	public Block generateNextBlock(String action, String param1, String param2) {

		Block previousBlock = this.getLatestBlock();
		int nextIndex = previousBlock.getIndex() + 1;
		long nextTimestamp = System.currentTimeMillis();
		int indexcertificateur=moi.getIndexAction(moi.getIdentite())+1;
		String indexcertificateurcrypte=moi.SignerIndex(indexcertificateur,moi.cleprive) ;

		//System.out.println(indexcertificateurcrypte);

		String nextHash = calculateHash(nextIndex, previousBlock.getHash(), nextTimestamp,  moi.getIdentite(),  indexcertificateur,  indexcertificateurcrypte,  action,  param1,  param2);
		return new Block(nextIndex, previousBlock.getHash(), nextTimestamp, moi.getIdentite(), indexcertificateur, indexcertificateurcrypte,action, param1, param2, nextHash);
	}

	public void addBlock(Block newBlock) {
		if (isValidNewBlock(newBlock, getLatestBlock())) {
			//si bloc valide j effectue l action demande par la blockchain
			
			moi.effectuerAction(newBlock.action,newBlock.param1,newBlock.param2);
			
			//je sauvegarde le bloc valide dans la blockchain
			this.blockChain.add(newBlock);
			addBlocktoDb(newBlock);
		}
	}

	private void addBlockLocal(Block newBlock) {
		//  if (isValidNewBlock(newBlock, getLatestBlock())) {
		this.blockChain.add(newBlock);

		// }
	}

	private boolean isValidNewBlock(Block newBlock, Block previousBlock) {
		/*if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
            System.out.println("invalid index");
            return false;
        } else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            System.out.println("invalid previoushash");
            return false;
        } else {
            String hash = calculateHash(newBlock.getIndex(), newBlock.getPreviousHash(), newBlock.getTimestamp(),
                    newBlock.getData());
            if (!hash.equals(newBlock.getHash())) {
                System.out.println("invalid hash: " + hash + " " + newBlock.getHash());
                return false;
            }
        }*/

		System.out.println(moi.extractindexcrypte(newBlock.getIndexCertificateurCrypte(),moi.clepublique));
		//System.out.println(newBlock.getIndexCertificateurCrypte());
		return true;
	}

	public void replaceChain(List<Block> newBlocks) {
		if (isValidBlocks(newBlocks) && newBlocks.size() > blockChain.size()) {
			blockChain = newBlocks;
		} else {
			System.out.println("Received blockchain invalid");
		}
	}

	private boolean isValidBlocks(List<Block> newBlocks) {
		Block firstBlock = newBlocks.get(0);
		if (firstBlock.equals(getFirstBlock())) {
			return false;
		}

		for (int i = 1; i < newBlocks.size(); i++) {
			if (isValidNewBlock(newBlocks.get(i), firstBlock)) {
				firstBlock = newBlocks.get(i);
			} else {
				return false;
			}
		}
		return true;
	}

	public List<Block> getBlockChain() {
		return blockChain;
	}



	private void addBlocktoDb(Block b) {

		String jsontemp=b.changeBlocktoJson(b);

		Document doc = Document.parse(jsontemp);


		this.db.getCollection("blocks").insertOne(doc);

	}

	public ArrayList<String> listAllCertificateurs() {
		ArrayList<String> listcertificateur = new ArrayList<String>();
		
		FindIterable<Document> results = BlockService.db.getCollection("blocks").find(com.mongodb.client.model.Filters.eq("action", "publishcertifier"));
				//distinct("param1",com.mongodb.client.model.Filters.eq("action", "publishcertifier"),Document );
				
	
		for(Document doc : results) {
			if(!listcertificateur.contains(doc.getString("param1")))
            {
				listcertificateur.add(doc.getString("param1"));
            }
			//listcertificateur.add( doc.getString("param1"));
			//System.out.println(doc.getString("certificateur"));
		}
		return listcertificateur;

	}

	public ArrayList<String> listAllCandidates() {
		

		ArrayList<String> listcandidats = new ArrayList<String>();
		FindIterable<Document> results = BlockService.db.getCollection("blocks").find(com.mongodb.client.model.Filters.eq("action", "publishcandidat"));
		//distinct("param1",com.mongodb.client.model.Filters.eq("action", "publishcertifier"),Document );
		

		for(Document doc : results) {
			if(!listcandidats.contains(doc.getString("param1")))
		    {
				listcandidats.add(doc.getString("param1"));
		    }
			//listcertificateur.add( doc.getString("param1"));
			//System.out.println(doc.getString("certificateur"));
		}
		return listcandidats;
	}

}
