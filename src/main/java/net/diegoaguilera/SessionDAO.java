package net.diegoaguilera;

import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Created by diegoaguilerazambrano on 21/05/14.
 */
public class SessionDAO {
    public SessionDAO(DB blogDatabase){
        DBCollection sessionsCollection = blogDatabase.getCollection("sessions");
    }
}
