<!DOCTYPE html>
<html>

    <head>
        <title>reg3x blog</title>
        <link rel="stylesheet" type="text/css" href="/c ss/blog.css">
    </head>

    <body>
        <h2>Welcome to Reg3x's Blog</h1>
        <a href="/login">Login</a>
        <p>Free technology everywhere for the top languages and techs </p>
        <br>
        <p>This has been Created by: <a href="http://diegoaguilera.net">reg3x</a></p>
        <br>
        <p>we should see a list of posts here:</p>
        <#if myposts??>
            <#list myposts as post>
                <li> ${post.title!"empty"} </li>
            </#list>
        </#if>
        <#include "footer.ftl">
    </body>

</html>