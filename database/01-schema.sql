-- =============================================================================
-- Candidate Management Desktop - Esquema de base de datos (Oracle)
-- Ejecutar con el usuario dueño del esquema de la aplicación.
-- =============================================================================

-- Para recrear el esquema desde cero, descomentar:
-- DROP TABLE POSTULANTE;
-- DROP TABLE UBIGEO;
-- DROP SEQUENCE SEQ_POSTULANTE;

-- -----------------------------------------------------------------------------
-- UBIGEO: departamentos, provincias y distritos del Perú (codificación INEI).
-- El código tiene 6 dígitos: 2 de departamento, 2 de provincia y 2 de distrito.
-- -----------------------------------------------------------------------------
CREATE TABLE UBIGEO(
    UBIGEO_ID     CHAR(6)       NOT NULL,
    DEPARTAMENTO  VARCHAR2(60)  NOT NULL,
    PROVINCIA     VARCHAR2(60)  NOT NULL,
    DISTRITO      VARCHAR2(60)  NOT NULL,
    CONSTRAINT PK_UBIGEO PRIMARY KEY (UBIGEO_ID)
);

-- -----------------------------------------------------------------------------
-- POSTULANTE: datos personales del postulante.
-- Las validaciones de formato se hacen en Java; aquí quedan las reglas
-- que la base de datos debe garantizar aunque se inserte por fuera de la app.
-- -----------------------------------------------------------------------------
CREATE TABLE POSTULANTE(
    POSTULANTE_ID     NUMBER(8)      NOT NULL,
    NOMBRES           VARCHAR2(60)   NOT NULL,
    APELLIDO_PATERNO  VARCHAR2(60)   NOT NULL,
    APELLIDO_MATERNO  VARCHAR2(60),
    DNI               CHAR(8)        NOT NULL,
    FECHA_NACIMIENTO  DATE           NOT NULL,
    TELEFONO_FIJO     VARCHAR2(20),
    TELEFONO_CELULAR  VARCHAR2(20),
    CORREO            VARCHAR2(120)  NOT NULL,
    SEXO              CHAR(1)        NOT NULL,
    DIRECCION         VARCHAR2(200)  NOT NULL,
    UBIGEO_ID         CHAR(6)        NOT NULL,
    CONSTRAINT PK_POSTULANTE          PRIMARY KEY (POSTULANTE_ID),
    CONSTRAINT UQ_POSTULANTE_DNI      UNIQUE (DNI),
    CONSTRAINT UQ_POSTULANTE_CORREO   UNIQUE (CORREO),
    CONSTRAINT CK_POSTULANTE_SEXO     CHECK (SEXO IN ('F', 'M')),
    CONSTRAINT CK_POSTULANTE_TELEFONO CHECK (TELEFONO_FIJO IS NOT NULL OR TELEFONO_CELULAR IS NOT NULL),
    CONSTRAINT FK_POSTULANTE_UBIGEO   FOREIGN KEY (UBIGEO_ID) REFERENCES UBIGEO (UBIGEO_ID)
);

CREATE SEQUENCE SEQ_POSTULANTE;
