package com.election.sms.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.election.sms.SendObject;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

public class DBEntry {
	private String msisdn, msg, candidate, constituency, party, type;
	private int constituencyId, candidateId, canVotes;
	private int correspondentId;
	private Connection con;
	private PreparedStatement prstmt;
	
	public DBEntry(Connection con, String msisdn, String msg, String type){
		this.con = con;
		this.msg = msg;
		this.msisdn = msisdn;
		this.type = type;
	}
	
	public void load(){		
		if(msisdn.startsWith("+")){
			msisdn = msisdn.substring(1);
		}
			
		String[] tokens = msg.split("[\\s]+");
		if(type.equals("M")){
			doPaliamentarianStuff(msisdn, tokens[0], Integer.parseInt(tokens[1]));
		}else{
			doPresidentialStuff(msisdn, tokens[0], Integer.parseInt(tokens[1]));
		}
		System.out.println("*** Execution done ***");
	}
	
	private boolean isValidEntry(String candidateId){
		Statement stmt = null;
		try{
			stmt = (Statement) con.createStatement();
			ResultSet result = stmt.executeQuery("select candidates.constituency_id, candidates.name as candidate, parties.name as party, constituencies.name as constituency from candidates, parties, constituencies where candidates.id = "+candidateId+" and parties.id = candidates.party_id and constituencies.id = candidates.constituency_id");
			if(result != null){
				int constituencyId = 0;
				while(result.next()){
					constituencyId = result.getInt("constituency_id");
					candidate = result.getString("candidate");
					constituency = result.getString("constituency");
					party = result.getString("party");
				}
				if(constituencyId == this.constituencyId){
					return true;
				}else{
					candidate = "";
					constituency = "";
					party = "";
					return false;
				}
			}
		}catch(SQLException sql){
			sql.printStackTrace();
		}
		
		return false;
	}
	
	private boolean isValidCode(String candidateCode){
		//Statement stmt = null;
		PreparedStatement prstmt = null;
		try{
			candidateCode = candidateCode.toUpperCase();
			con.setAutoCommit(false);
			prstmt = (PreparedStatement) con.prepareStatement("select candidates.id, candidates.name as candidate, constituencies.name as constituency, parties.name as party from candidates, constituencies, parties where candidates.constituency_id = ? and candidates.code = ? and candidates.type = 'P' and constituencies.id = candidates.constituency_id and parties.id = candidates.party_id");
			prstmt.setInt(1, constituencyId);
			prstmt.setString(2, candidateCode);
			
			ResultSet result = prstmt.executeQuery();
			if(result != null){
				while(result.next()){
					candidateId = result.getInt("id");
					candidate = result.getString("candidate");
					constituency = result.getString("constituency");
					party = result.getString("party");
					return true;
				}
			}else{
				candidateId = 0;
				candidate = "";
				constituency = "";
				party = "";
				return false;
			}
			
			/*candidateCode = candidateCode.toUpperCase();
			stmt = (Statement) con.createStatement();
			System.out.println("Constituency id"+constituencyId+" candidate code is "+candidateCode);
			ResultSet result = stmt.executeQuery("select candidates.id, candidates.name as candidate, constituencies.name as constituency, parties.name as party from candidates, constituencies, parties where candidates.constituency_id = "+constituencyId+" and candidates.code = "+candidateCode+" and candidates.type = 'P' and constituencies.id = candidates.constituency_id and parties.id = candidates.party_id");
			if(result != null){
				while(result.next()){
					candidateId = result.getInt("id");
					candidate = result.getString("candidate");
					constituency = result.getString("constituency");
					party = result.getString("party");
					return true;
				}
			}else{
				candidateId = 0;
				candidate = "";
				constituency = "";
				party = "";
				return false;
			}*/
		}catch(SQLException sql){
			sql.printStackTrace();
		}
		return false;
	}
	
	private void doPaliamentarianStuff(String msisdn, String candidateId, int votes){
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement)con.prepareStatement("select id,constituency_id from correspondents where msisdn = ?");
			prstmt.setString(1, msisdn);
			ResultSet result = prstmt.executeQuery();
			if(result != null){
				while(result.next()){
					constituencyId = result.getInt("constituency_id");
					correspondentId = result.getInt("id");
				}
				if(constituencyId > 0 && isValidEntry(candidateId)){
					//Putting it into the polls db
					prstmt = (PreparedStatement) con.prepareStatement("insert into polls (correspondent_id, constituency_id, candidate_id, votes) values (?, ?, ?, ?)");
					prstmt.setInt(1, correspondentId);
					prstmt.setInt(2, constituencyId);
					prstmt.setInt(3, Integer.parseInt(candidateId));
					prstmt.setInt(4, votes);
					
					canVotes = votes;
					
					if(prstmt.executeUpdate() > 0){
						
						SendObject send = new SendObject();
						send.setMessage(getPreparedMessage());
						send.setMsisdn(msisdn);
						send.sendMessage();
						
						System.out.println("Entry Added Successfully!!!");
					}else{
						System.out.println("Save was unSuccessfull");
					}
				}else{
					System.out.println("DB insertion error");
				}
			}else{
				System.out.println("Result is NULL");
			}
			
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}
	
	private void doPresidentialStuff(String msisdn, String candidateCode, int votes){
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement)con.prepareStatement("select id,constituency_id from correspondents where msisdn = ?");
			prstmt.setString(1, msisdn);
			ResultSet result = prstmt.executeQuery();
			if(result != null){
				while(result.next()){
					constituencyId = result.getInt("constituency_id");
					correspondentId = result.getInt("id");
				}
				if(constituencyId > 0 && isValidCode(candidateCode)){
					//Putting it into the polls db
					prstmt = (PreparedStatement) con.prepareStatement("insert into polls (correspondent_id, constituency_id, candidate_id, votes) values (?, ?, ?, ?)");
					prstmt.setInt(1, correspondentId);
					prstmt.setInt(2, constituencyId);
					prstmt.setInt(3, candidateId);
					prstmt.setInt(4, votes);
					
					canVotes = votes;
					
					if(prstmt.executeUpdate() > 0){
						
						SendObject send = new SendObject();
						send.setMessage(getPreparedMessage());
						send.setMsisdn(msisdn);
						send.sendMessage();
						
						System.out.println("Entry Added Successfully!!!");
					}else{
						System.out.println("Save was unSuccessfull");
					}
				}else{
					System.out.println("DB insertion error");
				}
			}else{
				System.out.println("Result is NULL");
			}
			
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}
	
	private String getPreparedMessage(){
		String msg = canVotes+" was voted on behalf of "+candidate+"("+party+")"+" from "+constituency+" constituency. \nThank you.";
		return msg;
	}
}
