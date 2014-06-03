<!DOCTYPE html>
<html>
    <head>
        <title>Log in for Admin Site</title>
    </head>
    <body>
        <h3>Log in for Access:</h1>
        <form method="post">
            User: <input type="text" name="username"></input>
            Password: <input type="password" name="password"></input>
            <input type="submit" name="submit"></input>
            ${login_error!""}
        </form>
        <br>
        <p>New user? you can <a href="/signup">Sign Up</a> for a new account</p>
        <#include "footer.ftl">
    </body>
</html>