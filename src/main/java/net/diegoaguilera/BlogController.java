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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
                    System.out.println("User "+username+" does not exist");
                    map.put("username", username);
                    map.put("password", password);
                    map.put("login_error", "Invalid User");
                    template.process(map, writer);
                }else response.redirect("/welcome");
            }
        });
        get(new FreemarkedRoute("/welcome","welcome.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                String access = new String("validUser");
                map.put("access",access);
                template.process(map, writer);

                //create a new post

                //delete a post

                //edit a post
            }
        });
        get(new FreemarkedRoute("/signup","signup.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                map.put("username","");
                map.put("email","");
                map.put("password","");
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
                System.out.println("An attempt to create an user with:");
                System.out.println("Username: "+username);
                System.out.println("Email: "+email);
                System.out.println("Password: "+password);

                Hashtable<String, String> map = new Hashtable<String, String>();

                //validate inputs
                String validationError = validateSignup(username, email, password);
                System.out.println("value: "+validationError);
                if (validationError.equals("none")){
                    //validate User creation
                    String addUserError = userDAO.addUser(username,email,password);
                    if (addUserError.equals("none")) {
                        System.out.println("User: "+username+" created successfully");
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
        });
        get(new FreemarkedRoute("newpost","newpost.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                template.process(map, writer);
            }
        });
        post(new FreemarkedRoute("newpost","newpost.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash map = new SimpleHash();
                System.out.println("Title: "+request.queryParams("title"));
                System.out.println("Body: "+request.queryParams("body"));
                System.out.println("tags: "+request.queryParams("tags"));
                //validate tags
                template.process(map, writer);
            }
        });
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
