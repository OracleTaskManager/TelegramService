## 🤖 Telegram Integration Service - Oracle Task Manager

Este repositorio contiene el microservicio que integra el sistema Oracle Task Manager con Telegram. Permite recibir notificaciones, comandos y actualizaciones desde la plataforma de mensajería.

### Requisitos

* Java JDK 23
* Maven
* Git

### Configuración del entorno

#### 1. Clonar el repositorio

```bash
git clone https://github.com/OracleTaskManager/TelegramService.git
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
```

#### 3. Ejecutar

```bash
mvn clean package
java -jar target/TelegramService-0.0.1-SNAPSHOT.jar
# O directamente:
mvn spring-boot:run
```

#### 4. Verificación

* API: [http://localhost:8082](http://localhost:8082)
