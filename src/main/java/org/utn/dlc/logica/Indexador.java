package org.utn.dlc.logica;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.dominio.Vocabulario;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class Indexador implements Runnable {


    private static Hashtable<Integer, Documento> documentos = new Hashtable<>();
    //La variable contiene todo el vocavulario hasta el momento
    private static Hashtable<Integer,Vocabulario> vocabulario = new Hashtable<>();
    //La variable controla las palabras del documento que se esta leyendo en ese momento
    private static Hashtable<Integer, Vocabulario> vocabularioAux;
    private static Hashtable<Integer, Vocabulario> vocabularioParaActializar;
    private static HashSet<Posteo> posteos = new HashSet<>();

    /**
     * Comienza la indexacion de los archivos
     * @throws FileNotFoundException
     */
    public static void indexar(String ruta) throws Exception {

        //Se carga en memoria todos los documento
        documentos = Documento.buscarAllDocumentos();

        Documento documento;

        Scanner scanDocumentoActual;
        File carpeta = new File(ruta);

        String nombre;
        int idDocumento;
        String path;
        Date fechaUltimaActualizacion;

        //Se crea una bandera para controlar si se realziar un INSERT o un UPDATE
        //por defecto se es false se realiza un INSERT
        //boolean actualizarDocumento = false;

        //Recorre cada documento ".txt" de la carpeta
        for (File file: Objects.requireNonNull(carpeta.listFiles((File pathname) -> pathname.getName().endsWith(".txt")))){

            //Reinicio la variable
            vocabularioAux = new Hashtable<>();
            vocabularioParaActializar = new Hashtable<>();

            //seteo las variable con los datos del archivo actua
            nombre = file.getName();
            idDocumento = nombre.hashCode();
            path = file.getPath();
            fechaUltimaActualizacion = new Date(file.lastModified());

            //Verifica si el documento ya se encuentra
            documento = documentos.get(idDocumento);
            if (documento != null) {

                //Verifica si el documento guardado fue actualizado y fue asi se actualiza la fecha
                //y si no lo fue se salta al proximo documento
                if (documento.esActualizado(fechaUltimaActualizacion)) {

                    //Como el archivo ya se encontraba se actualiza
                    documento.setFechaHoraActualizacion(fechaUltimaActualizacion);
                    Documento.actualizarFechaDocumento(idDocumento, fechaUltimaActualizacion);
                }
                else
                    continue;
            }
            else{
                //Como el documento no se encontraba se crea un documento con los
                //datos del archivo que se esta leyedo, lo agrego al vector y lo guarda en la DB
                documento = new Documento(idDocumento, nombre, path, fechaUltimaActualizacion);
                documentos.put(idDocumento, documento);
                Documento.insertarDocumento(documento);
            }

            //Se crea un Scanner que nos permitira leer el documento actual
            scanDocumentoActual = new Scanner(file, StandardCharsets.ISO_8859_1);

            //Recorre cada palabra del documento y lo agrega al Vocabulario auxiliar y en caso de ya encontrarse
            //se le suma 1 a la cantidad
            recorrerPalabras(scanDocumentoActual);

            //Aca se controla si los terminos indexados en el documento ya se encontraban en el vocabulario y si lo
            //estaban se guada el de mayor frecuencia
            if (!vocabulario.isEmpty()) {
                int maxFrecPalabra;
                int maxFrecPalabraAux;

                //Recorro del vocabulario auxiliar palabra por palabras
                for (Vocabulario palabraAux : vocabularioAux.values()) {

                    //Busca el id de la palabra
                    int idPalabra = palabraAux.getIdPalabra();

                    //Busca si la parala ya se encuentra en el vocabulario y si esta se guarda la palabra
                    // y si no esta sera NULL
                    Vocabulario palabra = vocabulario.get(idPalabra);

                    if (palabra != null) {
                        palabra.increaseCantDoc();

                        maxFrecPalabraAux = palabraAux.getMaxFrecuenciaPalabra();
                        maxFrecPalabra = palabra.getMaxFrecuenciaPalabra();

                        if (maxFrecPalabra < maxFrecPalabraAux) {
                            palabra.setMaxFrecuenciaPalabra(maxFrecPalabraAux);
                        }

                        vocabularioParaActializar.put(palabra.getIdPalabra(), palabra);

                        //Se agrega un posteo de la palabra y documento actual asi como la frecuencia
                        //de la palabra en este
                        posteos.add(new Posteo(palabra, documento, maxFrecPalabraAux));

                    } else {
                        vocabulario.put(idPalabra, palabraAux);
                    }
                }
            } else {
                vocabulario = new Hashtable<>(vocabularioAux);
            }

            if (actualizarDocumento) {
                Documento.actualizarFechaDocumento(documento);
                actualizarDocumento = false;
            }else {
                Documento.insertarDocumento(documento);
            }

            Vocabulario.insertarPalabra(vocabulario);
            Vocabulario.actualizarPalabra(vocabularioParaActializar);
            Posteo.insertarPosteo(posteos);
        }
    }

    /**
     * Recive un scanner el cual se recorrera palabra por palabra y las ira agregando o acutualizando la cantidad
     * si este ya se encontraba
     * @param scan Scanner del Archivo a leer
     */
    private static void recorrerPalabras(Scanner scan){
        String palabra;
        while (scan.hasNext()) {

            //Guardo la proxima palabra, previamente convirtiendola en minuscula
            palabra = scan.next().toLowerCase();

            //Obtengo el hashcode de la palabra actual
            int idPalabra = palabra.hashCode();

            //Pregunta si la palabra ya se encuentra y si lo esta, se aumenta su frecuencia y si no, la agrega
            if (vocabularioAux.containsKey(idPalabra)) {
                vocabularioAux.get(idPalabra).increaseMaxFrec();
            } else {
                vocabularioAux.put(idPalabra, new Vocabulario(palabra));
            }
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


    public boolean checkChanges(){
        return true;
    }

}
