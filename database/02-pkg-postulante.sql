-- =============================================================================
-- Candidate Management Desktop - Paquete PKG_POSTULANTE
-- Procedimientos almacenados que usa la capa de persistencia (CallableStatement).
-- En SQL Developer ejecutar con "Ejecutar script" (F5).
-- =============================================================================

CREATE OR REPLACE PACKAGE PKG_POSTULANTE AS

    -- Registra un postulante y devuelve el id generado.
    PROCEDURE SP_INSERTAR(
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_TIPO_DOCUMENTO    IN  VARCHAR2,
        P_NUMERO_DOCUMENTO  IN  VARCHAR2,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_MUNICIPIO_ID      IN  CHAR,
        P_POSTULANTE_ID     OUT NUMBER
    );

    -- Actualiza todos los datos de un postulante existente.
    PROCEDURE SP_ACTUALIZAR(
        P_POSTULANTE_ID     IN  NUMBER,
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_TIPO_DOCUMENTO    IN  VARCHAR2,
        P_NUMERO_DOCUMENTO  IN  VARCHAR2,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_MUNICIPIO_ID      IN  CHAR
    );

    -- Busca por nombres, apellido paterno o apellido materno sin distinguir
    -- mayúsculas ni tildes ("perez" encuentra "PÉREZ").
    -- Si P_TEXTO es nulo o vacío devuelve todos los postulantes.
    PROCEDURE SP_BUSCAR(
        P_CURSOR  OUT SYS_REFCURSOR,
        P_TEXTO   IN  VARCHAR2
    );

END PKG_POSTULANTE;
/

CREATE OR REPLACE PACKAGE BODY PKG_POSTULANTE AS

    PROCEDURE SP_INSERTAR(
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_TIPO_DOCUMENTO    IN  VARCHAR2,
        P_NUMERO_DOCUMENTO  IN  VARCHAR2,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_MUNICIPIO_ID      IN  CHAR,
        P_POSTULANTE_ID     OUT NUMBER
    ) AS
    BEGIN
        SELECT SEQ_POSTULANTE.NEXTVAL INTO P_POSTULANTE_ID FROM DUAL;

        INSERT INTO POSTULANTE(POSTULANTE_ID, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO,
                               TIPO_DOCUMENTO, NUMERO_DOCUMENTO, FECHA_NACIMIENTO,
                               TELEFONO_FIJO, TELEFONO_CELULAR, CORREO, SEXO, DIRECCION, MUNICIPIO_ID)
        VALUES(P_POSTULANTE_ID, P_NOMBRES, P_APELLIDO_PATERNO, P_APELLIDO_MATERNO,
               P_TIPO_DOCUMENTO, P_NUMERO_DOCUMENTO, P_FECHA_NACIMIENTO,
               P_TELEFONO_FIJO, P_TELEFONO_CELULAR, P_CORREO, P_SEXO, P_DIRECCION, P_MUNICIPIO_ID);

        COMMIT;
    END SP_INSERTAR;

    PROCEDURE SP_ACTUALIZAR(
        P_POSTULANTE_ID     IN  NUMBER,
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_TIPO_DOCUMENTO    IN  VARCHAR2,
        P_NUMERO_DOCUMENTO  IN  VARCHAR2,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_MUNICIPIO_ID      IN  CHAR
    ) AS
    BEGIN
        UPDATE POSTULANTE
           SET NOMBRES          = P_NOMBRES,
               APELLIDO_PATERNO = P_APELLIDO_PATERNO,
               APELLIDO_MATERNO = P_APELLIDO_MATERNO,
               TIPO_DOCUMENTO   = P_TIPO_DOCUMENTO,
               NUMERO_DOCUMENTO = P_NUMERO_DOCUMENTO,
               FECHA_NACIMIENTO = P_FECHA_NACIMIENTO,
               TELEFONO_FIJO    = P_TELEFONO_FIJO,
               TELEFONO_CELULAR = P_TELEFONO_CELULAR,
               CORREO           = P_CORREO,
               SEXO             = P_SEXO,
               DIRECCION        = P_DIRECCION,
               MUNICIPIO_ID     = P_MUNICIPIO_ID
         WHERE POSTULANTE_ID = P_POSTULANTE_ID;

        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'No existe el postulante con id ' || P_POSTULANTE_ID);
        END IF;

        COMMIT;
    END SP_ACTUALIZAR;

    PROCEDURE SP_BUSCAR(
        P_CURSOR  OUT SYS_REFCURSOR,
        P_TEXTO   IN  VARCHAR2
    ) AS
        V_TEXTO VARCHAR2(4000) := '%' || TRIM(P_TEXTO) || '%';
    BEGIN
        OPEN P_CURSOR FOR
            SELECT P.POSTULANTE_ID, P.NOMBRES, P.APELLIDO_PATERNO, P.APELLIDO_MATERNO,
                   P.TIPO_DOCUMENTO, P.NUMERO_DOCUMENTO, P.FECHA_NACIMIENTO,
                   P.TELEFONO_FIJO, P.TELEFONO_CELULAR, P.CORREO, P.SEXO, P.DIRECCION,
                   M.MUNICIPIO_ID, M.NOMBRE AS MUNICIPIO,
                   D.DEPARTAMENTO_ID, D.NOMBRE AS DEPARTAMENTO, D.INDICATIVO
              FROM POSTULANTE P
              JOIN MUNICIPIO M    ON M.MUNICIPIO_ID = P.MUNICIPIO_ID
              JOIN DEPARTAMENTO D ON D.DEPARTAMENTO_ID = M.DEPARTAMENTO_ID
             WHERE TRIM(P_TEXTO) IS NULL
                -- BINARY_AI: comparación sin distinguir mayúsculas ni tildes
                OR P.NOMBRES          COLLATE BINARY_AI LIKE V_TEXTO
                OR P.APELLIDO_PATERNO COLLATE BINARY_AI LIKE V_TEXTO
                OR P.APELLIDO_MATERNO COLLATE BINARY_AI LIKE V_TEXTO
             ORDER BY P.APELLIDO_PATERNO, P.APELLIDO_MATERNO, P.NOMBRES;
    END SP_BUSCAR;

END PKG_POSTULANTE;
/
