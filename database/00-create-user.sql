-- =============================================================================
-- Candidate Management Desktop - Tablespace y usuario dueño del esquema
-- Ejecutar con SYSTEM conectado a la PDB FREEPDB1.
-- Recibe la contraseña del usuario como primer parámetro, por ejemplo:
--   sqlplus system/<ORACLE_PWD>@//localhost:1521/FREEPDB1 @00-create-user.sql <APP_PWD>
-- =============================================================================

-- Tablespace propio de la aplicación. El archivo de datos se crea en la misma
-- carpeta que los de la PDB (la imagen "lite" de Oracle Free no trae USERS).
DECLARE
    V_EXISTE  NUMBER;
    V_RUTA    VARCHAR2(500);
BEGIN
    SELECT COUNT(*) INTO V_EXISTE FROM DBA_TABLESPACES WHERE TABLESPACE_NAME = 'POSTULANTES_DATA';
    IF V_EXISTE = 0 THEN
        SELECT SUBSTR(FILE_NAME, 1, INSTR(FILE_NAME, '/', -1))
          INTO V_RUTA
          FROM DBA_DATA_FILES
         WHERE TABLESPACE_NAME = 'SYSTEM';

        EXECUTE IMMEDIATE 'CREATE TABLESPACE POSTULANTES_DATA DATAFILE '''
            || V_RUTA || 'postulantes_data01.dbf'' SIZE 50M AUTOEXTEND ON NEXT 10M MAXSIZE 500M';
    END IF;
END;
/

CREATE USER POSTULANTES IDENTIFIED BY "&1"
    DEFAULT TABLESPACE POSTULANTES_DATA
    QUOTA UNLIMITED ON POSTULANTES_DATA;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE VIEW TO POSTULANTES;
