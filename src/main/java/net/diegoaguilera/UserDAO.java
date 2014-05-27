package net.diegoaguilera;

import com.mongodb.*;

import java.io.IOException;

/**
 * Created by diegoaguilerazambrano on 21/05/14.
 */
public class UserDAO {
    private DBCollection usersCollection;

    public UserDAO(DB blogDatabase){
        usersCollection =  blogDatabase.getCollection("users");
    }

    public DBObject validateUser(String username, String password)throws MongoException{
        DBObject user = usersCollection.findOne(new BasicDBObject("_id",username));
        if (user == null){
            System.out.println("user not in database");
            return null;
        }else if (user.get("password").equals(password)){
            System.out.println("correct user and password");
            return user;
        }else {
            System.out.println("Incorrect password for username: " + username);
            return null;
        }
    }

    public String addUser(String username, String email, String password) {
        try {
            usersCollection.insert(new BasicDBObject("_id",username).append("email",email).append("password",password));
            return "none";
        }catch (MongoException.DuplicateKey duplicateKey){
            System.out.println("Duplicate Key Error at inserting username: "+username);
            return "Duplicate key Error";
        }catch (MongoServerSelectionException e){
            System.out.println("Server seems unreachable");
            return "MongoDB Server is not reachable";
        }
    }
}
