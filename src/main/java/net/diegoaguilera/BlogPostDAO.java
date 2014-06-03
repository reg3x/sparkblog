package net.diegoaguilera;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diegoaguilerazambrano on 21/05/14.
 */
public class BlogPostDAO {
    private DBCollection postsCollection;

    public BlogPostDAO(DB blogDatabase){
        postsCollection =  blogDatabase.getCollection("posts");
    }

    public List<DBObject> findByTagDateDescending(){
        //perform a search in the collection posts and return a list of DBObjects which would be the cursors
        List<DBObject> posts = null;
        return posts;
    }

    public boolean addPost(String author, String title, String body, ArrayList<String> tags) {
        try {
            postsCollection.insert(new BasicDBObject("author",author).append("body",body).append("tags", tags));
            return true;
        }catch (MongoException e){
            System.out.println("Couldn't access usersCollection");
            e.printStackTrace();
            return false;
        }
    }
}
