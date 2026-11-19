-- =============================================================================
-- Candidate Management Desktop - Esquema de base de datos (Oracle)
-- Ejecutar con el usuario dueño del esquema de la aplicación (POSTULANTES).
-- =============================================================================

-- Para recrear el esquema desde cero, descomentar:
-- DROP TABLE POSTULANTE;
-- DROP TABLE MUNICIPIO;
-- DROP TABLE DEPARTAMENTO;
-- DROP SEQUENCE SEQ_POSTULANTE;

-- -----------------------------------------------------------------------------
-- DEPARTAMENTO: departamentos de Colombia según la codificación DIVIPOLA del DANE
-- (2 dígitos). INDICATIVO es el prefijo de telefonía fija de la región
-- (601 a 608), que se usa para validar los teléfonos fijos.
-- -----------------------------------------------------------------------------
CREATE TABLE DEPARTAMENTO(
    DEPARTAMENTO_ID  CHAR(2)       NOT NULL,
    NOMBRE           VARCHAR2(100) NOT NULL,
    INDICATIVO       CHAR(3)       NOT NULL,
    CONSTRAINT PK_DEPARTAMENTO PRIMARY KEY (DEPARTAMENTO_ID)
);

-- -----------------------------------------------------------------------------
-- MUNICIPIO: municipios de Colombia (DIVIPOLA). El código tiene 5 dígitos:
-- 2 del departamento y 3 del municipio. Ejemplo: 15001 = Tunja (Boyacá).
-- -----------------------------------------------------------------------------
CREATE TABLE MUNICIPIO(
    MUNICIPIO_ID     CHAR(5)       NOT NULL,
    DEPARTAMENTO_ID  CHAR(2)       NOT NULL,
    NOMBRE           VARCHAR2(100) NOT NULL,
    CONSTRAINT PK_MUNICIPIO              PRIMARY KEY (MUNICIPIO_ID),
    CONSTRAINT FK_MUNICIPIO_DEPARTAMENTO FOREIGN KEY (DEPARTAMENTO_ID) REFERENCES DEPARTAMENTO (DEPARTAMENTO_ID)
);

-- -----------------------------------------------------------------------------
-- POSTULANTE: datos personales del postulante.
-- Las validaciones de formato se hacen en Java; aquí quedan las reglas
-- que la base de datos debe garantizar aunque se inserte por fuera de la app.
-- Tipos de documento: CC cédula de ciudadanía, TI tarjeta de identidad,
-- CE cédula de extranjería, PA pasaporte, PPT permiso por protección temporal.
-- -----------------------------------------------------------------------------
CREATE TABLE POSTULANTE(
    POSTULANTE_ID     NUMBER(8)      NOT NULL,
    NOMBRES           VARCHAR2(60)   NOT NULL,
    APELLIDO_PATERNO  VARCHAR2(60)   NOT NULL,
    APELLIDO_MATERNO  VARCHAR2(60),
    TIPO_DOCUMENTO    VARCHAR2(3)    NOT NULL,
    NUMERO_DOCUMENTO  VARCHAR2(12)   NOT NULL,
    FECHA_NACIMIENTO  DATE           NOT NULL,
    TELEFONO_FIJO     VARCHAR2(20),
    TELEFONO_CELULAR  VARCHAR2(20),
    CORREO            VARCHAR2(120)  NOT NULL,
    SEXO              CHAR(1)        NOT NULL,
    DIRECCION         VARCHAR2(200)  NOT NULL,
    MUNICIPIO_ID      CHAR(5)        NOT NULL,
    CONSTRAINT PK_POSTULANTE           PRIMARY KEY (POSTULANTE_ID),
    CONSTRAINT UQ_POSTULANTE_DOCUMENTO UNIQUE (TIPO_DOCUMENTO, NUMERO_DOCUMENTO),
    CONSTRAINT UQ_POSTULANTE_CORREO    UNIQUE (CORREO),
    CONSTRAINT CK_POSTULANTE_TIPO_DOC  CHECK (TIPO_DOCUMENTO IN ('CC', 'TI', 'CE', 'PA', 'PPT')),
    CONSTRAINT CK_POSTULANTE_SEXO      CHECK (SEXO IN ('F', 'M')),
    CONSTRAINT CK_POSTULANTE_TELEFONO  CHECK (TELEFONO_FIJO IS NOT NULL OR TELEFONO_CELULAR IS NOT NULL),
    CONSTRAINT FK_POSTULANTE_MUNICIPIO FOREIGN KEY (MUNICIPIO_ID) REFERENCES MUNICIPIO (MUNICIPIO_ID)
);

CREATE SEQUENCE SEQ_POSTULANTE;
