<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>E-Commerce</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            color: #222;
        }

        header {
            background: #222;
            color: white;
            padding: 18px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        header h1 {
            font-size: 24px;
        }

        nav button {
            background: transparent;
            border: none;
            color: white;
            margin-left: 20px;
            cursor: pointer;
            font-size: 15px;
        }

        .hero {
            background: white;
            padding: 50px 40px;
            text-align: center;
        }

        .hero h2 {
            font-size: 36px;
            margin-bottom: 12px;
        }

        .hero p {
            color: #666;
            margin-bottom: 25px;
        }

        .container {
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .section-title {
            margin-bottom: 20px;
        }

        .users {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 20px;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .card h3 {
            margin-bottom: 8px;
        }

        .card p {
            color: #666;
            margin: 6px 0;
        }

        button.primary {
            background: #222;
            color: white;
            border: none;
            padding: 11px 18px;
            border-radius: 5px;
            cursor: pointer;
        }

        button.primary:hover {
            background: #444;
        }

        .form-container {
            background: white;
            padding: 25px;
            margin-top: 35px;
            border-radius: 8px;
        }

        input {
            width: 100%;
            padding: 11px;
            margin: 8px 0 15px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        #message {
            margin-top: 15px;
            font-weight: bold;
        }

        footer {
            margin-top: 50px;
            padding: 20px;
            text-align: center;
            background: #222;
            color: white;
        }
    </style>
</head>

<body>

<header>
    <h1>My E-Commerce</h1>

    <nav>
        <button onclick="loadUsers()">Users</button>
        <button onclick="showAddUser()">Add User</button>
    </nav>
</header>

<section class="hero">
    <h2>Welcome to My E-Commerce</h2>
    <p>A simple full-stack application using Spring MVC, MySQL and JavaScript.</p>

    <button class="primary" onclick="loadUsers()">
        View Users
    </button>
</section>

<div class="container">

    <div id="userSection">
        <h2 class="section-title">Users</h2>

        <div id="users" class="users">
            <p>Click "View Users" to load users.</p>
        </div>
    </div>

    <div id="formSection" class="form-container" style="display:none;">

        <h2>Add User</h2>

        <form id="userForm">

            <label>Username</label>
            <input
                type="text"
                id="username"
                required
            >

            <label>Email</label>
            <input
                type="email"
                id="email"
                required
            >

            <label>Password</label>
            <input
                type="password"
                id="password"
                required
            >

            <button type="submit" class="primary">
                Create User
            </button>

        </form>

        <div id="message"></div>

    </div>

</div>

<footer>
    <p>My E-Commerce Application</p>
</footer>

<script>

const API_URL = "/ecommerce/api/users";


function loadUsers() {

    fetch(API_URL, {
        headers: {
            "Accept": "application/json"
        }
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Failed to load users");
        }

        return response.json();
    })
    .then(users => {

        const container = document.getElementById("users");

        container.innerHTML = "";

        users.forEach(user => {

            const card = document.createElement("div");

            card.className = "card";

            card.innerHTML = `
                <h3>${user.username}</h3>
                <p><strong>ID:</strong> ${user.id}</p>
                <p><strong>Email:</strong> ${user.email}</p>
            `;

            container.appendChild(card);
        });
    })
    .catch(error => {

        document.getElementById("users").innerHTML =
            "<p>Unable to load users.</p>";

        console.error(error);
    });
}


function showAddUser() {

    document.getElementById("formSection").style.display = "block";

    document
        .getElementById("formSection")
        .scrollIntoView({
            behavior: "smooth"
        });
}


document
    .getElementById("userForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        const user = {

            username:
                document.getElementById("username").value,

            email:
                document.getElementById("email").value,

            password:
                document.getElementById("password").value
        };


        fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(user)
        })

        .then(response => response.text())

        .then(message => {

            document.getElementById("message").textContent =
                message;

            document.getElementById("userForm").reset();

            loadUsers();
        })

        .catch(error => {

            document.getElementById("message").textContent =
                "Failed to create user.";

            console.error(error);
        });

    });

</script>

</body>
</html>