package costa.barreto.alessandro.camerareceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Alessandro on 29/07/2015.
 */
public class CameraReceiver extends BroadcastReceiver {

    private String caminhoDaImagem ="";

    @Override
    public void onReceive(Context context, Intent intent) {

            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(intent.getData(), projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATA);
            if(cursor.moveToFirst()){
                caminhoDaImagem =  cursor.getString(column_index);
            }
            cursor.close();

            if (temConexao(context)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        enviarImagemJsonBase64(caminhoDaImagem);
                    }
                }).start();
            }else{
                Log.i("TAG", "SEM CONEXAO");
            }

    }

    /**
     * Metodo para verificar se existe conexao no aparelho
     * @param ctx contexto da class
     * @return true ou false
     */
    private boolean temConexao(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * Metodo para codificar a imagem em base64
     * @param path caminho da imagem no device
     * @return string codificada
     */
    private String encodeImage(String path)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getResizedBitmap(bm,250,250).compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;

    }

    /**
     * Metodo para redimensionar a imagem
     * @param bm o bitmap
     * @param newWidth nova largura
     * @param newHeight nova altura
     * @return bitmap redimensionado
     */
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Metodo para enviar imagem em formato JSON (string base64)
     * @param path caminho da imagem
     */
    private void enviarImagemJsonBase64(String path){
        JSONObject jsonImagem = new JSONObject();
        try{
            jsonImagem.put("imagem", encodeImage(path));
        }catch(JSONException e){}
        try {
            URL url = new URL("https://servidor-imagem-android-alebarreto.c9.io/json.php");
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url.toURI());
            httpPost.setEntity(new StringEntity(jsonImagem.toString(), "UTF-8"));
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept-Encoding", "application/json");
            httpPost.setHeader("Accept-Language", "en-US");
            // Execute POST
            httpClient.execute(httpPost);
        }catch (ClientProtocolException e){}
        catch(IOException e){} catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
