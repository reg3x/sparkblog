<!DOCTYPE html>
<html>
    <head>
        <title>Sign up for an Account</title>
    </head>
    <body>
        <h2>Sign up</h2>
        <form  method="post">
            Username: <input name="username" type="text"></input>
            Email: <input name="email" type="text"></input>
            Password: <input name="password" type="password"></input>
            ReEnter Password: <input name="repassword" type="password"></input>
            <input type="submit"></input>
            ${signup_error!""}
            <br>
            <p>Consider the Following:</p>
            <ul>
                <li>Username should be at least 2 characters length</li>
                <li>Password should be numbers and letters 3 charaters minimum</li>
            </ul>
        </form>
    </body>
    ${success!""}
</html>
