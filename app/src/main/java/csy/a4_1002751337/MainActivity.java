package csy.a4_1002751337;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private HttpURLConnection urlConnection=null;

    private EditText urlinput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // bar = (ProgressDialog) findViewById(R.id.progressBar);
        Button buttonPopulate = (Button) findViewById(R.id.buttonpopulate);
        Button buttonView = (Button) findViewById(R.id.buttonview);
        Button buttonClear = (Button) findViewById(R.id.buttonclear);
        urlinput = (EditText) findViewById(R.id.editText);

        buttonPopulate.setOnClickListener(new ButtonClickListener());
        buttonView.setOnClickListener(new ButtonClickListener());
        buttonClear.setOnClickListener(new ButtonClickListener());

    }

    class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.buttonpopulate){
                startProgress();
            }
            if(v.getId() == R.id.buttonview){
                boolean have=false;
                DataBase database = new DataBase(MainActivity.this, "people.db", null, 1);
                SQLiteDatabase db = null;
                db = database.getReadableDatabase();
                String sql = "select COUNT(*) from person";
                Cursor cs = db.rawQuery(sql, null);
                if(cs != null){

                    cs.moveToFirst();

                    int count = cs.getInt(0);

                    if(count > 0){
                        have=true;
                    }

                    cs.close();
                }

                if (have){
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ViewActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
                }

            }
            if(v.getId() == R.id.buttonclear){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete")
                        .setMessage("Clear all people?")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DataBase database = new DataBase(MainActivity.this, "people.db", null, 1);
                                        SQLiteDatabase db = null;
                                        db = database.getReadableDatabase();
                                        deleteDatabase("people.db");
                                    }
                                }).setNegativeButton("Cancel", null).create()
                        .show();

            }
        }

    }

    public void startProgress() {

        progressDialog = new ProgressDialog(MainActivity.this);
        // 设置进度条风格，风格为长形
       // progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置ProgressDialog 标题
        progressDialog.setTitle("");
        // 设置ProgressDialog 提示信息
        progressDialog.setMessage("Populating, please wait");

        // 设置ProgressDialog 的进度条是否不明确
        progressDialog.setIndeterminate(true);
        // 设置ProgressDialog 是否可以按退回按键取消
        progressDialog.setCancelable(false);
        // 让ProgressDialog显示
        progressDialog.show();

        new Thread(new Task()).start();
        //progressDialog.cancel();

    }


    class Task implements Runnable {

        @Override

        public void run() {
                Looper.prepare();
                try {
                    String mainurl=urlinput.getText().toString();
                    String details = getUrlContents(mainurl);

                    savetxt(details);


                    List<String> imagename=new ArrayList<String>();
                    DataBase database=new DataBase(MainActivity.this,"people.db",null,1);
                    SQLiteDatabase db = null;
                    int i=1;
                    String introduction="";
                    String name="";
                    String img="";
                    Scanner scanner = new Scanner(new FileInputStream("/mnt/sdcard/csyA4/A4content.txt"));
                    while (scanner.hasNextLine()) {

                        if(i%3==0) {
                            img=scanner.nextLine().toString();
                            imagename.add(img);
                            db = database.getReadableDatabase();
                            Cursor cs=db.rawQuery("SELECT name FROM person WHERE name="+" '"+name+"'", null);
                            if(cs.getCount()==0) {
                                cs.close();
                                db.execSQL("INSERT INTO person VALUES (NULL,?,?,?)", new Object[]{name, introduction, img});
                            }
                            else
                            {
                                cs.close();
                            }
                            i=1;
                            continue;
                        }
                        else if(i%2==0)
                        {
                            introduction=scanner.nextLine().toString();
                            i++;
                            continue;
                        }else{
                            name=scanner.nextLine().toString();
                            i++;
                        }

                    }
                    Pattern p = Pattern.compile(".*/");
                    Matcher m = p.matcher(mainurl);
                    String imgpath="";
                    if(m.find()==true) imgpath=m.group(0).toString();


                    for(i=0;i<imagename.size();i++)
                    {
                        saveimg(getUrlimg(imgpath.toString() + imagename.get(i).toString()), imagename.get(i).toString() );


                    }



                    progressDialog.cancel();
                    Toast.makeText(MainActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();
                    Looper.loop();

                } catch (Exception e)
                {
                    e.printStackTrace();
                    progressDialog.cancel();
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    Looper.loop();

                } finally{
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }


                }

        }

    }

    private byte[] getUrlimg(String theUrl) throws Exception
    {
        ByteArrayOutputStream bufferb=null;

        try
        {
            // create a url object
            URL url = new URL(theUrl);
            HttpURLConnection imgcon;
            // create a urlconnection object
            imgcon = (HttpURLConnection)url.openConnection();
            imgcon.setConnectTimeout(10000);
            imgcon.setRequestMethod("GET");
            imgcon.setReadTimeout(10000);


            BufferedInputStream bis = new BufferedInputStream(imgcon.getInputStream());
            bufferb = new ByteArrayOutputStream();

            byte[] data = new byte[50];
            int current = 0;

            while ((current = bis.read(data, 0, data.length)) != -1) {
                bufferb.write(data, 0, current);
            }


            bis.close();
            imgcon.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error in save img", Toast.LENGTH_SHORT).show();
            throw e;
        }
        return bufferb.toByteArray();
    }

    private String getUrlContents(String theUrl) throws Exception
    {
        StringBuffer content = new StringBuffer();

        try
        {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }

            bufferedReader.close();
            urlConnection.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error in save text", Toast.LENGTH_SHORT).show();
            throw e;
        }
        return content.toString();
    }


    public void savetxt(String temple) throws IOException {
        File filedir = new File("/mnt/sdcard/csyA4");
        if (!filedir.exists())
            filedir.mkdir();
        File file = new File("/mnt/sdcard/csyA4/A4content.txt");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(temple.getBytes());
        fos.close();
    }
    public void saveimg(byte[] temple, String imgname) throws IOException {
        File filedir = new File("/mnt/sdcard/csyA4/pics");
        if (!filedir.exists())
            filedir.mkdir();
        File file = new File("/mnt/sdcard/csyA4/"+imgname);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(temple);
        fos.close();
    }


}
