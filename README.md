## 🤖 Telegram Integration Service - Oracle Task Manager

Este repositorio contiene el microservicio que integra el sistema Oracle Task Manager con Telegram. Permite recibir notificaciones, comandos y actualizaciones desde la plataforma de mensajería.

### Requisitos

* Java JDK 23
* Maven
* Git

### Configuración del entorno

#### 1. Clonar el repositorio

```bash
git clone https://github.com/tuOrganizacion/TelegramService.git
cd TelegramService
```

#### 2. Configurar `application.properties`

```properties
server.port=8082
telegram.bot.secret=${TELEGRAM_BOT_SECRET}
jwt.secret.oracle=${JWT_SECRET_ORACLE}


Variables de entorno necesarias:

* `TELEGRAM_BOT_SECRET`
* `JWT_SECRET_ORACLE`

#### 3. Ejecutar

```bash
mvn clean package
java -jar target/TelegramService-0.0.1-SNAPSHOT.jar
# O directamente:
mvn spring-boot:run
```

#### 4. Verificación

* API: [http://localhost:8082](http://localhost:8082)
* Swagger UI: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

---

### Producción

| Aspecto              | Producción                                                                                                     |
| -------------------- | -------------------------------------------------------------------------------------------------------------- |
| Swagger              | [http://140.84.189.81/swagger-telegram/swagger-ui.html](http://140.84.189.81/swagger-telegram/swagger-ui.html) |
| Prefijo de endpoints | `/api/telegram/...`                                                                                            |
| Despliegue           | GitHub Actions (`build-push-telegram.yml`) automatiza todo                                                     |

---

¿Quieres que también te genere los archivos `README.md` como archivos descargables o prefieres copiar y pegar directamente?
