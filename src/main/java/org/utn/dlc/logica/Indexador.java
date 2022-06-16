package org.utn.dlc.logica;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.dominio.Vocabulario;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

public abstract class Indexador implements Runnable {


    private static Hashtable<Integer, Documento> documentos = new Hashtable<>();
    //La variable contiene todo el vocavulario hasta el momento
    private static Hashtable<Integer, Vocabulario> vocabulario = new Hashtable<>();
    //La variable controla las palabras del documento que se esta leyendo en ese momento
    private static Hashtable<Integer, Vocabulario> vocabularioDocActual;
    private static Hashtable<Integer, Vocabulario> vocabularioParaActualizar;
    private static Hashtable<Integer, Vocabulario> vocabularioParaInsertar;
    private static HashSet<Posteo> posteos;
    private static Documento documentoActual;

    /**
     * Comienza la indexacion de los archivos
     *
     * @throws FileNotFoundException
     */
    public static void indexar(String ruta) throws Exception {

        //Se carga en memoria todos los documentos que ya es encuentra guardado
        documentos = Documento.findAllDocuments();
        //Se carga en memoria todo el vocabulario que ya es encuentra guardado
        vocabulario = Vocabulario.findAllWords();

        Scanner scanDocumentoActual;
        File carpeta = new File(ruta);

        String nombre;
        int idDocumento;
        String path;
        Timestamp fechaUltimaActualizacion;
        //Recorre cada documento ".txt" de la carpeta
        for (File file : Objects.requireNonNull(carpeta.listFiles((File pathname) -> pathname.getName().endsWith(".txt")))) {

            //Reinicio la variable
            vocabularioDocActual = new Hashtable<>();
            vocabularioParaActualizar = new Hashtable<>();
            vocabularioParaInsertar = new Hashtable<>();
            posteos = new HashSet<>();
            documentoActual = null;

            //seteo las variable con los datos del archivo actual
            nombre = file.getName();
            idDocumento = nombre.hashCode();
            path = file.getPath();
            fechaUltimaActualizacion = new Timestamp(file.lastModified());

            System.out.println(nombre);

            //Verifica si el documentoActual ya se encuentra y si no lo esta se guarda
            documentoActual = documentos.get(idDocumento);
            if (documentoActual != null) {
                long fecha = documentoActual.getFechaHoraActualizacion().getTime();
                long fechaAux = fechaUltimaActualizacion.getTime();
                //Verifica si el documentoActual guardado fue actualizado y si fue asi se actualiza la fecha
                //y si no lo fue se salta al proximo documento
                if (fecha != fechaAux) {

                    //Como el documentoActual guardado al parece fue actualizado se tiene que actualizar
                    documentoActual.setFechaHoraActualizacion(fechaUltimaActualizacion);
                    Documento.actualizarFechaDocumento(idDocumento, fechaUltimaActualizacion);
                } else{
                    continue;
                }
            } else {
                //Como el documentoActual no se encontraba se crea un documentoActual con los
                //datos del archivo que se esta leyedo, lo agrego al vector y lo guarda en la DB
                documentoActual = new Documento(idDocumento, nombre, path, fechaUltimaActualizacion);
                documentos.put(idDocumento, documentoActual);
                Documento.insertarDocumento(documentoActual);
            }

            //Se crea un Scanner que nos permitira leer el documento actual
            scanDocumentoActual = new Scanner(file, StandardCharsets.ISO_8859_1);

            //Recorre cada palabra del documentoActual y lo agrega al Vocabulario auxiliar y en caso de ya encontrarse
            //se le suma 1 a la cantidad
            recorrerPalabras(scanDocumentoActual);

            //Aca se controla si los terminos indexados en el documentoActual ya se encontraban en el vocabulario y si lo
            //estaban se guarda el de mayor frecuencia
            verificarExistenciaPalabra();

            if (vocabularioParaActualizar.size() > 0) {
                Vocabulario.actualizarPalabra(vocabularioParaActualizar);
                //Posteo.actualizarPosteos(posteos, documentoActual, vocabularioParaActualizar);
            }
            Vocabulario.insertarPalabras(vocabularioParaInsertar);
            Posteo.insertarPosteo(posteos);
        }
    }

    public static void verificarExistenciaDocumento() {

    }

    /**
     * Recive un scanner el cual se recorrera palabra por palabra y las ira agregando o acutualizando la cantidad
     * si este ya se encontraba
     *
     * @param scan Scanner del Archivo a leer
     */
    private static void recorrerPalabras(Scanner scan) {
        String palabra;
        System.out.println("Inicio");
        while (scan.hasNext()) {

            //Guardo la proxima palabra, previamente convirtiendola en minuscula
            palabra = scan.next().toLowerCase();
            //Obtengo el hashcode de la palabra actual
            int idPalabra = palabra.hashCode();

            //Pregunta si la palabra ya se encuentra y si lo esta, se aumenta su frecuencia y si no, la agrega
            if (vocabularioDocActual.containsKey(idPalabra)) {
                vocabularioDocActual.get(idPalabra).increaseMaxFrec();
            } else {
                vocabularioDocActual.put(idPalabra, new Vocabulario(palabra));
            }
        }
        System.out.println("fin");
    }

    public static void verificarExistenciaPalabra() {
        /*if (!vocabulario.isEmpty()) {

        } else {
            vocabulario = new Hashtable<>(vocabularioDocActual);
            vocabularioParaInsertar = new Hashtable<>(vocabulario);
        }*/

        int maxFrecPalabra;
        int maxFrecPalabraAux;

        //Recorro el vocabulario auxiliar palabra por palabras
        for (Vocabulario palabraAux : vocabularioDocActual.values()) {

            //Busca el id de la palabra
            int idPalabra = palabraAux.getIdPalabra();

            //Busca si la palabra ya se encuentra en el vocabulario, si lo esta se verifica la frecuencia
            // y si no se encuentra se guarda
            Vocabulario palabra = vocabulario.get(idPalabra);
            if (palabra != null) {
                palabra.increaseCantDoc();

                maxFrecPalabraAux = palabraAux.getMaxFrecuenciaPalabra();
                maxFrecPalabra = palabra.getMaxFrecuenciaPalabra();

                if (maxFrecPalabra < maxFrecPalabraAux) {
                    palabra.setMaxFrecuenciaPalabra(maxFrecPalabraAux);
                    vocabularioParaActualizar.put(idPalabra, palabra);
                }
            } else {
                vocabulario.put(idPalabra, palabraAux);
                vocabularioParaInsertar.put(idPalabra, palabraAux);
            }
            //Se agrega un posteo de la palabra y documento actual asi como la frecuencia
            //de la palabra en este
            posteos.add(new Posteo(palabraAux, documentoActual, palabraAux.getMaxFrecuenciaPalabra()));
        }
    }

    public Hashtable<Integer, Documento> getDocumentos() {
        return documentos;
    }

    public Hashtable<Integer, Vocabulario> getVocabulario() {
        return vocabulario;
    }

    public HashSet<Posteo> getPosteos() {
        return posteos;
    }


    public boolean checkChanges() {
        return true;
    }

}
