<!DOCTYPE html>
<html>
    <head>
        <title>Sign up for an Account</title>
    </head>
    <body>
        <h2>Sign up</h2>
        <form method="post">
            Username: <input name="username" type="text"></input>
            Email: <input name="email" type="text"></input>
            Password: <input name="password" type="password"></input>
            <input type="submit"></input>
            ${signup_error}
        </form>
    </body>
</html>