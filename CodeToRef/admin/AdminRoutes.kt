package backend.admin

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.request.*
import backend.repositories.UserRepository

fun Route.adminRoutes(userRepository: UserRepository) {
    route("/admin") {
        // Главная страница админки
        get {
            val users = userRepository.getAllUsers()
            val html = buildString {
                append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Администрирование пользователей</title>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
                        tr:nth-child(even) { background-color: #f9f9f9; }
                        .btn { padding: 5px 10px; margin: 2px; text-decoration: none; display: inline-block; color: white; border-radius: 3px; }
                        .btn-danger { background-color: #f44336; }
                        h1 { color: #333; }
                        .form-container { margin: 20px 0; padding: 15px; border: 1px solid #ddd; background-color: #f9f9f9; }
                        .form-group { margin-bottom: 10px; }
                        label { display: block; margin-bottom: 5px; }
                        input, select { width: 100%; padding: 8px; box-sizing: border-box; }
                        button { padding: 10px 15px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }
                    </style>
                </head>
                <body>
                    <h1>Управление пользователями</h1>
                    
                    <div class="form-container">
                        <h2>Добавить нового пользователя</h2>
                        <form id="registerForm">
                            <div class="form-group">
                                <label for="lastName">Фамилия:</label>
                                <input type="text" id="lastName" name="lastName" required>
                            </div>
                            <div class="form-group">
                                <label for="firstName">Имя:</label>
                                <input type="text" id="firstName" name="firstName" required>
                            </div>
                            <div class="form-group">
                                <label for="middleName">Отчество:</label>
                                <input type="text" id="middleName" name="middleName">
                            </div>
                            <div class="form-group">
                                <label for="email">Email:</label>
                                <input type="email" id="email" name="email" required>
                            </div>
                            <div class="form-group">
                                <label for="college">Колледж:</label>
                                <input type="text" id="college" name="college" required>
                            </div>
                            <div class="form-group">
                                <label for="group">Группа:</label>
                                <input type="text" id="group" name="group" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Пароль:</label>
                                <input type="password" id="password" name="password" required>
                            </div>
                            <div class="form-group">
                                <label for="role">Роль:</label>
                                <select id="role" name="role">
                                    <option value="student">Студент</option>
                                    <option value="leader">Староста</option>
                                    <option value="admin">Администратор</option>
                                </select>
                            </div>
                            <button type="submit">Добавить пользователя</button>
                        </form>
                    </div>
                    
                    <h2>Список пользователей</h2>
                    <table>
                        <tr>
                            <th>ID</th>
                            <th>Фамилия</th>
                            <th>Имя</th>
                            <th>Отчество</th>
                            <th>Email</th>
                            <th>Колледж</th>
                            <th>Группа</th>
                            <th>Роль</th>
                            <th>Дата создания</th>
                            <th>Действия</th>
                        </tr>
                """)

                users.forEach { user ->
                    append("""
                        <tr>
                            <td>${user.userId}</td>
                            <td>${user.lastName}</td>
                            <td>${user.firstName}</td>
                            <td>${user.middleName ?: ""}</td>
                            <td>${user.email}</td>
                            <td>${user.college}</td>
                            <td>${user.group}</td>
                            <td>${user.role}</td>
                            <td>${user.createdAt}</td>
                            <td>
                                <button class="btn btn-danger" onclick="deleteUser(${user.userId})">Удалить</button>
                            </td>
                        </tr>
                    """)
                }

                append("""
                    </table>
                    
                    <script>
                        // Функция удаления пользователя
                        function deleteUser(userId) {
                            if (confirm('Вы уверены, что хотите удалить пользователя с ID ' + userId + '?')) {
                                fetch('/users/' + userId, {
                                    method: 'DELETE'
                                })
                                .then(response => response.json())
                                .then(data => {
                                    alert(data.message);
                                    location.reload();
                                })
                                .catch(error => {
                                    console.error('Error:', error);
                                    alert('Ошибка при удалении пользователя');
                                });
                            }
                        }
                        
                        // Обработка формы регистрации
                        document.getElementById('registerForm').addEventListener('submit', function(e) {
                            e.preventDefault();
                            
                            const formData = {
                                firstName: document.getElementById('firstName').value,
                                lastName: document.getElementById('lastName').value,
                                middleName: document.getElementById('middleName').value || null,
                                email: document.getElementById('email').value,
                                college: document.getElementById('college').value,
                                group: document.getElementById('group').value,
                                password: document.getElementById('password').value,
                                role: document.getElementById('role').value
                            };
                            
                            fetch('/register', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify(formData)
                            })
                            .then(response => response.json())
                            .then(data => {
                                alert(data.message);
                                if (data.userId) {
                                    location.reload();
                                }
                            })
                            .catch(error => {
                                console.error('Error:', error);
                                alert('Ошибка при регистрации пользователя');
                            });
                        });
                    </script>
                </body>
                </html>
                """)
            }
            call.respondText(html, ContentType.Text.Html)
        }
    }
} 