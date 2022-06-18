package org.utn.dlc.logica;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.dominio.Vocabulario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Indexador implements Runnable {


    private static Hashtable<Integer, Documento> documentos = new Hashtable<>();
    //La variable contiene todo el vocavulario hasta el momento
    private static Hashtable<Integer, Vocabulario> vocabulario = new Hashtable<>();
    //La variable controla las palabras del documento que se esta leyendo en ese momento
    private static Hashtable<Integer, Vocabulario> vocabularioDocActual = new Hashtable<>();
    private static Hashtable<Integer, Vocabulario> vocabularioParaActualizar = new Hashtable<>();
    private static Hashtable<Integer, Vocabulario> vocabularioParaInsertar = new Hashtable<>();
    private static HashSet<Posteo> posteos = new HashSet<>();
    private static HashSet<Posteo> posteosParaActualizar = new HashSet<>();
    private static Documento documentoActual;

    @Override
    public void run() {

    }

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
        BufferedReader bfDoc;
        File carpeta = new File(ruta);
        long ini;
        //Recorre cada documento ".txt" de la carpeta
        for (File file : Objects.requireNonNull(carpeta.listFiles((File pathname) -> pathname.getName().endsWith(".txt")))) {
            ini =  System.currentTimeMillis();
            if (verificarExistenciaDocumento(file))
                continue;

            //Se crea un Scanner que nos permitira leer el documento actual
            scanDocumentoActual = new Scanner(file, StandardCharsets.ISO_8859_1);
            bfDoc = new BufferedReader(new FileReader(file));

            //Recorre cada palabra del documentoActual y lo agrega al Vocabulario auxiliar y en caso de ya encontrarse
            //se le suma 1 a la cantidad
            recorrerPalabras(scanDocumentoActual);

            //Aca se controla si los terminos indexados en el documentoActual ya se encontraban en el vocabulario y si lo
            //estaban se guarda el de mayor frecuencia
            verificarExistenciaPalabra();

            //Verifica si hay documentos para actualizar
            if (vocabularioParaActualizar.size() > 0) {
                Vocabulario.actualizarPalabra(vocabularioParaActualizar);
                //Posteo.actualizarPosteos(posteos, documentoActual, vocabularioParaActualizar);
            }

            //Se procese a realizar la insercion de las nuevas palabras
            long vi = System.currentTimeMillis();
            Vocabulario.insertarPalabras(vocabularioParaInsertar);
            long vf = System.currentTimeMillis();
            System.out.println("Duracion insercion palabras: " + (vf-vi));

            //Se procese a realizar la insercion de los posteos
            long pi = System.currentTimeMillis();
            Posteo.insertarPosteos(posteos);
            long pf = System.currentTimeMillis();
            System.out.println("Duracion insercion posteos: " + (pf-pi));

            //reinicio variables
            documentoActual = null;
            vocabularioParaActualizar = new Hashtable<>();
            vocabularioParaInsertar = new Hashtable<>();
            posteos = new HashSet<>();
            long fin = System.currentTimeMillis();
            System.out.println("Duracion insercion: s-" + (double)(fin - ini)/1000);

            vocabularioDocActual = new Hashtable<>();
        }
    }

    public static boolean verificarExistenciaDocumento(File file) {
        //seteo las variable con los datos del archivo actual
        String nombre = file.getName();
        int idDocumento = nombre.hashCode();
        String path = file.getPath();
        Timestamp fechaUltimaActualizacion = new Timestamp(file.lastModified());

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
                return true;
            }
        } else {
            //Como el documentoActual no se encontraba se crea un documentoActual con los
            //datos del archivo que se esta leyedo, lo agrego al vector y lo guarda en la DB
            documentoActual = new Documento(idDocumento, nombre, path, fechaUltimaActualizacion);
            documentos.put(idDocumento, documentoActual);
            Documento.insertarDocumento(documentoActual);
        }
        return false;
    }

    /**
     * Recive un scanner el cual se recorrera palabra por palabra y las ira agregando o acutualizando la cantidad
     * si este ya se encontraba
     *
     * @param scan Scanner del Archivo a leer
     */
    private static void recorrerPalabras(Scanner scan) throws InterruptedException {
        String palabra = null;
        //scan.useDelimiter("[\\w\\S]+[-\\w]");
        //scan.useDelimiter("[a-zA-Z0-9á-úÁ-Úä-ÿ]+");
        //scan.useDelimiter("([\\w]+@[\\w]+.[\\w]+)|([a-zA-Z0-9á-úÁ-Úä-ÿ]+([^a-zA-Z0-9á-úÁ-Úä-ÿ\\s]?[a-zA-Z0-9á-úÁ-Úä-ÿ]+)?)");
        //Pattern pat = Pattern.compile("([\\w]+@[\\w]+.[\\w]+)|([a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù]+([^a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù\\s]?[a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù]+)?)");
        //String reg = "([\\w]+@[\\w]+.[\\w]+)|([\\p{L}0-9]+([^\\p{L}\\s]?[\\p{L}0-9]+)?)";
        scan.useDelimiter("[^\\p{L}\\d]|[\\d]{2,}");

        while (scan.hasNext()) {
            //Guardo la proxima palabra, previamente convirtiendola en minuscula
            palabra = scan.next().toLowerCase();
            if (palabra.length()==0)
                continue;
            //System.out.println(palabra);
            //Obtengo el hashcode de la palabra actual
            int idPalabra = palabra.hashCode();
            //Pregunta si la palabra ya se encuentra y si lo esta, se aumenta su frecuencia y si no, la agrega
            if (vocabularioDocActual.containsKey(idPalabra)) {
                vocabularioDocActual.get(idPalabra).increaseMaxFrec();
            } else {
                vocabularioDocActual.put(idPalabra, new Vocabulario(palabra));
            }
        }
    }
    private static void recorrerPalabras(BufferedReader bufferedReader) throws InterruptedException, IOException {
        String palabra = null;
        //scan.useDelimiter("[\\w\\S]+[-\\w]");
        //scan.useDelimiter("[a-zA-Z0-9á-úÁ-Úä-ÿ]+");
        //scan.useDelimiter("([\\w]+@[\\w]+.[\\w]+)|([a-zA-Z0-9á-úÁ-Úä-ÿ]+([^a-zA-Z0-9á-úÁ-Úä-ÿ\\s]?[a-zA-Z0-9á-úÁ-Úä-ÿ]+)?)");
        //Pattern pat = Pattern.compile("([\\w]+@[\\w]+.[\\w]+)|([a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù]+([^a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù\\s]?[a-zA-Z0-9á-úÁ-Úä-ÿâ-ûà-ù]+)?)");
        //String reg = "([\\w]+@[\\w]+.[\\w]+)|([\\p{L}0-9]+([^\\p{L}\\s]?[\\p{L}0-9]+)?)";
        Pattern pat = Pattern.compile("[^\\p{L}\\d]|[\\d]{2,}");
        Matcher mat;
        while ((palabra = bufferedReader.readLine()) != null) {
            mat = pat.matcher(palabra);
            //Guardo la proxima palabra, previamente convirtiendola en minuscula
            while (mat.find()){
                for (int i = 0; i < mat.groupCount(); i++) {
                    System.out.println(mat.group(i));
                }
            }
            Thread.sleep(200);
            //System.out.println(palabra);
            //Obtengo el hashcode de la palabra actual
            int idPalabra = palabra.hashCode();
            //Pregunta si la palabra ya se encuentra y si lo esta, se aumenta su frecuencia y si no, la agrega
            if (vocabularioDocActual.containsKey(idPalabra)) {
                vocabularioDocActual.get(idPalabra).increaseMaxFrec();
            } else {
                vocabularioDocActual.put(idPalabra, new Vocabulario(palabra));
            }
        }
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
                //Se incrementa en +1 la cantidad de documento en las que aparece
                palabra.increaseCantDoc();

                //Se determina la frecuencia maxima que se guardara de la palabra
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
            //Se agrega un posteo de la palabra y documento actual asi como la frecuencia de la palabra en este
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
