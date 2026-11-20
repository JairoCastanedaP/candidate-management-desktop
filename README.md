<div align="center">

# Candidate Management Desktop

Aplicación de escritorio para gestionar postulantes: registro, actualización, eliminación, listado y búsqueda, con validación de datos para Colombia y ubicación por departamento y municipio (DIVIPOLA del DANE).

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Swing-5382A1)
![JDBC](https://img.shields.io/badge/JDBC-ojdbc-F80000)
![Oracle](https://img.shields.io/badge/Oracle-Database-F80000?logo=oracle&logoColor=white)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)

</div>

## Funcionalidades

- Registro, actualización, eliminación y listado de postulantes.
- Búsqueda por nombres, apellido paterno o apellido materno, sin distinguir mayúsculas ni tildes.
- Operaciones de registro, actualización y búsqueda mediante procedimientos almacenados.
- Documento de identidad colombiano por tipo y número: cédula de ciudadanía (CC), tarjeta de identidad (TI), cédula de extranjería (CE), pasaporte (PA) y permiso por protección temporal (PPT).
- Validación de nombres, documento, correo electrónico, teléfonos y sexo, con aviso de documento o correo ya registrados.
- Teléfonos con el formato colombiano: celular de 10 dígitos y fijo con el indicativo regional (601 a 608).
- Selección de departamento y municipio desde el catálogo DIVIPOLA del DANE, con búsqueda por código o por nombre de municipio.

### Reglas de validación

Antes de registrar o actualizar, `ValidadorPostulante` quita espacios sobrantes, pasa los textos a mayúsculas y devuelve todos los errores juntos.

| Dato | Regla |
|---|---|
| Nombres y apellidos | Entre 3 y 60 caracteres; solo letras, espacios y apóstrofo. El apellido materno es opcional. |
| Documento | CC de 6 a 10 dígitos, TI de 10 a 11, CE de 6 a 7, PPT de 6 a 8 y PA de 5 a 9 letras o números. No se repite: el mensaje indica a quién pertenece. |
| Correo | Validado sin expresiones regulares: una sola `@`, usuario y dominio válidos y extensión de al menos 2 letras. No se repite. |
| Teléfonos | Al menos uno; solo dígitos, espacios, `-` y `()`. Celular de 10 dígitos que empieza por 3; fijo de 10 dígitos con el indicativo del departamento del municipio. |
| Sexo | F o M. |

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
    DEPARTAMENTO ||--o{ MUNICIPIO : "contiene"
    MUNICIPIO ||--o{ POSTULANTE : "ubica"
    DEPARTAMENTO {
        CHAR2 DEPARTAMENTO_ID PK
        VARCHAR2 NOMBRE
        CHAR3 INDICATIVO
    }
    MUNICIPIO {
        CHAR5 MUNICIPIO_ID PK
        CHAR2 DEPARTAMENTO_ID FK
        VARCHAR2 NOMBRE
    }
    POSTULANTE {
        NUMBER POSTULANTE_ID PK
        VARCHAR2 NOMBRES
        VARCHAR2 APELLIDO_PATERNO
        VARCHAR2 APELLIDO_MATERNO
        VARCHAR2 TIPO_DOCUMENTO UK
        VARCHAR2 NUMERO_DOCUMENTO UK
        DATE FECHA_NACIMIENTO
        VARCHAR2 TELEFONO_FIJO
        VARCHAR2 TELEFONO_CELULAR
        VARCHAR2 CORREO UK
        CHAR1 SEXO
        VARCHAR2 DIRECCION
        CHAR5 MUNICIPIO_ID FK
    }
```

Los scripts están en [`database/`](database) y se ejecutan en orden:

| Script | Usuario | Contenido |
|---|---|---|
| [`00-create-user.sql`](database/00-create-user.sql) | `SYSTEM` | Tablespace `POSTULANTES_DATA` y usuario `POSTULANTES` |
| [`01-schema.sql`](database/01-schema.sql) | `POSTULANTES` | Tablas `DEPARTAMENTO`, `MUNICIPIO` y `POSTULANTE`, restricciones y secuencia `SEQ_POSTULANTE` |
| [`02-pkg-postulante.sql`](database/02-pkg-postulante.sql) | `POSTULANTES` | Paquete `PKG_POSTULANTE` con `SP_INSERTAR`, `SP_ACTUALIZAR` y `SP_BUSCAR` |
| [`03-departamentos.sql`](database/03-departamentos.sql) | `POSTULANTES` | Los 33 departamentos de Colombia con su indicativo de telefonía fija |
| [`04-seed.sql`](database/04-seed.sql) | `POSTULANTES` | Municipios y postulantes de prueba |
| [`05-municipios.sql`](database/05-municipios.sql) | `POSTULANTES` | Los 1.122 municipios de la DIVIPOLA del DANE ([datos.gov.co](https://www.datos.gov.co/d/gdxc-w37w)); se puede ejecutar varias veces |

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
export NLS_LANG=AMERICAN_AMERICA.AL32UTF8
docker cp . candidate-management-oracle:/tmp/db
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus system/$ORACLE_PWD@//localhost:1521/FREEPDB1 @/tmp/db/00-create-user.sql $APP_PWD
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/01-schema.sql
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/02-pkg-postulante.sql
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/03-departamentos.sql
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/04-seed.sql
docker exec -it -e NLS_LANG candidate-management-oracle sqlplus POSTULANTES/$APP_PWD@//localhost:1521/FREEPDB1 @/tmp/db/05-municipios.sql
```

`NLS_LANG` hace que `sqlplus` lea los scripts en UTF-8, para que las tildes (BOYACÁ, MEDELLÍN) se guarden bien.
Cada `sqlplus` queda en su consola al terminar el script; se sale con `exit`.

**3. Configurar la conexión:** copiar `AppPostulantes/db.properties.example` como `AppPostulantes/db.properties` y poner la misma contraseña de `APP_PWD`.

**4. Ejecutar:** abrir `AppPostulantes` en NetBeans y ejecutar con **F6**. El driver `ojdbc11` ya está incluido en `AppPostulantes/lib/`. Por ahora la aplicación recorre el CRUD en consola: registra, busca, actualiza y elimina un postulante de prueba.

## Estructura

```
candidate-management-desktop/
├── database/                        scripts SQL y docker-compose de Oracle
├── AppPostulantes/                  proyecto NetBeans (Ant)
│   ├── lib/ojdbc11.jar              driver JDBC de Oracle
│   ├── db.properties.example        plantilla de conexión
│   └── src/com/co/java/core/
│       ├── app/                     punto de entrada
│       ├── bean/                    Postulante, Municipio y Departamento
│       ├── persistencia/            interfaces DB e implementaciones JDBC
│       ├── persistencia/util/       conexión, parámetros y excepción de base de datos
│       └── validacion/              reglas de negocio del postulante
└── README.md
```

## Avance

| Módulo | Estado |
|---|---|
| Base de datos y procedimientos almacenados | ✅ |
| Proyecto NetBeans y conexión JDBC | ✅ |
| Capa de persistencia (JDBC) | ✅ |
| Validaciones | ✅ |
| Carga del catálogo completo de municipios (DIVIPOLA) | ✅ |
| Interfaz Swing | ⏳ |

## Autor

**Jairo Alejandro Castañeda Pedraza**
