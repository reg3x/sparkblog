package net.diegoaguilera;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

/**
 * Hello world!
 */
public class BlogController {
    private Configuration configuration;
    private BlogPostDAO blogPostDAO;
    private SessionDAO sessionDAO;
    private UserDAO userDAO;

    public BlogController() throws IOException {
        final MongoClient mongoClient = new MongoClient("localhost",27017);
        final DB blogDatabase = mongoClient.getDB("blogdiego");

        blogPostDAO = new BlogPostDAO(blogDatabase);
        sessionDAO = new SessionDAO(blogDatabase);
        userDAO = new UserDAO(blogDatabase);

        configuration = configureFreemarker();
        Spark.setPort(8080);
        initializeRoutes();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting blog");
        new BlogController();
    }

    abstract class FreemarkedRoute extends Route{
        final Template template;

        protected FreemarkedRoute(final String path, final String templateName) throws IOException {
            super(path);
            template = configuration.getTemplate(templateName);
        }

        @Override
        public Object handle(Request request, Response response) {
            StringWriter writer = new StringWriter();
            try {
                doHandle(request, response, writer);
            }catch (Exception e){
                e.printStackTrace();
            }
            return writer;
        }

        protected abstract void doHandle(final Request request, final Response response, final Writer writer) throws IOException, TemplateException;
    }

    private void initializeRoutes() throws IOException {
        get(new FreemarkedRoute("/", "blog.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String name = new String("Diego");

                // check user session

                //return last posts

                List<DBObject> posts = blogPostDAO.findByTagDateDescending();


                SimpleHash map = new SimpleHash();
                map.put("myposts", posts);
                template.process(map, writer);
            }
        });

        get(new FreemarkedRoute("/login", "login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                map.put("username", "");
                map.put("password", "");
                map.put("login_error", "");
                template.process(map,writer);
            }
        });
        post(new FreemarkedRoute("/login","login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                String username = request.queryParams("username");
                String password = request.queryParams("password");
                System.out.println("user: "+username+" is requesting access with password: "+password);
                //validate login with UserDAO
                DBObject user = userDAO.validateUser(username,password);
                if (user == null){
                    System.out.println("Invalid login for username: "+username+" ");
                    map.put("username", username);
                    map.put("password", password);
                    map.put("login_error", "Invalid User/Password");
                    template.process(map, writer);
                }else {
                    //valid user checked at DB, start session and then create cookie
                    String sessionID = sessionDAO.startSession(user.get("_id").toString());
                    if (sessionID!=null){
                        //session created now create the cookie
                        response.raw().addCookie(new Cookie("session", sessionID));
                        response.redirect("/welcome");

                    }else{
                        // error creating session
                        //issue #6 - need to handle sessions error at /internal_error
                        response.redirect("/internal_error");
                    }
                }
            }
        });
        get(new FreemarkedRoute("/welcome","welcome.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                String sessionCookie = getSessionCookie(request);
                String user = sessionDAO.findUsernameBySessionID(sessionCookie);

                if (user == null){
                    System.out.println("user no yet logged trying to access");
                    response.redirect("/login");
                }else {
                    map.put("username", user);
                    template.process(map, writer);
                }
            }
        });
        get(new FreemarkedRoute("/signup","signup.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                map.put("username","");
                map.put("email","");
                map.put("password","");
                map.put("repassword", "");
                map.put("signup_error", "");
                template.process(map,writer);
            }
        });
        post(new FreemarkedRoute("/signup","signup.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String username = request.queryParams("username");
                String email = request.queryParams("email");
                String password = request.queryParams("password");
                String repassword = request.queryParams("repassword");
                System.out.println("An attempt to create an user with:");
                System.out.println("Username: "+username);
                System.out.println("Email: "+email);
                System.out.println("Password: "+password);
                System.out.println("RePassword: "+repassword);

                Hashtable<String, String> map = new Hashtable<String, String>();

                //validate inputs
                if (!password.equals(repassword)){
                    System.out.println("password and repassword are not the same");
                    map.put("username", username);
                    map.put("email",email);
                    map.put("password","");
                    map.put("repassword","");
                    map.put("signup_error","passwords are not the same!");
                    template.process(map, writer);
                }else{
                    String validationError = validateSignup(username, email, password);
                    System.out.println("value: "+validationError);
                    if (validationError.equals("none")){
                        //validate User creation
                        String addUserError = userDAO.addUser(username,email,password);
                        if (addUserError.equals("none")) {
                            System.out.println("User: "+username+" created successfully");
                            //user created successfully
                            //the user needs confirmation of successfully created account, check ftl
                            //signup ftl for variable success 
                            response.redirect("/login");
                        } else if (addUserError.equals("Duplicate key Error")) {
                            System.out.println("Couldn't create User: "+username+" Due to Duplicate keys");
                            map.put("signup_error",addUserError);
                            template.process(map, writer);
                        } else if (addUserError.equals("MongoDB Server is not reachable")){
                            System.out.println("Couldn't create User: "+username+" Server is Unreachable");
                            map.put("signup_error",addUserError);
                            template.process(map, writer);
                        }
                    } else {
                        System.out.println("Invalid parameters for an user");
                        map.put("signup_error",validationError);
                        template.process(map, writer);
                    }
                }

            }
        });
        get(new FreemarkedRoute("/newpost","newpost.ftl") {
            @Override
            //need to validate if the user is not logged check for cookie and then search in DB
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                template.process(map, writer);
            }
        });
        post(new FreemarkedRoute("/newpost","newpost.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                String title = new String(request.queryParams("title"));
                String body = new String(request.queryParams("body"));
                String tags = new String(request.queryParams("tags"));
                System.out.println("Title: "+ title);
                System.out.println("Body: "+ body);
                System.out.println("tags: "+ tags);
                String user = sessionDAO.findUsernameBySessionID(getSessionCookie(request));

                ArrayList<String> cleanedTags = new ArrayList<String>();
                cleanedTags = extractTags(tags);
                if ( user != null) {
                    if (blogPostDAO.addPost(user, title, body, cleanedTags)){
                        //post added
                        System.out.println("newpost posted!");
                        response.redirect("/welcome");

                    }else {
                        //couldn't add the post
                        System.out.println("there has been an error at DB");
                        response.redirect("/internal_error");
                    }
                }else {
                    //user does not exist please login
                    System.out.println("user does not exist");
                    response.redirect("/login");
                }
                //validate tags
            }
        });
        get(new FreemarkedRoute("/logout","login.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String sessionID = getSessionCookie(request);

                if (sessionID == null){
                    //nothing to do if the user has no cookie
                    System.out.println("cookie not found, nothing to delete");
                }else {
                    if (sessionDAO.endSession(sessionID)){
                        //session deleted from database
                        System.out.println("session deleted from DB");
                        Cookie cookie  = getSessionCookieActual(request);
                        cookie.setMaxAge(0);
                        //delete cookie
                        response.raw().addCookie(cookie);
                    } else {
                        //couldn't delete the cookie
                        System.out.println("Error: Couldn't erase session");
                    }
                    response.redirect("/login");
                }
            }
        });
    }

    private  ArrayList<String> extractTags(String tags) {
        System.out.println("tags are: "+tags);
        //require to improve and also to eliminate special characters to only allow -&,\s
        tags = tags.replaceAll("\\s|-|&", ",").replaceAll(",+", ",");
        System.out.println("tags after replaceAll: "+ tags);
        String tagsArray[] = tags.split(",");

        ArrayList<String> cleanedTags = new ArrayList<String>();
        for (String arrayElement:tagsArray){
            if (arrayElement!="" && !cleanedTags.contains(arrayElement)){
                cleanedTags.add(arrayElement);
            }
        }
        return cleanedTags;
    }

    private String getSessionCookie(final Request request) {
        if (request.raw().getCookies() == null){
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session"))
            return cookie.getValue();
        }
        return null;
    }
    private Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null){
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session"))
                return cookie;
        }
        return null;
    }

    private String validateSignup(String username, String email, String password) {
        String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
        String PASS_RE = "^.{3,20}$";
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

        if (!username.matches(USER_RE)){
            return "Invalid User name";
        }
        else if (!email.matches(EMAIL_RE)){
            return "Invalid Email";
        }
        else if (!password.matches(PASS_RE)){
            return "Invalid Password" ;
        } else return "none";
    }

    private Configuration configureFreemarker(){
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(BlogController.class, "/freemarker");
        staticFileLocation("/staticFiles");
        return configuration;
    }
}
