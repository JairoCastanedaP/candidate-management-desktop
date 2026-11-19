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
        P_DNI               IN  CHAR,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_UBIGEO_ID         IN  CHAR,
        P_POSTULANTE_ID     OUT NUMBER
    );

    -- Actualiza todos los datos de un postulante existente.
    PROCEDURE SP_ACTUALIZAR(
        P_POSTULANTE_ID     IN  NUMBER,
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_DNI               IN  CHAR,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_UBIGEO_ID         IN  CHAR
    );

    -- Busca por nombres, apellido paterno o apellido materno (sin distinguir
    -- mayúsculas). Si P_TEXTO es nulo o vacío devuelve todos los postulantes.
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
        P_DNI               IN  CHAR,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_UBIGEO_ID         IN  CHAR,
        P_POSTULANTE_ID     OUT NUMBER
    ) AS
    BEGIN
        SELECT SEQ_POSTULANTE.NEXTVAL INTO P_POSTULANTE_ID FROM DUAL;

        INSERT INTO POSTULANTE(POSTULANTE_ID, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO,
                               DNI, FECHA_NACIMIENTO, TELEFONO_FIJO, TELEFONO_CELULAR,
                               CORREO, SEXO, DIRECCION, UBIGEO_ID)
        VALUES(P_POSTULANTE_ID, P_NOMBRES, P_APELLIDO_PATERNO, P_APELLIDO_MATERNO,
               P_DNI, P_FECHA_NACIMIENTO, P_TELEFONO_FIJO, P_TELEFONO_CELULAR,
               P_CORREO, P_SEXO, P_DIRECCION, P_UBIGEO_ID);

        COMMIT;
    END SP_INSERTAR;

    PROCEDURE SP_ACTUALIZAR(
        P_POSTULANTE_ID     IN  NUMBER,
        P_NOMBRES           IN  VARCHAR2,
        P_APELLIDO_PATERNO  IN  VARCHAR2,
        P_APELLIDO_MATERNO  IN  VARCHAR2,
        P_DNI               IN  CHAR,
        P_FECHA_NACIMIENTO  IN  DATE,
        P_TELEFONO_FIJO     IN  VARCHAR2,
        P_TELEFONO_CELULAR  IN  VARCHAR2,
        P_CORREO            IN  VARCHAR2,
        P_SEXO              IN  CHAR,
        P_DIRECCION         IN  VARCHAR2,
        P_UBIGEO_ID         IN  CHAR
    ) AS
    BEGIN
        UPDATE POSTULANTE
           SET NOMBRES          = P_NOMBRES,
               APELLIDO_PATERNO = P_APELLIDO_PATERNO,
               APELLIDO_MATERNO = P_APELLIDO_MATERNO,
               DNI              = P_DNI,
               FECHA_NACIMIENTO = P_FECHA_NACIMIENTO,
               TELEFONO_FIJO    = P_TELEFONO_FIJO,
               TELEFONO_CELULAR = P_TELEFONO_CELULAR,
               CORREO           = P_CORREO,
               SEXO             = P_SEXO,
               DIRECCION        = P_DIRECCION,
               UBIGEO_ID        = P_UBIGEO_ID
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
        V_TEXTO VARCHAR2(4000) :='%' || UPPER(TRIM(P_TEXTO)) || '%';
    BEGIN
        OPEN P_CURSOR FOR
            SELECT P.POSTULANTE_ID, P.NOMBRES, P.APELLIDO_PATERNO, P.APELLIDO_MATERNO,
                   P.DNI, P.FECHA_NACIMIENTO, P.TELEFONO_FIJO, P.TELEFONO_CELULAR,
                   P.CORREO, P.SEXO, P.DIRECCION, P.UBIGEO_ID,
                   U.DEPARTAMENTO, U.PROVINCIA, U.DISTRITO
              FROM POSTULANTE P
              JOIN UBIGEO U ON U.UBIGEO_ID = P.UBIGEO_ID
             WHERE TRIM(P_TEXTO) IS NULL
                OR UPPER(P.NOMBRES)          LIKE V_TEXTO
                OR UPPER(P.APELLIDO_PATERNO) LIKE V_TEXTO
                OR UPPER(P.APELLIDO_MATERNO) LIKE V_TEXTO
             ORDER BY P.APELLIDO_PATERNO, P.APELLIDO_MATERNO, P.NOMBRES;
    END SP_BUSCAR;

END PKG_POSTULANTE;
/
