/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

import rules.PLAYER;



/**
 *
 * @author julia
 */

public class DBHandler {
    
    public static Connection connection;
    public static Random turn=new Random();

    public static final String driver = "com.mysql.jdbc.Driver";
    public static final String url = "jdbc:mysql://localhost/";
    public static final String DBName = "Chaturaji";
    public static final String DBuser="chaturajiUser";
    public static final String DBpassword="chaturajiUserPW";
    private static int gameCounter;

    public DBHandler(){
        gameCounter = 0;
        try{
            Class.forName(driver); 
        }catch(ClassNotFoundException e){
            System.err.println("Driver not found!");
        }

        try{	
            connection = DriverManager.getConnection(url+DBName,DBuser,DBpassword);
            //Statement statement = connection.createStatement();

        }catch(SQLException ex){
            System.err.println("Connection failed!");
            ex.printStackTrace();
        }
        
        //drop existing tables
        //dropTables();

        // Creat new tables UserInfo, GameLobbyInfo and GameBoardInfo       
        /*try{    
        	 String sql;
             //UserInfo table
             Statement statement = connection.createStatement();
             sql = "CREATE TABLE UserInfo (UserName VARCHAR(45) NOT NULL, ";
             sql+= "PRIMARY KEY(UserName))";
             statement.executeUpdate(sql);
             System.out.println("UserInfo Table Created");

             //GameLobbyInfo table
             sql = "CREATE TABLE GameLobbyInfo (GameID VARCHAR(45) NOT NULL,"; 
             sql+= "P1 VARCHAR(45) NULL,P2 VARCHAR(45) NULL,P3 VARCHAR(45) NULL,P4 VARCHAR(45) NULL,";
             sql+= "PRIMARY KEY (GameID))";
             statement.executeUpdate(sql);
             System.out.println("GameLobbyInfo Table created");

             //GameBoardInfo table
             sql = "CREATE TABLE GameBoardInfo (GameID VARCHAR(45) NOT NULL, MoveRecord VARCHAR(5000) NULL,";
             sql+= "PRIMARY KEY (GameID))";
             statement.executeUpdate(sql);
             System.out.println("GameBoardInfo Table created");

         }catch(SQLException ex){
             System.err.println("Table creation failed.");
             ex.printStackTrace();
         }   */
    }

    //******  Page 1  *******//

    //Check whether current user is a new user
    public static boolean newUser(String userName){
        try{
            Statement statement = connection.createStatement();
            String sql= "SELECT UserName FROM UserInfo WHERE UserName='" + userName + "';";
            ResultSet result=statement.executeQuery(sql);
            return !result.first();
        }catch(SQLException ex){
            System.err.println("newUser() failed.");
            ex.printStackTrace();
        }
     
        return false;
    }
	
    // Check for valid string
    public static boolean isValidUserString(String userName){
    	int minUserString = 3;
    	int maxUserString = 15;
    	int strLen = userName.length();
    	if (strLen < minUserString || strLen > maxUserString) {
    		return false;
    	}
    	if (userName.substring(0,2).equals("AI")) {
    		return false;
    	}
    	for (int i = 0; i < strLen; i++) {
    		int c = userName.charAt(i);
    		System.out.println(c);
    		if (c < (int)'!' || c > (int)'~') {
    			// only ASCII between 33 and 126
    			return false;		
    		}
    	}
    	return true;
    }
    
    // Check whether current user is valid or not
    public static boolean validateUser(String userName){//, String password){
    	if(isValidUserString(userName) && newUser(userName)){
    		insertUserInfo(userName);//,password);
            System.out.println("new user: " + userName);
            return true;
    	}else{
    		return false;
    	}
    }
//    	
//    	//insertUserInfo(userName,password);
//        //return true;
//        try{
//            Statement statement = connection.createStatement();
//            if(newUser(userName)){
//                
//                insertUserInfo(userName);//,password);
//                System.out.println("new user: " + userName);
//                return true;
//                
//            }else{//user already logged-in with this username
//            	return false;
//            	
////            	
////                String sql= "SELECT Password FROM UserInfo WHERE UserName='" + userName+"'";
////                ResultSet result=statement.executeQuery(sql);
////                System.out.println("not new user");
////                while(result.next()){
////                    //System.out.println(result.getString(1)+ " length is " + result.getString(1).length());
////                    //System.out.println(password + " length is " + password.length());
////                    return result.getString(1).equals(password);
////                }
//            }
//        }catch(SQLException ex){
//            System.err.println("validateUser() failed.");
//            ex.printStackTrace();
//        }
//        
//        return false;
//    }	
	
    // Add new uer into table
    public static void insertUserInfo(String userName){//, String password){
        
        try{
            Statement statement = connection.createStatement();
            String sql= "INSERT INTO UserInfo VALUES('" + userName + "')";//,'" +password + "')";
            statement.executeUpdate(sql);
        }catch(SQLException ex){
            System.err.println("insertUserInfo() failed.");
            ex.printStackTrace();
        }
    }

    //******  Page 2  *******//

    // Add New Game Entry
    public static void addNewGameEntry(String gameID, String player){
        try{
            Statement statement = connection.createStatement();
            String sql= "INSERT INTO GameLobbyInfo ( GameID, P1,P2,P3,P4) VALUES('" + gameID + "', null,null,null,null)";
            statement.executeUpdate(sql);
            addNewPlayer(gameID,player);
            addGameMoveRecordEntry(gameID);
        }catch(SQLException ex){
            
            System.err.println("addNewGameEntry() failed.");
            ex.printStackTrace();
        }
    }
	
    // Add new player into a specific game board
    public static void addNewPlayer(String gameID, String player){
        try{
            Statement statement = connection.createStatement();
            String order=orderGenerator(gameID);
            //System.out.println("Order: " + order);
            String sql= "UPDATE GameLobbyInfo SET " + order+ "='"+player+"' WHERE GameID='"+ gameID +"'";
            statement.executeUpdate(sql);
        }catch(SQLException ex){
            System.err.println("addNewPlayer() failed." );
            ex.printStackTrace();
       }
    }
	
    // Random player order generator
    public static String orderGenerator(String gameID){
        Vector<String> playerList = getPlayerList(gameID);
        //System.out.println("In orderGenerator()");
        Set<Integer> currentPlayerID = new HashSet<Integer>();
        int index=1;
        int playOrder;
        int size=playerList.size();
	           
        for(int i=0; i<size; i++){

            if(playerList.get(i).equals("null")){
                currentPlayerID.add(index);
            }
            index++;
        }
			
        do{
            playOrder=turn.nextInt(4)+1;
            
        }while(!currentPlayerID.contains(playOrder));
		
        return "P"+playOrder;
		
    }
	
    // When a player quit, set the its field "null" in database
    public static void setNull(String clientID){
        try{
            Statement statement = connection.createStatement();
            String gameID=gameIn(clientID);
            PLAYER side = clientSide(clientID);
            String sql= "UPDATE GameLobbyInfo SET " + side.toString() + "='null' WHERE GameID='"+ gameID +"'";
            statement.executeUpdate(sql);
         }catch(SQLException ex){
            System.err.println("setNull() failed.");
            ex.printStackTrace();
         }
    }
	
	
    // Check whether there are 4 players in one game board
    public static boolean playerReady(String gameID){
	Vector<String> players = new Vector<String>();
        players=getPlayerList(gameID);
        //System.err.println("playerReady: got player list " + players.toString());
        // Add new entry for GameBoardInfo
        return !players.contains("null");         
    }

    public static void addGameMoveRecordEntry(String gameID){
    try{
            Statement statement = connection.createStatement();
            String sql="INSERT INTO GameBoardInfo (GameID, MoveRecord) VALUES('" + gameID + "', 'null')";
            statement.executeUpdate(sql);
        }catch(SQLException ex){
            System.err.println("Insert entry to GameBoardInfo failed.");
            ex.printStackTrace();
        }
    }
    
    // Store the player id into a set of a specific game board
    public static Vector<String> getPlayerList(String gameID){
        Vector<String> players = new Vector<String>();
        try{
            Statement statement = connection.createStatement();
            String sql="SELECT P1,P2,P3,P4 FROM GameLobbyInfo WHERE GameID='" +gameID+"'";
            ResultSet result=statement.executeQuery(sql);
            //System.out.println("getPlayerList: result is " + result.getString(1));
            
            while (result.next()){
                for(int i = 1; i < 5; i++){ 
                    if(result.getString(i) == null){
                        players.add("null");
                    }else{
                        players.add(result.getString(i));
                    }
                    //System.out.println("result is "+result.getString(i));
                }
            }
            //System.out.println("In getPlayerList() playerList is "+players+", GameID is "+gameID);
            return players;
            
        }catch(SQLException ex){
        
            System.err.println("getPlayerList() failed.");
            ex.printStackTrace();
        }
        
        players.add("getPlayerList() error returned.");
        return players;
    }
        
    // Return PlayerID according the gameID and PLAYER NO
    public static String getPlayerID(String gameID, PLAYER p){
        try{
            int playerNo = p.ordinal();  
            Vector<String> players=new Vector<String>();
            players=getPlayerList(gameID);
            return players.get(playerNo);
            
        }catch(Exception ex){

            System.err.println("getPlayerID failed.");
            ex.printStackTrace();
        }
        return "getPlayerID exception error return.";
    }
	
    //Get next GameID
    public static String nextGameID(){
        return Integer.toString(++gameCounter);
    }
    
    //Get current GameID
    public static String getCurGameID(){     
        return Integer.toString(gameCounter);
    }
    // Store gameInfo into JSON object    
    public static JSONArray gameJSON(){
        
        JSONArray games= new JSONArray();
        
        try{
            Statement statement = connection.createStatement();
            String sql="SELECT GameID FROM GameLobbyInfo";
            ResultSet result=statement.executeQuery(sql);


            //System.out.println("In gameJson() firstrow is "+result.getString(1));
            while(result.next()){

                JSONObject entry=new JSONObject();
                JSONArray players=new JSONArray();
                String curGameID=result.getString(1);

                entry.put("gameID",Integer.parseInt(curGameID));
                //System.out.println("In gameJson() gameId is "+curGameID);
                for(String s : getPlayerList(curGameID) ){
                        players.add(s);
                }
                //System.out.println("In gameJson() playerList is "+players);
                entry.put("playerList", players.toString());

                games.add(entry);

            }
            
            //System.out.println("In gameJson() gamesList is "+games);
            return games;
            
        }catch(Exception ex){
            System.err.println("gameJSON() failed");
            ex.printStackTrace();
            System.exit(1);
            return games;
        }  
    }
	
    // Get current player at which side
    public static PLAYER clientSide(String clientID){
	Vector<String> players = new Vector<String>();
        int index=1;
        int gamei=0;
        
        do{
            gamei++;
            players =getPlayerList(Integer.toString(gamei)); 
            
        }while(!players.contains(clientID) && gamei <= gameCounter);
		
        for(String s : players){
            if(s.equals(clientID)){
                return PLAYER.valueOf("P"+index);
            }
            index++;
        }
        
        return PLAYER.valueOf("INVALID_PLAYER");    
    }
	
    // According to playerid to get gameID
    public static String gameIn(String clientID){
        
        Vector<String> players=new Vector<String>();
        int gamei=1;
        
        while(gamei <= gameCounter && !players.contains(clientID)){
            
            players = getPlayerList(Integer.toString(gamei));
            gamei++;
            
        }
        if(players.contains(clientID)){
            return Integer.toString(gamei-1);
        }else{
            return "0";
        }
    }
    
    // Delete specific game in GameLobbyInfo table    
    public static void deleteGame(String gameID){
    	try{
            Statement statement = connection.createStatement();
            String sql="DELETE FROM GameLobbyInfo WHERE GameID='"+gameID+"'";
            statement.executeUpdate(sql);
            System.out.println("GAME " + gameID + " deleted from database");
            deleteMoveRecord(gameID);
        }catch(SQLException ex){
            System.err.println("deleteGame() failed.");
            ex.printStackTrace();
        }   
    }
    
    public static Boolean checkGameID(String gameID){
        try{
            Statement statement = connection.createStatement();
            String sql="SELECT GameID FROM GameLobbyInfo WHERE GameID='"+gameID+"'";
            ResultSet result=statement.executeQuery(sql);
            return result.first();
            
        }catch(SQLException ex){
            System.err.println("checkGameID() failed.");
            ex.printStackTrace();
        }   
        return false;
    
    }
    
    //******  Page3  ******//
    
    //Update the Move Record 
    public static void updateMoveRecord(String gameID, String move){
    	try{
            Statement statement = connection.createStatement();
            String sql="SELECT MoveRecord FROM GameBoardInfo WHERE GameID ='" + gameID + "'";
            ResultSet result=statement.executeQuery(sql);
            String moveList;
            result.first();
            //System.out.println("Here "+ result.first());
            //System.out.println("Here "+ result.getString(1));
            //System.out.println("Here "+ result.getString(1).equals("null"));
            if (result.getString(1).equals("null")){moveList=move+",";}
            else{moveList = result.getString(1)+move+",";}
            System.out.println("DB::updateMoveRecord() for GAME " + gameID + ": "+moveList);
            sql="UPDATE GameBoardInfo SET MoveRecord='" + moveList+"' WHERE GameID='"+ gameID +"'";
            statement.executeUpdate(sql);
        }catch(SQLException ex){
            System.err.println("updateMoveRecord() failed.");
            ex.printStackTrace();
        }
    }
    
        // Return next move with a specific gameID
    public static String getMoveList(String gameID){
    	try{
            Statement statement = connection.createStatement();
            String sql="SELECT MoveRecord FROM GameBoardInfo WHERE GameID ='" + gameID + "'";
            ResultSet result=statement.executeQuery(sql);
            result.first();
            String moveList=result.getString(1);
            return moveList;
    	}catch(SQLException ex){
            ex.printStackTrace();
            return "Error from getMoveList()";
    	}
        
    }
    
    public static void deleteMoveRecord(String gameID){
	try{
            Statement statement = connection.createStatement();
            String sql="DELETE FROM GameBoardInfo WHERE GameID='"+gameID+"'";
            statement.executeUpdate(sql);
            System.out.println("GAME Move Record" + gameID + " deleted from database");
        }catch(SQLException ex){
            System.err.println("deleteMoveRecord() failed.");
            ex.printStackTrace();
        }   

    }
    
    public static void dropTables(){
        //Drop existing tables
    	
    	List<String> tableNames = new ArrayList<String>();
    	tableNames.add("UserInfo");
    	tableNames.add("GameLobbyInfo");
    	tableNames.add("GameBoardInfo");
    	
    	String sql;
    	for (int i = 0; i < tableNames.size();i++) {
    		try{
    			sql= "DROP TABLE Chaturaji."+tableNames.get(i)+";";
    			Statement statement = connection.createStatement();
    			statement.executeUpdate(sql);
    			System.out.println(tableNames.get(i) + "table dropped.");
            
    		}catch(SQLException ex){
    			System.err.println("Attempt to drop table '" + tableNames.get(i) + "' failed.");
    			//ex.printStackTrace();
    		}
    	}
    }
    
    
    public static void DBclose(){
	try{
            if(connection!=null){
		connection.close();
		System.out.println("Database closed.");
	    }
	}catch(SQLException ex){
	   System.err.println("DBclose() falied");
	   ex.printStackTrace();	
	}
    }
}
