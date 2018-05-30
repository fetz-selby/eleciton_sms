package com.election.sms.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.election.sms.SendObject;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class CandidateRequest {
	private Connection con;
	private String msisdn, constituencyId, constituencyName, str = "";
	
	public CandidateRequest(Connection con, String msisdn){
		this.con = con;
		this.msisdn = msisdn;
	}

	public void fetchCandidates(){
		doFetching();
	}
	
	private void doFetching(){
		if(isCorrespondent()){
			Statement stmt = null;
			try{
				stmt = (Statement) con.createStatement();
				ResultSet result = stmt.executeQuery("select candidates.id, candidates.name as candidate, parties.name as party from candidates, parties where candidates.constituency_id = "+constituencyId+" and parties.id = candidates.party_id");
				if(result != null){
					while(result.next()){
						int id = result.getInt("id");
						String candidate = result.getString("candidate");
						String party = result.getString("party");
					
						str += id +"   "+candidate+" ("+party+")\n";
					}
										
					SendObject sender = new SendObject();
					sender.setMsisdn(msisdn);
					sender.setMessage(getPreparedMsg());
					sender.sendMessage();
					
				}
			}catch(SQLException sql){
				sql.printStackTrace();
			}
		}
	}
	
	private boolean isCorrespondent(){
		Statement stmt = null;
		try{
			stmt = (Statement) con.createStatement();
			ResultSet result = stmt.executeQuery("select correspondents.constituency_id, constituencies.name as constituency from correspondents, constituencies where msisdn = "+msisdn+" and constituencies.id = correspondents.constituency_id");
			if(result != null){
				while(result.next()){
					constituencyId = result.getString("constituency_id");
					constituencyName = result.getString("constituency");
										
					return true;
				}
			}
		}catch(SQLException sql){
			sql.printStackTrace();
		}
		return false;
	}
	
	private String getPreparedMsg(){
		String msg = constituencyName+"\n--------------------------\n"+str;
		return msg;
	}
}
