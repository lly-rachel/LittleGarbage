package com.example.littlegarbage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import android.util.Base64;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.littlegarbage.db.DBManeger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import java.util.List;



public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    AutoCompleteTextView seachnameATV;

    /*显示联想词*/
    GridView hot_historyGv;
    private ArrayAdapter<String> arrayAdapter;
    Handler hd;
    String Imagename;
    String imageUrl;

    /*拍照用*/
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    String imgBase;

    /*获取相册图片用*/
    public static final int CHOOSE_PHOTO = 2;

    ImageView seachIv,soundIv,photoIv,takepictureIv;
    ListView historyLv;
    String garbage;
    SearchHistoryAdapter historyAdapter;
    List<String> garbagenameList;

    final static String hotSearchHistoryURL = "https://api.tianapi.com/txapi/hotlajifenlei/index?key=2fb9da721d164cdc0a45b990545796fa";
    final static String imageNameURL = "https://api.zhetaoke.com:10001/api/api_suggest.ashx?appkey=3982f6785fcd4b54a214c69f4c167477";

    final static List<String> newdata= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        iniDetail();

        hot_historyGv=findViewById(R.id.hot_history_Gridview);


       hd = new Handler();

        // 启用网络线程
        HttpThreadToGetData ht = new HttpThreadToGetData();
        ht.start();


        historyLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                String garbage = (String) historyAdapter.getItem(index);
                getTheGarbageMessage(garbage);
            }
        });


        iniEdt();

    }


    public class HttpThreadToGetData extends Thread{

        @Override
        public void run() {
            super.run();
            try {
                String data = GetHttpData.GetHotData(hotSearchHistoryURL);
                final String finalWi = data;
                // 多线程更新 UI
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        setData(finalWi);
                    }
                });


            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*解析获取的垃圾分类最多搜索记录的json数据*/
    public void setData(String data)  {

        if(data!=null){
            JSONObject joname = null;
            try {
                joname = new JSONObject(data);
                if(joname.getInt("code")==200){
                    JSONArray listArray = joname.getJSONArray("newslist");
                    for(int i = 0;i<listArray.length();i++){
                        JSONObject  jsonArray= listArray.getJSONObject(i);
                        String name =jsonArray.getString("name");
                        Integer type = jsonArray.getInt("type");
                        Integer index = jsonArray.getInt("index");
                        if(index>100&&name.length()<5&&newdata.size()<16&&!newdata.contains(name)){
                            newdata.add(name);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }


        arrayAdapter = new ArrayAdapter<>(this,R.layout.item_hotgarbage,newdata);
        hot_historyGv.setAdapter(arrayAdapter);
        setListener();
    }

    private void setListener() {

        hot_historyGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                garbage = newdata.get(position);
                getTheGarbageMessage(garbage);
            }
        });


    }

    /*根据传进garbage到展示界面*/
    private void getTheGarbageMessage(String garbage) {

        Intent intent = new Intent(this,ShowGarbageDetailActivity.class);
        intent.putExtra("garbage",garbage);
        startActivity(intent);

    }



    public  void iniEdt() {

        seachnameATV = findViewById(R.id.garbage_search_autoCompelete);
        seachnameATV.setThreshold(1);


        /*获取输入框监听  联想词可操作*/
        seachnameATV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String name=seachnameATV.getText().toString();
         //       String name = String.valueOf(s);
                try {
                    Imagename = java.net.URLEncoder.encode(name,"UTF-8");
                    GetImageData(Imagename);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }



            }

        });


    }

    /*根据输入的文本 传入content参数*/
    private void GetImageData(String name) {

        imageUrl  = imageNameURL+"&content="+name;
        //网络请求
        HttpThreadToGetImageData httpThreadToGetImageData = new HttpThreadToGetImageData();
        httpThreadToGetImageData.start();

    }

    /*获取联想词数据*/
    public class HttpThreadToGetImageData extends Thread{

        @Override
        public void run() {
            super.run();
            try {
                String imageData = GetHttpData.GetHotData(imageUrl);
                final String finalImageData =imageData;
                // 多线程更新 UI
                hd.post(new Runnable() {
                    @Override
                    public void run() {
                        setImageData(finalImageData);
                    }
                });


            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /*解析联想词数据*/
    private void setImageData(String finals) {

        List<String> ImageNameList = new ArrayList<>();
        if(finals!=null){
            JSONObject joname = null;
            try {
                joname = new JSONObject(finals);

                JSONArray listArray = joname.getJSONArray("result");
                for(int i = 0;i<listArray.length();i++){
                    JSONArray NameArray = listArray.getJSONArray(i);
                    String name = NameArray.getString(0);
                    ImageNameList.add(name);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayAdapter<String> atvArrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line,ImageNameList);

            seachnameATV.setAdapter(atvArrayAdapter);
            atvArrayAdapter.notifyDataSetChanged();

        }

    }

    private void iniDetail()  {

        seachIv = findViewById(R.id.garbage_search);
        soundIv = findViewById(R.id.search_sound);
        photoIv = findViewById(R.id.search_photo);
        takepictureIv = findViewById(R.id.search_takepicture);
        historyLv = findViewById(R.id.search_history);
        historyLv.setAdapter(historyAdapter);


        seachIv.setOnClickListener(this);
        soundIv.setOnClickListener(this);
        photoIv.setOnClickListener(this);
        takepictureIv.setOnClickListener(this);

        garbagenameList = DBManeger.queryAllGarbageName();


        historyAdapter = new SearchHistoryAdapter(this,garbagenameList);
        historyLv.setAdapter(historyAdapter);


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.garbage_search:

                garbage = seachnameATV.getText().toString();

                if(!TextUtils.isEmpty(garbage)){

                    getTheGarbageMessage(garbage);

                }else{
                    Toast.makeText(this,"输入信息不能为空",Toast.LENGTH_LONG).show();
                }

                break;

                /*拍照*/
            case R.id.search_takepicture:

                startTakePicture();

                break;

                /*获取相册图片*/
            case R.id.search_photo:

                if(ContextCompat.checkSelfPermission
                        (this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
                break;

            case R.id.search_sound:

                break;

        }
    }

    private void openAlbum() {

        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);//打开相册

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this,"您拒绝了相册访问",Toast.LENGTH_LONG).show();
                }
                break;

            default:break;
        }
    }

    private void startTakePicture() {

        /*创建File对象，存储拍照后的图片*/
        File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
        if(outputImage.exists()){
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT>=24){
            imageUri = FileProvider.getUriForFile
                    (this,"com.example.littlegarbage.fileprovider",outputImage);
        }else{
            imageUri = Uri.fromFile(outputImage);
        }
        /*启动相机程序*/
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(imageUri));
 //                       Bitmap bitmapCompress = compressImage(bitmap);
                        String imgbase = bitmaptoString(bitmap);

                        getThePictureName(imgbase);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    //判断手机系统版本号
                    if(Build.VERSION.SDK_INT>=19){
                        //4.4及以上系统
                        handleImageOnKitKat(data);
                    }else{
                        //4.4以下系统
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath=null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse(
                        "content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }else if("content".equalsIgnoreCase(uri.getScheme())){
                //如果是content类型的uri，则使用普通方法处理
                imagePath=getImagePath(uri,null);
            }else if("file".equalsIgnoreCase(uri.getScheme())){
                //如果是file类型的uri，直接获取图片路径即可
                imagePath=uri.getPath();
            }
            displayImage(imagePath);
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片的路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if(imagePath!=null){
            Bitmap bitmap =BitmapFactory.decodeFile(imagePath);
//            Bitmap bitmapCompress = compressImage(bitmap);
            String imgbase = bitmaptoString(bitmap);

            getThePictureName(imgbase);

        }else{
            Toast.makeText(this,"获取照片失败",Toast.LENGTH_LONG).show();
        }
    }

    private void getThePictureName(String imgbase) {

        imgBase = imgbase;
        // 启用网络线程
        HttpThreadToGetPictureName ht = new HttpThreadToGetPictureName();
        ht.start();

    }

    public class HttpThreadToGetPictureName extends Thread{

        @Override
        public void run() {
            super.run();

            GarbageBean garbageBean = null;
            JsonParser jp = new JsonParser();

            // 城市代码
            String garbageString = null;
            try {

                garbageString = HttpUtil.sendOkHttpRequest(garbage);

            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }

            if (garbageString == null) {
                garbageString="数据获取错误";

            }else{
                //获取数据成功

                // 调用自定义的 JSON 解析类解析获取的 JSON 数据
                garbageBean = jp.GarbageParse(garbageString);

                final String garbageName = garbageBean.getResult().garbage_info.get(0).getGarbage_name();

                // 多线程更新 UI
                hd.post(new Runnable() {
                    @Override
                    public void run() {

                        getTheGarbageMessage(garbageName);
                    }
                });

            }




        }
    }



//    /**
//     * 压缩图片
//     * @param image
//     * @return
//     */
//    public static Bitmap compressImage(Bitmap image) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//        int options = 100;
//        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
//            baos.reset();//重置baos即清空baos
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
//            options -= 10;//每次都减少10
//        }
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
//        return bitmap;
//    }

    /*将图像进行Base64编码*/
    public String bitmaptoString(Bitmap bitmap) {

        // 将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }
}
