package com.semlex.blockchainmanager;

import com.google.gson.Gson;

/**
 * Created by sunysen on 2017/7/6.
 */
public class Block {
    private int    index;
    private String previousHash;
    private long   timestamp;
	

	public String certificateur ;
	public int indexCertificateur ;
	

	public String indexCertificateurCrypte ;
	public String action ;
	public String param1 ;
	public String param2 ;
	public String nonce ;
    private String hash;

    public Block() {
    }

    public Block(int index, String previousHash, long timestamp, String certificateur, int indexCertificateur, String indexCertificateurCrypte, String action, String param1, String param2, String hash) {
        this.index = index;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.certificateur = certificateur;
		this.indexCertificateur = indexCertificateur;
		this.indexCertificateurCrypte = indexCertificateurCrypte;
		this.action = action;
		this.param1 = param1;
		this.param2 = param2;
        this.hash = hash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

   

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public int getIndexCertificateur() {
		return indexCertificateur;
	}

	public void setIndexCertificateur(int indexCertificateur) {
		this.indexCertificateur = indexCertificateur;
	}
	
	public String getIndexCertificateurCrypte() {
		return indexCertificateurCrypte;
	}

	public void setIndexCertificateurCrypte(String indexCertificateurCrypte) {
		this.indexCertificateurCrypte = indexCertificateurCrypte;
	}
    
	public Block changeJsontoBlock(String json) {
		Gson gson = new Gson();
		Block bl = gson.fromJson(json,Block.class);
		return bl;


	}
	
	public String changeBlocktoJson(Block block) {
		Gson gson = new Gson();
		String result=gson.toJson(block);
		return result;


	}
}

