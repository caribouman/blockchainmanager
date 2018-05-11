package com.semlex.blockchainmanager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bson.Document;

class Certificateur implements  ActionCertificateur{

	String identite;
	int indexAction;
	PublicKey clepublique;
	PrivateKey cleprive;



	public Certificateur() {
		try {
			AsymmetricCryptography ac =new AsymmetricCryptography();

			this.cleprive = ac.getPrivate("Keypair/privateKey");
			this.clepublique = ac.getPublic("Keypair/publicKey");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		this.indexAction=getIndexAction(this.identite);

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
		this.identite=properties.getProperty("identite");



	}

	public Certificateur(String identite,String clepublique) {
		this.identite=identite;

		// rebuild key using SecretKeySpec
		try {
			this.clepublique=setPublicKey(clepublique);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.indexAction=getIndexAction(identite);
	}

	public PublicKey setPublicKey(String clepublique) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(clepublique);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}



	public String getIdentite() {
		return identite;
	}



	public int getIndexAction(String certificateur) {

		Document result = BlockService.db.getCollection("blocks").find(com.mongodb.client.model.Filters.eq("certificateur", certificateur) ). sort( com.mongodb.client.model.Sorts.orderBy(com.mongodb.client.model.Sorts.descending("indexCertificateur"))).first();
		if (result == null) {
			return 0;
		}else {
			//System.out.println(result.getInteger("indexCertificateur"));
			setIndexAction(result.getInteger("indexCertificateur"));
			return result.getInteger("indexCertificateur");
		}

	}

	private void setIndexAction(int indexAction) {

		this.indexAction = indexAction;
	}

	public String SignerIndex(int indexcertificateur,PrivateKey cleprive) {
		AsymmetricCryptography ac;
		String encode =null;
		try {
			ac = new AsymmetricCryptography();
			encode = ac.encryptText(String.valueOf(indexcertificateur),cleprive );
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encode;
	}

	public int extractindexcrypte(String indexCertificateurCrypte, PublicKey clepublique) {
		AsymmetricCryptography ac;
		String decode =null;
		try {
			ac = new AsymmetricCryptography();

			decode = ac.decryptText(indexCertificateurCrypte,clepublique );
		} catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			return -1;
		}
		System.out.println(decode);
		return Integer.parseInt(decode);
	}	



	public String getClepubliqueToString() {
		return new String(Base64.getEncoder().encode(clepublique.getEncoded()));
		//return clepublique.getEncoded();
	}

	public void effectuerAction(String action, String param1, String param2) {
		// TODO Auto-generated method stub
		
	}

}