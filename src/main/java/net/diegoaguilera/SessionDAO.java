package net.diegoaguilera;

import com.mongodb.*;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Created by diegoaguilerazambrano on 21/05/14.
 */
public class SessionDAO {

    private final DBCollection sessionsCollection;

    public SessionDAO(DB blogDatabase){
        sessionsCollection = blogDatabase.getCollection("sessions");
    }

    public String startSession(String username) {
        SecureRandom generator = new SecureRandom();
        byte randomBytes[] = new byte[32];
        System.out.println("randomBytes[] before generator: "+randomBytes);
        generator.nextBytes(randomBytes);
        System.out.println("ramdomBytes[] after generator: "+randomBytes);
        BASE64Encoder encoder = new BASE64Encoder();
        String sessionID = encoder.encode(randomBytes);
        System.out.println("sessionID to use: "+sessionID);
        //what if the session already exists in the database? we should modify instead of insert
        // so take the new cookie and modify the session id
        BasicDBObject session = new BasicDBObject("username",username);
        session.append("_id",sessionID);
        try {
            sessionsCollection.insert(session);
        } catch (Exception e) {
            System.out.println("Error: Couldn't insert session for user: "+username);
            e.printStackTrace();
            return null;
        }
        return session.getString("_id");
    }

    public DBObject getSession(String sessionID) {
        try {
            return sessionsCollection.findOne(new BasicDBObject("_id",sessionID));
        }catch (MongoException e){
            System.out.println("Couldn't access usersCollection");
            e.printStackTrace();
            return null;
        }
    }

    public String findUsernameBySessionID(String sessionCookie) {
        DBObject user = getSession(sessionCookie);
        if (user == null){
            return null;
        }else{
            return user.get("username").toString();
        }
    }

    public boolean endSession(String sessionID) {
        try {
            sessionsCollection.remove(new BasicDBObject("_id",sessionID));
            return true;
        }catch (MongoException e){
            e.printStackTrace();
        }
            return false;
    }
}
