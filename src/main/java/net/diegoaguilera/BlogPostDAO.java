package net.diegoaguilera;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

/**
 * Created by diegoaguilerazambrano on 21/05/14.
 */
public class BlogPostDAO {
    public BlogPostDAO(DB blogDatabase){
        DBCollection postsCollection =  blogDatabase.getCollection("posts");
    }

    public List<DBObject> findByTagDateDescending(){
        //perform a search in the collection posts and return a list of DBObjects which would be the cursors
        List<DBObject> posts = null;
        return posts;
    }
}
