package com.qiufeng;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;


/**
 * 职教云慕课类
 * 秋枫：2020.6.17
 * 版本：1.6
 *    修复刷课完成无提示(1.1)
 * 秋枫：2020.7.4   修复登录问题(1.2)
 *    修复登录失效(1.3)_7.29
 *    自动获取app最新版本  防止登录失效(1.4)      8.28
 * 秋枫：2020.10.1 更换参数 (1.5)
 *    业务逻辑改变 更安全
 * 秋枫：2020.11.2 增加图片功能(1.6)
 *
 * 待修复：登录过期问题
 */


public class zjyMoocCourses extends Thread{



    public String message="";

    private String cookie;
    private String courseOpenId;
    private String token;

    /**
     * 这里删掉
     */

    系统相关类  系统相关类= new 系统相关类();


    private String newToken;
    private String userId;

    public  void 初始化(String token, String courseOpenId){

        this.token =token;
        this.courseOpenId=courseOpenId;

    }

    @Override
    public void run() {
        if(getcookie()){
            系统相关类.发送广播("test",1,"登录协议获取成功");
        }else {
            系统相关类.发送广播("test",1,"登录失败,程序退出");
            return;
        }

        String info_unit = getunit();
        JSONObject json_unit = null;
        try {


            json_unit = new JSONObject(info_unit);
            System.out.println(json_unit);
            JSONObject json_unit_proces = json_unit.getJSONObject("proces");
            JSONArray json_unit_array =json_unit_proces.getJSONArray("moduleList");//每一个单元数组
            for (int i=0 ;i<json_unit_array.length();i++){
                getcookie();  //更新登录协议
                JSONObject json_unit_sing = json_unit_array.getJSONObject(i);
                System.out.println(json_unit_sing);
                String unit_name = json_unit_sing.getString("name");
                String unit_moduleId= json_unit_sing.getString("id");
                System.out.println("开始执行单元名称->"+unit_name);
                系统相关类.发送广播("test",1,"开始执行单元名称->"+unit_name);
                String info_section =this.getsection(unit_moduleId);
                JSONObject json_section = new JSONObject(info_section);
                System.out.println(json_section);
                JSONArray info_topicList = json_section.getJSONArray("topicList");
                for (int i1=0;i1<info_topicList.length();i1++){
                    JSONObject json_topicList = info_topicList.getJSONObject(i1);
                    System.out.println(json_topicList);
                    String detailed_name = json_topicList.getString("name");//每节的名称
                    String detailed_id = json_topicList.getString("id");//每节的id          topicId
                    System.out.println("开始执行小节名称->"+detailed_name);
                    系统相关类.发送广播("test",1,"开始执行小节名称->"+detailed_name);
                    String info_lesson = this.getlesson(detailed_id);
                    JSONObject json_lesson = new JSONObject(info_lesson);
                    System.out.println(json_lesson); //小节数组

                    JSONArray json_lesson_array  = json_lesson.getJSONArray("cellList"); //小节的数组
                    for (int i3=0;i3<json_lesson_array.length();i3++){
                        JSONObject json_lesson_sing= json_lesson_array.getJSONObject(i3);//每节课的json
                        System.out.println(json_lesson_sing);
                        String cellName = json_lesson_sing.getString("cellName");  //每节课的名称
                        String categoryName = json_lesson_sing.getString("categoryName"); //每节课的类型

                        boolean isStudyFinish = json_lesson_sing.getBoolean ("isStudyFinish"); //是否完毕
                        if (isStudyFinish){
                            System.out.println("[跳过]本节课已学习完毕..");
                            系统相关类.发送广播("test",1,"[跳过]本节课已学习完毕..");
                            continue;
                        }
                        String cellid = json_lesson_sing.getString("Id"); //每节课的id
                        System.out.println(cellName+"------"+categoryName);
                        系统相关类.发送广播("test",1,cellName);
                        System.out.println("--------");
                        switch (categoryName){
                            case "视频":
                                System.out.println("开始处理视频");
                                系统相关类.发送广播("test",1,"开始处理视频");
                                String info_detailed = this.getdetailed(cellid,this.courseOpenId,cellid,"stu",unit_moduleId);//视频的详细信息
                                JSONObject json_detailed = new JSONObject(info_detailed);
                                JSONObject json_detailed_info = json_detailed.getJSONObject("courseCell"); //这个是视频详细信息
                                int VideoTimeLong = json_detailed_info.getInt("VideoTimeLong");  //视频长度
                                this.videodispose(unit_moduleId,cellid,VideoTimeLong);
                                System.out.println(json_detailed);
                                //break;

                            case "ppt":
                            case "文档":
                            case "图片":
                            case "图文":
                                System.out.println("开始处理文档...");
                                系统相关类.发送广播("test",1,"开始处理文档...");


                                this.getpptdetailed(unit_moduleId,cellid);


                                this.pptdispose(unit_moduleId,cellid);
                            case "子节点":
                                JSONArray childNodeList = json_lesson_sing.getJSONArray("childNodeList"); //子节点的详细内容
                                for (int i4=0;i4<childNodeList.length();i4++){
                                    JSONObject json_childNodeList = childNodeList.getJSONObject(i4);
                                    boolean z_isStudyFinish = json_childNodeList.getBoolean("isStudyFinish"); //子节点每节课是否完毕
                                    String z_categoryName = json_childNodeList.getString("categoryName");//子节点每节课类型
                                    String z_cellid = json_childNodeList.getString("Id");//子节点每节课id
                                    System.out.println("正在处理子节点->"+z_categoryName);
                                    系统相关类.发送广播("test",1,"正在处理子节点->"+z_categoryName);
                                    if (z_isStudyFinish){
                                        System.out.println("[跳过]本节课已学习完毕..");
                                        系统相关类.发送广播("test",1,"[跳过]本节课已学习完毕..");
                                        continue;
                                    }

                                    switch (z_categoryName){
                                        case "视频":
                                        case "音频":
                                            System.out.println("开始处理子节点的视频..");
                                            系统相关类.发送广播("test",1,"开始处理子节点的视频..");
                                            String z_info_detailed  =this.getdetailed(z_cellid,this.courseOpenId,z_cellid,"stu",unit_moduleId);
                                            JSONObject json_z_detailed = new JSONObject(z_info_detailed);
                                            JSONObject json_z_detailed_info = json_z_detailed.getJSONObject("courseCell");
                                            int z_VideoTimeLong = json_z_detailed_info.getInt("VideoTimeLong");//长度
                                            this.videodispose(unit_moduleId,z_cellid,z_VideoTimeLong);  //执行刷课
                                            break;
                                        case "ppt":
                                        case "文档":
                                        case "图片":
                                        case "图文":
                                            System.out.println("开始处理字节点的文档流...");
                                            系统相关类.发送广播("test",1,"开始处理字节点的文档流...");
                                            this.getpptdetailed(unit_moduleId,z_cellid);
                                            this.pptdispose(unit_moduleId,z_cellid);
                                            break;
                                    }
                                }
                        }
                    }
                }
            }
            System.out.println("【完成】本课程已全部完成...");
            系统相关类.发送广播("test",1,"【完成】本课程已全部完成...");

        } catch (JSONException e) {
            e.printStackTrace();
            系统相关类.发送广播("test",1,"发送错误已退出请联系管理员");
        }
    }

    public String 获取信息() {
        return message;
    }



    private String getunit(){
        /**
         * 获取单元信息
         * courseOpenId
         */
        String url ="https://mooc.icve.com.cn/study/learn/getProcessList";
        zHttpRequest unitdata = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseOpenId", this.courseOpenId);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        try {
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
            return unitdata.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return "获取单元信息异常";
        }

    }

    //获取章节的信息
    private String getsection(String moduleId){
        /**
         * 获取章节的信息
         * :param moduleId: 单元ID
         */
        String url ="https://mooc.icve.com.cn/study/learn/getTopicByModuleId";
        zHttpRequest unitdata = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseOpenId", this.courseOpenId);
        data.put("moduleId", moduleId);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        try {
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取章节的信息数据异常..";
        }
        return unitdata.getData();

    }

    //获取到每节课的数据
    private String getlesson(String topicId){
        /**
         * 获取到每节课的数据
         * :param topicId: 章节ID
         */
        String url ="https://mooc.icve.com.cn/study/learn/getCellByTopicId";
        zHttpRequest unitdata = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseOpenId", this.courseOpenId);
        data.put("topicId", topicId);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        try {
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取到每节课的数据异常..";
        }
        return unitdata.getData();

    }

    //获取每节课的详细数据
    private String getdetailed(String cellIdHash,String page,String  cellId,String fromType,String moduleId){
        /**
         * 获取每节课的详细数据
         * :param topicId: 章节ID
         */
        String url ="https://mooc.icve.com.cn/study/learn/getModulsSliderList";
        zHttpRequest unitdata = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("cellIdHash", cellIdHash);
        data.put("page", page);
        data.put("courseOpenId",this.courseOpenId);
        data.put("cellId", cellId);
        data.put("fromType", fromType);
        data.put("moduleId", moduleId);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        try {
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取每节课的详细数据异常..";
        }
        return unitdata.getData();
    }

    //处理视频
    private void videodispose(String moduleId,String cellId,int videoTimeTotalLong){
        /**
         *处理视频
         *
         *
         *
         */
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");
        Random r = new Random();
        for (int i = 0; i<videoTimeTotalLong; i+=20){
            HashMap<String, String> data = new HashMap<String, String>();
            data.put("courseId", "");
            data.put("courseOpenId", this.courseOpenId);
            data.put("moduleId",moduleId);
            data.put("cellId",cellId);
            data.put("auvideoLength",i+"");
            data.put("videoTimeTotalLong",videoTimeTotalLong+"");
            data.put("sourceForm","993");
            zHttpRequest getunitdata=null;

            try {
                Thread.sleep(2000);
                getunitdata = new zHttpRequest("https://mooc.icve.com.cn/study/learn/statStuProcessCellLogAndTimeLong", "POST",headers,this.cookie, data);
                this.message =this.message + "\n"+getunitdata.getData();
                System.out.println(getunitdata.getData());
                系统相关类.发送广播("test",1,getunitdata.getData());
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("提交视频进度异常 (: ...");
                this.message =this.message + "\n提交视频进度异常 :) ...";
                系统相关类.发送广播("test",1,"提交视频进度异常 :) ...");
            }


        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseId", "");
        data.put("courseOpenId", this.courseOpenId);
        data.put("moduleId",moduleId);
        data.put("cellId",cellId);
        data.put("auvideoLength",videoTimeTotalLong+"");
        data.put("videoTimeTotalLong",videoTimeTotalLong+"");
        data.put("sourceForm","993");
        zHttpRequest getunitdata=null;

        try {
            getunitdata = new zHttpRequest("https://mooc.icve.com.cn/study/learn/statStuProcessCellLogAndTimeLong", "POST",headers,this.cookie, data);
//            return getunitdata.getData();
            this.message+="\n"+getunitdata.getData();
            System.out.println(getunitdata.getData());
            this.message+="\n视频处理完成..";
            系统相关类.发送广播("test",1,"视频处理完成..");
            System.out.println("视频处理完成..");
        } catch (Exception e) {
            e.printStackTrace();
            this.message+="\n提交视频完结异常...";
            System.out.println("提交视频完结异常...");
            系统相关类.发送广播("test",1,"提交视频完结异常...");
//            return "提交视频完结异常...";
        }





    }



    /**
     * @desc 更新职教云的cookie
     */
    public boolean getcookie(){
        String url = "http://api.qiufengvip.top/newapi/Login";

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("token", this.token);

        zHttpRequest getdata=null;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");
        try {
            系统相关类.发送广播("test",1,"开始更新职教云cookie");
            getdata = new zHttpRequest(url, "POST",headers,this.cookie, data);

            String yuandata = getdata.getData();


//            String as = "{\"code\":\"200\",\"msg\":\"\\u9a8c\\u8bc1\\u6210\\u529f\",\"user\":\"196307575\",\"userid\":\"vmttaw2lbpbbtccgjxhra\",\"cookie\":\"acw_tc=2f624a1d16047350678751595e313c8fd2ad964d0c4183d2d640bfb3f3a704;auth=0102D94E84F4F082D808FED95E30C64483D808011576006D00740074006100770032006C00620070006200620074006300630067006A00780068007200610000012F00FFC443CD8E447C9E6699D4793DF75BD0E60CF167B9\",\"newtoken\":\"@b1ac2e8193a04a1caee1d320a6a85e1a\"}\n";

            String code =getSubString(yuandata,"\"code\":\"","\"");
            String cookie = getSubString(yuandata,"\"cookie\":\"","\"");
            String newToken = getSubString(yuandata,"\"newtoken\":\"","\"");
            String userId = getSubString(yuandata,"\"userid\":\"","\"");



            if (code.equals("200")){
                this.cookie = cookie;
                this.newToken = newToken;
                this.userId =userId;
                系统相关类.发送广播("test",1,"更新职教云cookie成功");
                return true;
            }else {
                系统相关类.发送广播("test",1,"职教管家账号异地登录，长时间不更新cookie会导致刷课程序退出");
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            系统相关类.发送广播("test",1,"更新cookie未能成功");
            return false;
        }
    }


    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right 后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }










    //处理ppt作用域
    private void getpptdetailed(String moduleId,String cellId){
        /**
         * 处理ppt作用域
         *
         */
        String url = "https://mooc.icve.com.cn/study/learn/viewDirectory";
        String url2="https://mooc.icve.com.cn/common/localStorage/getUserInfo";
        String url3="https://mooc.icve.com.cn/study/learn/getCellCommentData";
        String url4="https://mooc.icve.com.cn/study/learn/statStuProcessCellLogAndTimeLong";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseOpenId",this.courseOpenId);
        data.put("cellId", cellId);
        data.put("fromType", "stu");
        data.put("moduleId", moduleId);

        HashMap<String, String> data3 = new HashMap<String, String>();
        data3.put("courseOpenId",this.courseOpenId);
        data3.put("cellId", cellId);
        data3.put("dType", "2");

        HashMap<String, String> data4 = new HashMap<String, String>();
        data4.put("courseId", "");
        data4.put("courseOpenId",this.courseOpenId);
        data4.put("moduleId", moduleId);
        data4.put("cellId", cellId);
        data4.put("videoTimeTotalLong", "0");
        data4.put("sourceForm", "1030");


        try {
            new zHttpRequest(url, "POST",headers,this.cookie, data);
            new zHttpRequest(url2, "POST",headers,this.cookie, data);
            new zHttpRequest(url3, "POST",headers,this.cookie, data3);
            new zHttpRequest(url4, "POST",headers,this.cookie, data4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //处理ppt文档
    private void pptdispose(String moduleId,String cellId){
        /**
         * 处理ppt文档
         *
         */

        zHttpRequest unitdata = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseId", "");
        data.put("courseOpenId",this.courseOpenId);
        data.put("moduleId", moduleId);
        data.put("cellId", cellId);
        data.put("auvideoLength", "9");

                                                        //statStuProcessCellLogAndTimeLong
        String url="https://mooc.icve.com.cn/study/learn/computatlearningTimeLong";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        HashMap<String, String> data1 = new HashMap<String, String>();
        data1.put("courseId", "");
        data1.put("courseOpenId",this.courseOpenId);
        data1.put("moduleId", moduleId);
        data1.put("cellId", cellId);
        data1.put("auvideoLength", "10");


        try {
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
            系统相关类.发送广播("test",1,unitdata.getData());
            Thread.sleep(5000);
            unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data1);
            系统相关类.发送广播("test",1,unitdata.getData());
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}

class 系统相关类 {
    public static void 发送广播(String name, int aaaa, String info){
        System.out.println(info);

    }
}

