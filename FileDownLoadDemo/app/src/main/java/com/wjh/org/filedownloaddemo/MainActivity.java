package com.wjh.org.filedownloaddemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * android 多线程下载,断点续传
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    EditText et_downloadpath;
    TextView tv_result;
    static ProgressBar pb0, pb1, pb2;

    // 定义三个线程
    static int THREAD_COUNT = 3;
    // 定义路径为全局路径
    static String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 查找控件的id
        et_downloadpath = (EditText) findViewById(R.id.et_downloadpath);
        pb0 = (ProgressBar) findViewById(R.id.pb0);
        pb1 = (ProgressBar) findViewById(R.id.pb1);
        pb2 = (ProgressBar) findViewById(R.id.pb2);


    }

    // 2. 下载按钮的方法,封装了下载的逻辑代码
    public void download(View v) {

        // 2.1 获取输入框的文本路径,并且判断是否为空
        path = et_downloadpath.getText().toString();
        if (TextUtils.isEmpty(path)) {
            // 进来则表示为空,弹出吐司提示用户
            Toast.makeText(this, "下载路径不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2.2 请求提交服务器连接的逻辑处理,需要在子线程中执行
        new Thread() {
            public void run() {

                // 2.3 当客户端点击下载按钮的时候,执行这个(封装好数据)的线程
                startDownload();
            };
        }.start();
    }

    /**
     * // 3. 封装线程下载和断点续传的代码为一个方法,方便调用
     */
    public void startDownload() {

        try {
            // 3.1 指定资源定位符
            URL url = new URL(path);

            // 3.2 打开连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 3.3 设定提交请求的方式
            conn.setRequestMethod("GET");

            // 3.4 判断服务器响应的状态码
            if (conn.getResponseCode() == 200) {

                // 3.4.1 进来则说明请求成功了,获取要下载的文件的长度和大小
                int fileLength = conn.getContentLength();
                Log.d(TAG, "要下载的文件总大小:" + fileLength);

                // 3.4.2 在本地生成一个与服务器中要下载的,一样大小的文件
                RandomAccessFile fileName = new RandomAccessFile(
                        getFilePath(path), "rw");
                fileName.setLength(fileLength);

                // 3.4.3 计算出每个线程平均要下载文件的长度是多少(average:平均)
                int average = fileLength / THREAD_COUNT;

                // 3.4.4 循环计算出每个线程下载的区间是从什么位置开始和结束位置
                // 每个线程进来的时候,乘以平均值,并且后面的线程进来,累加一次
                for (int threadID = 0; threadID < THREAD_COUNT; threadID++) {

                    int startIndex = threadID * average; // 每个线程循环一次开始的位置
                    int endIndex = (threadID + 1) * average; // 每个线程循环一次结束的位置

                    // 3.4.5 然后判断一下,如果是最后一个线程进来,那么就表示是文件长度的末尾了
                    if (threadID == THREAD_COUNT - 1) {
                        // 最后一个的话,就把文件最后的不能等分的长度全部交由最后一个线程去下载
                        endIndex = fileLength - 1;
                    }
                    // 3.4.6 弹出log,显示每个线程下载的区间
                    Log.d(TAG, "第" + threadID + "个线程,下载的区间是" + startIndex
                            + "----" + endIndex);

                    // 3.5 启动线程,每一次循环执行一个线程
                    new myDownload(startIndex, endIndex, threadID).start();
                }
                fileName.close();// 释放资源

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 4. 线程下载和断点续传的 核心逻辑代码
     */
    static class myDownload extends Thread {

        // 4.1 构建3个成员变量,和一个断点续传记录的变量
        int startIndex; // 线程开始的位置
        int endIndex; // 线程结束的位置
        int threadID; // 线程的id
        int position; // 记录线程开始的位置

        public myDownload(int startIndex, int endIndex, int threadID) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.threadID = threadID;
            position = startIndex; // 记录线程开始下载的位置
        }

        @Override
        public void run() {

            try {
                // 4.9 在每个线程进来的时候,就判断一下上次下载的缓存文件是否存在,存在就进来获取
                File cacheFileName = new File(Environment
                        .getExternalStorageDirectory().getAbsolutePath()+"/"
                        + threadID + ".position");
                Log.d(TAG, "文件路径是:" + cacheFileName);

                if (cacheFileName.exists()) {
                    // 4.9.1 进来则说明之前已经下载过该文件了,进行读取出来,
                    BufferedReader br = new BufferedReader(new FileReader(
                            cacheFileName));

                    // 4.9.2 把上次下载记录的位置,重新赋值给这个记录下载位置的变量,让该线程从上次那个位置开始是继续下载
                    position = Integer.parseInt(br.readLine());
                    // 释放资源
                    br.close();
                    Log.d(TAG, "之前已经下载过了,可以继续上次的下载!");

                } else {
                    // 4.9.3 否则就是没有下载过该文件,继续执行下面的代码
                    Log.d(TAG, "之前没下载过");

                }

                // 4.2 指定资源定位符
                URL url = new URL(path);

                // 4.3 打开连接
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();

                // 4.4 设置提交数据到服务器的请求方式
                conn.setRequestMethod("GET");

                // 4.5 注意:需要告诉服务器每个线程从什么位置开始下载和结束的位置
                conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
                        + endIndex); // 每个线程要下载的区间

                // 4.6 判断一下服务器响应的状态码
                Log.d(TAG, "服务器响应的状态码是:" + conn.getResponseCode());
                if (conn.getResponseCode() == 206) {

                    // 4.6.1 进来这里,则说明请求提交成功,获取服务器响应的数据
                    InputStream is = conn.getInputStream();

                    // 4.6.2 指定文件输出的位置(要保存的位置)
                    RandomAccessFile raf = new RandomAccessFile(
                            getFilePath(path), "rw");

                    // 4.6.3 要告诉每个线程下载回来的时候从什么位置开始写入数据
                    raf.seek(position); // 从记录位置开始请求,就从什么位置开始写入

                    int len = 0;
                    byte[] bys = new byte[1024];
                    while ((len = is.read(bys)) != -1) {
                        raf.write(bys, 0, len); // 读取,再写入

                        // 4.6.4 每次循环累加,记录下载的位置
                        position += len;
                        Log.d(TAG, "第" + threadID + "个线程,已下载了:" + position);


                        /**
                         * TODO:在这里需要设置一个进度条
                         */
                        if(threadID==0){
                            //设置进度条的最大值
                            pb0.setMax(endIndex-startIndex);
                            //设置进度条的累加状态(进度)
                            pb0.setProgress(position-startIndex);
                        }else if(threadID==1){
                            //设置进度条的最大值
                            pb1.setMax(endIndex-startIndex);
                            //设置进度条的累加状态(进度)
                            pb1.setProgress(position-startIndex);
                        }else if(threadID==2){
                            //设置进度条的最大值
                            pb2.setMax(endIndex-startIndex);
                            //设置进度条的累加状态(进度)
                            pb2.setProgress(position-startIndex);
                        }



                        // 4.6.5 把每次记录的下载位置,都以指定文件格式的,保存成一个缓存文件
                        RandomAccessFile file = new RandomAccessFile(
                                Environment.getExternalStorageDirectory()
                                        .getAbsolutePath()+"/"
                                        + threadID
                                        + ".position", "rw");
                        file.write((position+"").getBytes()); // 把这个记录了下载的变量数据,写入文件
                        file.close(); // 释放资源






                    }
                    // 4.7 走到这里,则说明文件已经下载完成了,释放资源
                    raf.close();
                    is.close();
                    Log.d(TAG, "第" + threadID + "个线程,已经下载完毕了");

                    // 4.8 当每个线程的长度都下载完毕的时候,把这个缓存文件进行删除
                    File cacheFile = new File(Environment
                            .getExternalStorageDirectory().getAbsolutePath()+"/"
                            + threadID + ".position");
                    if (cacheFile.exists()) {
                        cacheFile.delete(); // 进行删除
                    }

                }

            } catch (Exception e) {

                Log.d(TAG, "文件下载失败了,请重新下载");

                e.printStackTrace();
            }
        }
    }

    /**
     * 5. 把下载的文件保存在SD卡中,因为不能保存在系统根目录下
     */
    @Test
    static String getFilePath(String path) {
        // static String path = "http://10.0.2.2:8080/imgNews/b.jpg";
        // Environment.getExternalStorageDirectory().getAbsolutePath();

        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + path.substring(path.lastIndexOf("/") + 1);

        // Log.d(TAG,
        // Environment.getExternalStorageDirectory().getAbsolutePath()
        // + "/" + path.substring(path.lastIndexOf("/")));
    }

}