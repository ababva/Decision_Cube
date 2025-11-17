package com.decisioncube.backend

import com.decisioncube.backend.database.DatabaseFactory
import com.decisioncube.backend.routes.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(CORS) {
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        anyHost()
    }

    routing {
        get("/") {
            call.respondText(
                contentType = io.ktor.http.ContentType.Text.Html,
                status = io.ktor.http.HttpStatusCode.OK
            ) {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Decision Cube API</title>
                    <style>
                        body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
                        h1 { color: #333; }
                        .endpoint { margin: 15px 0; padding: 10px; background: #f5f5f5; border-radius: 5px; }
                        .method { display: inline-block; padding: 3px 8px; border-radius: 3px; font-weight: bold; margin-right: 10px; }
                        .get { background: #61affe; color: white; }
                        .post { background: #49cc90; color: white; }
                        code { background: #e8e8e8; padding: 2px 5px; border-radius: 3px; }
                        .links { margin: 20px 0; }
                        .links a { display: inline-block; margin: 10px 15px 10px 0; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }
                        .links a:hover { background: #45a049; }
                    </style>
                </head>
                <body>
                    <h1>Decision Cube API</h1>
                    <p>Backend API для фитнес-приложения Decision Cube</p>
                    
                    <div class="links">
                        <a href="/login">Войти</a>
                        <a href="/register">Зарегистрироваться</a>
                    </div>
                    
                    <h2>Доступные эндпоинты:</h2>
                    
                    <div class="endpoint">
                        <span class="method post">POST</span>
                        <code>/api/auth/register</code> - Регистрация пользователя
                    </div>
                    
                    <div class="endpoint">
                        <span class="method post">POST</span>
                        <code>/api/auth/login</code> - Вход пользователя
                    </div>
                    
                    <div class="endpoint">
                        <span class="method get">GET</span>
                        <code>/api/users/{id}</code> - Получение пользователя по ID
                    </div>
                    
                    <div class="endpoint">
                        <span class="method get">GET</span>
                        <code>/api/users/search?query=...</code> - Поиск пользователей
                    </div>
                    
                    <div class="endpoint">
                        <span class="method get">GET</span>
                        <code>/api/users/leaderboard</code> - Рейтинг пользователей
                    </div>
                    
                    <div class="endpoint">
                        <span class="method post">POST</span>
                        <code>/api/exercises</code> - Создание упражнения
                    </div>
                    
                    <div class="endpoint">
                        <span class="method get">GET</span>
                        <code>/api/statistics/daily?days=7</code> - Ежедневная статистика
                    </div>
                    
                    <hr>
                    <p><small>API работает на порту 8080</small></p>
                </body>
                </html>
                """.trimIndent()
            }
        }
        
        get("/login") {
            call.respondText(
                contentType = io.ktor.http.ContentType.Text.Html,
                status = io.ktor.http.HttpStatusCode.OK
            ) {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Вход - Decision Cube</title>
                    <style>
                        body { font-family: Arial, sans-serif; max-width: 400px; margin: 100px auto; padding: 20px; background: #f5f5f5; }
                        .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        h1 { color: #333; margin-top: 0; text-align: center; }
                        form { margin-top: 20px; }
                        .form-group { margin-bottom: 20px; }
                        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
                        input[type="text"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; font-size: 14px; }
                        button { width: 100%; padding: 12px; background: #4CAF50; color: white; border: none; border-radius: 5px; font-size: 16px; cursor: pointer; }
                        button:hover { background: #45a049; }
                        .error { background: #f44336; color: white; padding: 10px; border-radius: 5px; margin-bottom: 20px; display: none; }
                        .success { background: #4CAF50; color: white; padding: 10px; border-radius: 5px; margin-bottom: 20px; display: none; }
                        .links { text-align: center; margin-top: 20px; }
                        .links a { color: #4CAF50; text-decoration: none; }
                        .links a:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Вход</h1>
                        <div id="error" class="error"></div>
                        <div id="success" class="success"></div>
                        <form id="loginForm">
                            <div class="form-group">
                                <label for="username">Имя пользователя:</label>
                                <input type="text" id="username" name="username" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Пароль:</label>
                                <input type="password" id="password" name="password" required>
                            </div>
                            <button type="submit">Войти</button>
                        </form>
                        <div class="links">
                            <p>Нет аккаунта? <a href="/register">Зарегистрироваться</a></p>
                            <p><a href="/">На главную</a></p>
                        </div>
                    </div>
                    <script>
                        document.getElementById('loginForm').addEventListener('submit', async function(e) {
                            e.preventDefault();
                            const username = document.getElementById('username').value;
                            const password = document.getElementById('password').value;
                            const errorDiv = document.getElementById('error');
                            const successDiv = document.getElementById('success');
                            
                            errorDiv.style.display = 'none';
                            successDiv.style.display = 'none';
                            
                            try {
                                const response = await fetch('/api/auth/login', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ username, password })
                                });
                                
                                const data = await response.json();
                                
                                if (response.ok) {
                                    successDiv.textContent = 'Успешный вход! Токен: ' + data.token;
                                    successDiv.style.display = 'block';
                                    localStorage.setItem('token', data.token);
                                    localStorage.setItem('user', JSON.stringify(data.user));
                                } else {
                                    errorDiv.textContent = data.error || 'Ошибка входа';
                                    errorDiv.style.display = 'block';
                                }
                            } catch (error) {
                                errorDiv.textContent = 'Ошибка соединения: ' + error.message;
                                errorDiv.style.display = 'block';
                            }
                        });
                    </script>
                </body>
                </html>
                """.trimIndent()
            }
        }
        
        get("/register") {
            call.respondText(
                contentType = io.ktor.http.ContentType.Text.Html,
                status = io.ktor.http.HttpStatusCode.OK
            ) {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Регистрация - Decision Cube</title>
                    <style>
                        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; background: #f5f5f5; }
                        .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        h1 { color: #333; margin-top: 0; text-align: center; }
                        form { margin-top: 20px; }
                        .form-group { margin-bottom: 20px; }
                        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
                        input[type="text"], input[type="email"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; font-size: 14px; }
                        button { width: 100%; padding: 12px; background: #4CAF50; color: white; border: none; border-radius: 5px; font-size: 16px; cursor: pointer; }
                        button:hover { background: #45a049; }
                        .error { background: #f44336; color: white; padding: 10px; border-radius: 5px; margin-bottom: 20px; display: none; }
                        .success { background: #4CAF50; color: white; padding: 10px; border-radius: 5px; margin-bottom: 20px; display: none; }
                        .links { text-align: center; margin-top: 20px; }
                        .links a { color: #4CAF50; text-decoration: none; }
                        .links a:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Регистрация</h1>
                        <div id="error" class="error"></div>
                        <div id="success" class="success"></div>
                        <form id="registerForm">
                            <div class="form-group">
                                <label for="username">Имя пользователя:</label>
                                <input type="text" id="username" name="username" required>
                            </div>
                            <div class="form-group">
                                <label for="email">Email:</label>
                                <input type="email" id="email" name="email" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Пароль:</label>
                                <input type="password" id="password" name="password" required>
                            </div>
                            <button type="submit">Зарегистрироваться</button>
                        </form>
                        <div class="links">
                            <p>Уже есть аккаунт? <a href="/login">Войти</a></p>
                            <p><a href="/">На главную</a></p>
                        </div>
                    </div>
                    <script>
                        document.getElementById('registerForm').addEventListener('submit', async function(e) {
                            e.preventDefault();
                            const username = document.getElementById('username').value;
                            const email = document.getElementById('email').value;
                            const password = document.getElementById('password').value;
                            const errorDiv = document.getElementById('error');
                            const successDiv = document.getElementById('success');
                            
                            errorDiv.style.display = 'none';
                            successDiv.style.display = 'none';
                            
                            try {
                                const response = await fetch('/api/auth/register', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ username, email, password })
                                });
                                
                                const data = await response.json();
                                
                                if (response.ok) {
                                    successDiv.textContent = 'Регистрация успешна! Токен: ' + data.token;
                                    successDiv.style.display = 'block';
                                    localStorage.setItem('token', data.token);
                                    localStorage.setItem('user', JSON.stringify(data.user));
                                    setTimeout(() => window.location.href = '/login', 2000);
                                } else {
                                    errorDiv.textContent = data.error || 'Ошибка регистрации';
                                    errorDiv.style.display = 'block';
                                }
                            } catch (error) {
                                errorDiv.textContent = 'Ошибка соединения: ' + error.message;
                                errorDiv.style.display = 'block';
                            }
                        });
                    </script>
                </body>
                </html>
                """.trimIndent()
            }
        }
    }

    configureAuthRoutes()
    configureUserRoutes()
    configureExerciseRoutes()
    configureStatisticsRoutes()
}

