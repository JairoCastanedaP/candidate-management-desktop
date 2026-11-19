<div align="center">

# Candidate Management Desktop

Aplicación de escritorio para gestionar postulantes: registro, actualización, eliminación, listado y búsqueda, con validación de datos y ubicación geográfica por ubigeo.

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Swing-5382A1)
![JDBC](https://img.shields.io/badge/JDBC-ojdbc-F80000)
![Oracle](https://img.shields.io/badge/Oracle-Database-F80000?logo=oracle&logoColor=white)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)

</div>

## Funcionalidades

- Registro, actualización, eliminación y listado de postulantes.
- Búsqueda por nombres, apellido paterno o apellido materno.
- Operaciones de registro, actualización y búsqueda mediante procedimientos almacenados.
- Validación de nombres, DNI, correo electrónico, teléfonos y sexo, con aviso de DNI o correo ya registrados.
- Selección de departamento, provincia y distrito desde el catálogo de ubigeo del INEI, con búsqueda por código o por nombre de distrito.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java | Lenguaje de la aplicación |
| Swing | Interfaz gráfica de escritorio |
| JDBC (`ojdbc`) | Acceso a datos con `PreparedStatement` y `CallableStatement` |
| Oracle Database | Persistencia, restricciones y procedimientos almacenados (PL/SQL) |

## Base de datos

Modelo:

```mermaid
erDiagram
    UBIGEO ||--o{ POSTULANTE : "ubica"
    UBIGEO {
        CHAR6 UBIGEO_ID PK
        VARCHAR2 DEPARTAMENTO
        VARCHAR2 PROVINCIA
        VARCHAR2 DISTRITO
    }
    POSTULANTE {
        NUMBER POSTULANTE_ID PK
        VARCHAR2 NOMBRES
        VARCHAR2 APELLIDO_PATERNO
        VARCHAR2 APELLIDO_MATERNO
        CHAR8 DNI UK
        DATE FECHA_NACIMIENTO
        VARCHAR2 TELEFONO_FIJO
        VARCHAR2 TELEFONO_CELULAR
        VARCHAR2 CORREO UK
        CHAR1 SEXO
        VARCHAR2 DIRECCION
        CHAR6 UBIGEO_ID FK
    }
```

Los scripts están en [`database/`](database) y se ejecutan en orden:

| Script | Usuario | Contenido |
|---|---|---|
| [`00-create-user.sql`](database/00-create-user.sql) | `SYSTEM` | Tablespace `POSTULANTES_DATA` y usuario `POSTULANTES` |
| [`01-schema.sql`](database/01-schema.sql) | `POSTULANTES` | Tablas `UBIGEO` y `POSTULANTE`, restricciones y secuencia `SEQ_POSTULANTE` |
| [`02-pkg-postulante.sql`](database/02-pkg-postulante.sql) | `POSTULANTES` | Paquete `PKG_POSTULANTE` con `SP_INSERTAR`, `SP_ACTUALIZAR` y `SP_BUSCAR` |
| [`03-seed.sql`](database/03-seed.sql) | `POSTULANTES` | Ubigeos y postulantes de prueba |

## Cómo ejecutar

Requisitos: JDK 25, Apache NetBeans y Docker.

**1. Levantar Oracle Database Free** (puerto `1522`):

```bash
cd database
cp .env.example .env        # definir ORACLE_PWD y APP_PWD
docker compose up -d
```

La primera vez tarda unos minutos; está lista cuando `docker ps` muestra el contenedor `candidate-management-oracle` como `healthy`.

**2. Crear el usuario y el esquema:**

```bash
source .env
docker cp . candidate-management-oracle:/tmp/db
docker exec -it candidate-management-oracle sqlplus system/$ORACLE_PWD@//localhost:1521/FREEPDB1 @/tmp/db/00-create-user.sql $APP_PWD
docker exec -it candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/01-schema.sql
docker exec -it candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/02-pkg-postulante.sql
docker exec -it candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/03-seed.sql
```

Cada `sqlplus` queda en su consola al terminar el script; se sale con `exit`.

**3. Configurar la conexión:** copiar `AppPostulantes/db.properties.example` como `AppPostulantes/db.properties` y poner la misma contraseña de `APP_PWD`.

**4. Ejecutar:** abrir `AppPostulantes` en NetBeans y ejecutar con **F6**. El driver `ojdbc11` ya está incluido en `AppPostulantes/lib/`.

## Estructura

```
candidate-management-desktop/
├── database/                        scripts SQL y docker-compose de Oracle
├── AppPostulantes/                  proyecto NetBeans (Ant)
│   ├── lib/ojdbc11.jar              driver JDBC de Oracle
│   ├── db.properties.example        plantilla de conexión
│   └── src/com/co/java/core/
│       ├── app/                     punto de entrada
│       └── persistencia/util/       conexión, parámetros y excepción de base de datos
└── README.md
```

## Avance

| Módulo | Estado |
|---|---|
| Base de datos y procedimientos almacenados | ✅ |
| Proyecto NetBeans y conexión JDBC | ✅ |
| Capa de persistencia (JDBC) | ⏳ |
| Validaciones | ⏳ |
| Carga y búsqueda de ubigeo | ⏳ |
| Interfaz Swing | ⏳ |

## Autor

**Jairo Alejandro Castañeda Pedraza**
