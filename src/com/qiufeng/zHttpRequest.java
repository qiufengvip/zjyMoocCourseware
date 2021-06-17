package com.qiufeng;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * httprequest
 * 秋枫：2020.5.6
 */
class zHttpRequest {
    //    网址
    private String url;
    //    类型[POST,GET]
    private String mode;
    //    协议头
    private HashMap<String, String> headers;
    //    cookie
    private String cookies;
    //    提交数据
    private HashMap<String, String> submitdata;

    //    返回数据
    private String data;
//    Post(String url, String param, HashMap<String, String> headers, String cookie) {

    public zHttpRequest(String url, String mode) {
        /**
         * 只有url和访问类型
         */
        this.url = url;
        this.mode = mode;
        if (mode.equals("POST")) {
            this.Post(this.url, null, null, null);
        } else if (mode.equals("GET")) {
            this.sendGet(this.url, null, null, null);
        }

    }

    public zHttpRequest(String url, String mode, HashMap<String, String> submitdata) {
        /**
         * url
         * 访问类型
         * 提交数据
         */
        this.url = url;
        this.mode = mode;
        this.submitdata = submitdata;
        if (mode.equals("POST")) {
            this.Post(this.url, this.Processingdata(this.submitdata), null, null);
        } else if (mode.equals("GET")) {
            this.sendGet(this.url, this.Processingdata(this.submitdata), null, null);
        }
    }

    public zHttpRequest(String url, String mode, HashMap<String, String> headers, HashMap<String, String> submitdata) {

        /**
         * url
         * 提交类型
         * 协议头
         * 提交数据
         *
         */
        this.url = url;
        this.mode = mode;
        this.headers = headers;
        this.submitdata = submitdata;
        if (mode.equals("POST")) {
            this.Post(this.url, this.Processingdata(this.submitdata), this.headers, null);

        } else if (mode.equals("GET")) {
            this.sendGet(this.url, this.Processingdata(this.submitdata), this.headers, null);
        }
    }

    public zHttpRequest(String url, String mode, String cookies, HashMap<String, String> submitdata) {
        /**
         * url
         * 访问类型
         * cookie
         * 提交数据
         */
        this.url = url;
        this.mode = mode;
        this.cookies = cookies;
        this.submitdata = submitdata;
        if (mode.equals("POST")) {
            this.Post(this.url, this.Processingdata(this.submitdata), null, this.cookies);

        } else if (mode.equals("GET")) {
            this.sendGet(this.url, this.Processingdata(this.submitdata), null, this.cookies);
        }
    }

    public zHttpRequest(String url, String mode, HashMap<String, String> headers, String cookies, HashMap<String, String> submitdata) {

        /**
         * url
         * 访问类型
         * 协议头
         * cookie
         * 提交数据
         */
        this.url = url;
        this.mode = mode;
        this.headers = headers;
        this.cookies = cookies;
        this.submitdata = submitdata;
        if (mode.equals("POST")) {
            this.Post(this.url, this.Processingdata(this.submitdata), this.headers, this.cookies);

        } else if (mode.equals("GET")) {
            this.sendGet(this.url, this.Processingdata(this.submitdata), this.headers, this.cookies);
        }


    }

    private static String Processingdata(HashMap<String, String> submitdata) {

        String retu = "";

        for (Map.Entry<String, String> entry : submitdata.entrySet()) {
            retu = retu + "&" + entry.getKey() + "=" + entry.getValue();

        }
        return retu;
    }

    public String getCookies() {
        return this.cookies;
    }


    /**
     * 向指定URL发送GET方式的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数
     **/
    private void sendGet(String url, String param, HashMap<String, String> headers, String cookie) {
        StringBuilder result = new StringBuilder();
        String urlName = url + "?" + param;

        try {
            URL realUrl = new URL(urlName);


            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            //建立实际的连接
            conn.connect();
            //获取所有的响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            //遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "-->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //取cookie
            StringBuilder sessionId = new StringBuilder();
            String key = null;
            for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
                if (key.equalsIgnoreCase("set-cookie")) {
                    sessionId.append(conn.getHeaderField(i)).append(";");
                }
            }
            this.cookies = sessionId.toString();


            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            System.out.println("发送GET请求出现异常" + e);
            e.printStackTrace();
        }
        this.data = result.toString();
    }


    /**
     * 向指定URL发送POST方式的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数
     */
    private void Post(String url, String param, HashMap<String, String> headers, String cookie) {

        /**
         * 参数HashMap<String,String> headers说明：
         * HashMap<String,String> headers =new HashMap<String, String>();
         * headers.put("键", "键值");
         */
        System.out.println("=======POST=========");

        StringBuilder result = new StringBuilder();
        String line;
        try {
            URL realUrl = new URL(url);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    conn.addRequestProperty(k, v);
                }
            }



            if (cookie != null) {
                conn.setRequestProperty("cookie", cookie);

            }
            conn.setRequestProperty("charsert", "utf-8");

            //发送POST请求必须设置如下两行
//            conn.setDoOutput(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输出流
//            PrintWriter out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
//            out.print(param);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
            out.write(param);

            //flush输出流的缓冲
            out.flush();
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            //取cookie
            String sessionId = "";
            String cookieVal = "";
            String key = null;
            for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
                if (key.equalsIgnoreCase("set-cookie")) {
                    cookieVal = conn.getHeaderField(i);
                    sessionId = sessionId + cookieVal + ";";
                }
            }
            this.cookies = sessionId;


            while ((line = in.readLine()) != null) {
                result.append("\n").append(line);
            }


        } catch (Exception e) {
            System.out.println("发送POST请求出现异常" + e);
            e.printStackTrace();
        }

        this.data = result.toString();
//        System.out.println(this.data);

    }


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param submitdata 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, HashMap<String,String> submitdata) {

        String param = zHttpRequest.Processingdata(submitdata);


        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }









    public String getData() {
        return this.data;
    }



}


