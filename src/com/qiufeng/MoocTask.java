
package com.qiufeng;//包名必须固定为这个，不能自己修改


//import com.e4a.runtime.annotations.SimpleObject;
//import com.e4a.runtime.annotations.SimpleFunction;
//import com.e4a.runtime.应用操作;//可以引用E4A支持库中已经存在的类
//import com.e4a.runtime.系统相关类;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


/**
 * 职教云慕课类
 * 秋枫：2020.6.17
 * 版本：1.7
 *    修复刷课完成无提示(1.1)
 * 秋枫：2020.7.4   修复登录问题(1.2)
 *    修复登录失效(1.3)_7.29
 *    自动获取app最新版本  防止登录失效(1.4)      8.28
 * 秋枫：2020.10.1 更换参数 (1.5)
 *    业务逻辑改变 更安全
 * 秋枫：2020.11.2 增加图片功能(1.6)
 *
 * 秋枫：2020.12.5 增加自动考试自动做作业  优化程序稳定性
 *
 *
 */


public class MoocTask extends Thread{



    public String message="";

    private String cookie;
    private String courseOpenId;
    private String token;

    private boolean isWork;  //作业
    //测验
    private boolean isTestst;
    //考试
    private boolean isExamst;

    private boolean isclass;




    private String newToken;
    private String userId;


    public  void init(String token, String courseOpenId,boolean isWork,boolean isTestst,boolean isExamst,boolean isclass){

        this.token = token;
        this.courseOpenId=courseOpenId;
        this.isWork = isWork;
        this.isExamst = isExamst;
        this.isTestst  = isTestst;
        this.isclass  = isclass;

    }

    @Override
    public void run() {

        try {
            new MoocWork(courseOpenId,token,isWork,isTestst,isExamst).Run();  //执行刷课的东西
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(getcookie()){
            系统相关类.发送广播("test",1,"登录协议获取成功");
        }else {
            系统相关类.发送广播("test",1,"登录失败,程序退出");
            return;
        }


        JSONObject json_unit = null;
        if (!isclass){
            系统相关类.发送广播("test",1,"【完成】已选任务执行完毕");
            return;
        }
        try {

            json_unit = getunit();
            System.out.println(json_unit);

            JSONObject json_unit_proces = null;
            try {
                json_unit_proces = json_unit.getJSONObject("proces");

            } catch (JSONException e) {
                e.printStackTrace();

                json_unit = getunit();
                json_unit_proces = json_unit.getJSONObject("proces");

            }


            JSONArray json_unit_array =json_unit_proces.getJSONArray("moduleList");//每一个单元数组
            for (int i=0 ;i<json_unit_array.length();i++){
                getcookie();  //更新登录协议
                JSONObject json_unit_sing = json_unit_array.getJSONObject(i);
                System.out.println(json_unit_sing);
                String unit_name = json_unit_sing.getString("name");
                String unit_moduleId= json_unit_sing.getString("id");
                System.out.println("开始执行单元名称->"+unit_name);
                系统相关类.发送广播("test",1,"开始执行单元名称->"+unit_name);

                JSONObject json_section = this.getsection(unit_moduleId);
                System.out.println(json_section);
                JSONArray info_topicList = json_section.getJSONArray("topicList");
                for (int i1=0;i1<info_topicList.length();i1++){

                    try {
                        JSONObject json_topicList = info_topicList.getJSONObject(i1);
                        System.out.println(json_topicList);
                        String detailed_name = json_topicList.getString("name");//每节的名称
                        String detailed_id = json_topicList.getString("id");//每节的id          topicId
                        System.out.println("开始执行小节名称->" + detailed_name);
                        系统相关类.发送广播("test", 1, "开始执行小节名称->" + detailed_name);

                        JSONObject json_lesson = this.getlesson(detailed_id);
                        System.out.println(json_lesson); //小节数组

                        JSONArray json_lesson_array = json_lesson.getJSONArray("cellList"); //小节的数组
                        for (int i3 = 0; i3 < json_lesson_array.length(); i3++) {
                            JSONObject json_lesson_sing = json_lesson_array.getJSONObject(i3);//每节课的json

                            System.out.println(json_lesson_sing);
                            String cellName = json_lesson_sing.getString("cellName");  //每节课的名称
                            String categoryName = json_lesson_sing.getString("categoryName"); //每节课的类型

                            boolean isStudyFinish = json_lesson_sing.getBoolean("isStudyFinish"); //是否完毕
                            if (isStudyFinish) {
                                System.out.println("[跳过]本节课已学习完毕..");
                                系统相关类.发送广播("test", 1, "[跳过]本节课已学习完毕..");
                                continue;
                            }
                            String cellid = json_lesson_sing.getString("Id"); //每节课的id
                            System.out.println(cellName + "------" + categoryName);
                            系统相关类.发送广播("test", 1, cellName);
                            System.out.println("--------");

                            int cellType = json_lesson_sing.getInt("cellType"); //每节课类型  8  =讨论
                            if (cellType == 8) {
                                //判断是讨论的话 直接刷讨论 -> 退出
                                this.TopicAllReply(cellid);
                                continue;
                            }


                            switch (categoryName) {
                                case "视频":
                                case "音频":

                                    System.out.println("开始处理视频");
                                    系统相关类.发送广播("test", 1, "开始处理视频");
//                                String info_detailed = this.getdetailed(cellid,this.courseOpenId,cellid,"stu",unit_moduleId);//视频的详细信息
                                    System.out.println("视频信息");
//                                System.out.println(info_detailed);

                                    JSONObject json_detailed = this.getdetailed(cellid, this.courseOpenId, cellid, "stu", unit_moduleId);//视频的详细信息
                                    JSONObject json_detailed_info = json_detailed.getJSONObject("courseCell"); //这个是视频详细信息
                                    int VideoTimeLong = json_detailed_info.getInt("VideoTimeLong");  //视频长度
                                    this.videodispose(unit_moduleId, cellid, VideoTimeLong);
                                    System.out.println(json_detailed);
                                    //break;

                                case "ppt":
                                case "文档":
                                case "图片":

                                    JSONObject info_ppt_detailed = this.getdetailed(cellid, this.courseOpenId, cellid, "stu", unit_moduleId);//视频的详细信息

                                    System.out.println(info_ppt_detailed);

                                    System.out.println("开始处理文档...");
                                    系统相关类.发送广播("test", 1, "开始处理文档...");
                                    this.getpptdetailed(unit_moduleId, cellid);
                                    this.pptdispose(unit_moduleId, cellid);
                                case "子节点":
                                    JSONArray childNodeList = json_lesson_sing.getJSONArray("childNodeList"); //子节点的详细内容
                                    for (int i4 = 0; i4 < childNodeList.length(); i4++) {
                                        JSONObject json_childNodeList = childNodeList.getJSONObject(i4);
                                        boolean z_isStudyFinish = json_childNodeList.getBoolean("isStudyFinish"); //子节点每节课是否完毕
                                        String z_categoryName = json_childNodeList.getString("categoryName");//子节点每节课类型
                                        String z_cellid = json_childNodeList.getString("Id");//子节点每节课id
                                        System.out.println("正在处理子节点->" + z_categoryName);
                                        系统相关类.发送广播("test", 1, "正在处理子节点->" + z_categoryName);
                                        if (z_isStudyFinish) {
                                            System.out.println("[跳过]本节课已学习完毕..");
                                            系统相关类.发送广播("test", 1, "[跳过]本节课已学习完毕..");
                                            continue;
                                        }

                                        int icellType = json_childNodeList.getInt("cellType"); //每节课类型  8  =讨论
                                        if (icellType == 8) {
                                            //判断是讨论的话 直接刷讨论 -> 退出
                                            this.TopicAllReply(z_cellid);
                                            continue;
                                        }

                                        switch (z_categoryName) {
                                            case "视频":
                                            case "音频":
                                                System.out.println("开始处理子节点的视频..");
                                                系统相关类.发送广播("test", 1, "开始处理子节点的视频..");
                                                JSONObject json_z_detailed = this.getdetailed(z_cellid, this.courseOpenId, z_cellid, "stu", unit_moduleId);
                                                ;
                                                JSONObject json_z_detailed_info = json_z_detailed.getJSONObject("courseCell");
                                                int z_VideoTimeLong = json_z_detailed_info.getInt("VideoTimeLong");//长度
                                                this.videodispose(unit_moduleId, z_cellid, z_VideoTimeLong);  //执行刷课
                                                break;
                                            case "ppt":
                                            case "文档":
                                            case "图片":
                                                System.out.println("开始处理字节点的文档流...");
                                                系统相关类.发送广播("test", 1, "开始处理字节点的文档流...");
                                                this.getpptdetailed(unit_moduleId, z_cellid);
                                                this.pptdispose(unit_moduleId, z_cellid);
                                                break;
                                        }
                                    }
                            }
                        }
                    }catch (Exception e){
                        System.out.println("本节课处理异常已经跳过...");
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




    private JSONObject getunit(){
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

        while (true){
            try {

                系统相关类.发送广播("test",1,"正在获取单元信息");
                unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
                JSONObject ret = new JSONObject(unitdata.getData());
                return ret;
            } catch (Exception e) {
                系统相关类.发送广播("test",1,"单元信息获取失败，重新获取");
                e.printStackTrace();
            }
        }
    }

    //获取章节的信息
    private JSONObject getsection(String moduleId){
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


        while (true){
            try {

                系统相关类.发送广播("test",1,"正在获取章节信息");
                unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);


                return new JSONObject(unitdata.getData()) ;
            } catch (Exception e) {
                系统相关类.发送广播("test",1,"章节信息获取失败，重新获取");
                e.printStackTrace();

            }
        }

    }

    //获取到每节课的数据
    private JSONObject getlesson(String topicId){
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


        while (true){
            try {

                系统相关类.发送广播("test",1,"正在获取每节课的信息");
                unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);
                return  new JSONObject(unitdata.getData());
            } catch (Exception e) {
                e.printStackTrace();
                系统相关类.发送广播("test",1,"每节信息获取失败");
            }
        }
    }

    //获取每节课的详细数据
    private JSONObject getdetailed(String cellIdHash,String page,String  cellId,String fromType,String moduleId){
        /**
         * 获取每节课的详细数据
         * :param topicId: 章节ID
         */
        String url ="https://mooc.icve.com.cn/study/learn/viewDirectory";
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


        while (true){

            try {

                系统相关类.发送广播("test",1,"正在获取每节课的详细数据");

                unitdata = new zHttpRequest(url,"POST" ,headers,this.cookie, data);


                return new JSONObject(unitdata.getData());


            } catch (Exception e) {
                系统相关类.发送广播("test",1,"获取每节课的详细数据异常");
                e.printStackTrace();
            }
        }

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


    private void  TopicAllReply(String topicId){
        String url  = "https://mooc.icve.com.cn/study/discussion/getTopicAllReply";
        /**
         * 处理讨论
         * :param topicId: 章节ID
         */
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("courseOpenId",this.courseOpenId);
        data.put("topicId",topicId);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        try {

            系统相关类.发送广播("test",1,"正在获取讨论的信息 注意：讨论问题不自动回复");
            zHttpRequest unitdata = new zHttpRequest(url, "POST", headers, this.cookie, data);
        } catch (Exception e) {
            e.printStackTrace();
            系统相关类.发送广播("test",1,"获取讨论的信息失败");

        }
    }
}


class  MoocWork{

    String GetWorklisturl = "https://mooc.icve.com.cn/study/workExam/getWorkExamList";





    MoocWorkStart MoocWorkStart =null;


    private String courseOpenId;
    private String cookie;

    private  String token;

    private boolean isWork;  //作业
    //测验
    private boolean isTestst;
    //考试
    private boolean isExamst;

    private String newToken;
    private String userId;


    public MoocWork(String courseOpenId,String token,boolean isWork,boolean isTestst ,boolean isExamst){
        MoocWorkStart  =new MoocWorkStart(token);
        this.isWork = isWork;
        this.token  = token;
        this.isTestst = isTestst;
        this.isExamst = isExamst;
        this.courseOpenId = courseOpenId;
        getcookie();
    }



    /**
     *
     * @param Type    作业0  测验 1  考试 2
     * @return
     */
    private JSONObject GetWorklist(int Type ){
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("pageSize","1000");
        data.put("workExamType",Type+"");
        data.put("courseOpenId",this.courseOpenId);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");
        while (true){
            try {
                系统相关类.发送广播("test",1,"正在获取列表信息");
                zHttpRequest unitdata = new zHttpRequest(GetWorklisturl, "POST", headers, this.cookie, data);
                System.out.println(unitdata.getData()+"-------------");
                return new JSONObject(unitdata.getData());
            } catch (Exception e) {
                e.printStackTrace();
                系统相关类.发送广播("test",1,"获取列表信息失败");
            }
        }
    }


    public void Run(){
        for (int i=0; i<3;i++){

            if (i==0){
                if (!isWork){
                    continue;
                }

                系统相关类.发送广播("test",1,"开始处理作业");

            }else if (i==1){
                if (!isTestst){
                    continue;
                }
                系统相关类.发送广播("test",1,"开始处理测验");
            }else {
                if(!isExamst){
                    continue;
                }
                系统相关类.发送广播("test",1,"开始处理考试");
            }


            try {
                JSONObject json_info = GetWorklist(i);
                if (json_info.getInt("code") ==1){
                    JSONArray json_array = json_info.getJSONArray("list");
                    for (int i2=0;i2<json_array.length();i2++){
                        系统相关类.发送广播("test",1,"开始处理："+json_array.getJSONObject(i2).getString("Title"));

                        if (json_array.getJSONObject(i2).getInt("getScore")>=80){
                            系统相关类.发送广播("test",1,"本作业分数超过80 跳过..");
                            continue;
                        }

                        try {
                            系统相关类.发送广播("test",1,"开始自动做题.时间稍长耐心等待");
                            DisposeData(i,json_array.getJSONObject(i2).getString("Id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            系统相关类.发送广播("test",1,"本作业处理异常");
                        }

                    }


                }else {
                    系统相关类.发送广播("test",1,"获取列表信息错误");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }



    /**
     * @desc 执行刷课程序
     */
    private void DisposeData(int Type , String id){
        switch (Type){
            case 0:
                MoocWorkStart.Workst(this.courseOpenId,id);
                break;
            case 1:
                MoocWorkStart.Testst(this.courseOpenId,id);
                break;
            case 2:
                MoocWorkStart.Examst(this.courseOpenId,id);
                break;
        }

    }


    /**
     * @desc 更新职教云的cookie
     */
    public boolean getcookie(){
        String url = "http://api.qiufengvip.top/newapi/Login";


        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("token", this.token);

        zHttpRequest getdata=null;
        try {
            getdata = new zHttpRequest(url, "POST",headers,this.cookie, data);
            String yuandata = getdata.getData();
            String code =getSubString(yuandata,"\"code\":\"","\"");
            String cookie = getSubString(yuandata,"\"cookie\":\"","\"");
            String newToken = getSubString(yuandata,"\"newtoken\":\"","\"");
            String userId = getSubString(yuandata,"\"userid\":\"","\"");

            if (code.equals("200")){
                this.cookie = cookie;
                this.newToken = newToken;
                this.userId =userId;
                return true;
            }else {
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
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








}


class MoocWorkStart {





    /**
     * 定义API请求接口
     */
    private String get_MoocWork_url = "http://api.qiufengvip.top/newapi/MoocWork";//获取作业答案
    private String get_MoccTest_url = "http://api.qiufengvip.top/newapi/MoocTest";//获取测验答案
    private String get_MoocExam_url = "http://api.qiufengvip.top/newapi/MoocExam";//获取考试答案
    /**
     * 定义作业和测验的答案提交API
     */
    private String submitaw_url = "https://mooc.icve.com.cn/study/workExam/onlineHomeworkAnswer";
    private String sunExam_url = "https://mooc.icve.com.cn/study/workExam/onlineExamAnswer";

    /**
     * 定义作业和测验确认提交API
     */

    private String wKawSave = "https://mooc.icve.com.cn/study/workExam/workExamSave";
    private String eXawSave = "https://mooc.icve.com.cn/study/workExam/onlineExamSave";

    /**
     * 私有成员变量
     */
    private HashMap<String, String> dataMap = new HashMap<String,String>();
    private boolean isExam = false;
    private String token;
    private String cookie;
    private String newToken;
    private String userId;

    private HashMap<String, String> headers = new HashMap<String, String>();




    /**
     * 构造函数 传递请求通用数据结构
     * @param
     */
    public MoocWorkStart(String token) {
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");
        this.token =token;
    }

    /**
     * 外部调用
     */
    public void Workst(String courseopenid,String workexamid) {
        getcookie();
        isExam = false;
        dataMap.put("courseopenid", courseopenid);
        dataMap.put("workexamid", workexamid);
        dataMap.put("token", this.token );
        repadata(get_MoocWork_url,"0");
    }
    public void Testst(String courseopenid,String workexamid){
        getcookie();
        isExam =false;
        dataMap.put("courseopenid", courseopenid);
        dataMap.put("workexamid",workexamid);
        dataMap.put("token",  this.token);
        repadata(get_MoccTest_url,"1");
    }
    public void Examst(String courseopenid,String workexamid){
        getcookie();
        isExam = true;
        dataMap.put("courseopenid", courseopenid);
        dataMap.put("examId", workexamid);
        dataMap.put("token", this.token);
        Examstart(get_MoocExam_url);
    }


    /**
     * @desc  开始考试
     * @param get_moocExam_url
     */
    private void Examstart(String get_moocExam_url) {


        String rString = null;
        try {
            rString = initjson(getjson(get_moocExam_url));
            System.out.println(rString);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(rString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                if(jsonObject.getInt("code")<0){
                    System.out.println("本考试做过了不能在进行作答");
                    return ;
                }
            }catch (Exception e){

            }





            String courseOpenId = jsonObject.getString("courseOpenId");
            String examId    = jsonObject.getString("examId");
            String uniqueId    =  jsonObject.getString("uniqueId");
            System.out.println(courseOpenId);
            JSONObject questionObjict = jsonObject.getJSONObject("question");

            Iterator<String> iterator = questionObjict.keys();
            while(iterator.hasNext())
            {
                String question = (String)iterator.next();    //题目id   ok l
                String answer = questionObjict.getJSONObject(question).getString("answer");   //答案
                Integer questionType   = questionObjict.getJSONObject(question).getInt("questionType");  //题目类型

                int outtime = 50;// (new Random().nextInt(5) % (10-5+1)+5)*1000;
                System.out.println("outtime->"+outtime);
                try {
                    Thread.sleep(outtime);
                    HashMap<String,String> data = iniSubdata(uniqueId, question, answer, questionType,null);
                    ExamSubmitaw(data);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }
            HashMap<String,String> Exmap = new HashMap<String, String>();
            Exmap.put("uniqueId",uniqueId);
            Exmap.put("examId",examId);
            Exmap.put("workExamType","2");
            Exmap.put("courseOpenId",courseOpenId);
            Exmap.put("paperStructUnique","");
            Exmap.put("useTime","626");
            confirmSub_Ex(Exmap);
            ////////
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @desc  开始 作业和测验通用
     * @param urls
     */
    private void repadata(String urls,String WEid) {


        String rString = null;
        try {
            rString = initjson(getjson(urls));

            System.out.println(rString);
            JSONObject jsonObject = new JSONObject(rString);
            String courseOpenId = jsonObject.getString("courseOpenId");
            String workExamId    = jsonObject.getString("workExamId");
            String uniqueId    =  jsonObject.getString("uniqueId");
            //		System.out.println();
            System.out.println(courseOpenId);
            JSONObject questionObjict = jsonObject.getJSONObject("question");
            Iterator<String> iterator = questionObjict.keys();

            while(iterator.hasNext()) {
                String question = (String)iterator.next();    //题目id   ok l
                String answer = questionObjict.getJSONObject(question).getString("answer");   //答案
                Integer questionType   = questionObjict.getJSONObject(question).getInt("questionType");  //题目类型

                //			int outtime = (new Random().nextInt(5) % (10-5+1)+5)*1000;
                int outtime = 50;
                System.out.println("outtime->"+outtime);
                try {
                    Thread.sleep(outtime);

                    HashMap<String,String> data = iniSubdata(uniqueId, question, answer, questionType,WEid);
                    submitaw(data);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            HashMap<String,String> WTmap = new HashMap<String, String>();
            WTmap.put("uniqueId",uniqueId);
            WTmap.put("workExamId",workExamId);
            WTmap.put("workExamType",WEid);
            WTmap.put("courseOpenId",courseOpenId);
            WTmap.put("paperStructUnique","");
            WTmap.put("useTime", new Random().nextInt(600)+800+"");
            confirmSub_WT(WTmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @desc  提交考试的题目
     * @param data
     */
    private void ExamSubmitaw(HashMap<String,String> data) {
        String substr = new zHttpRequest(sunExam_url,"POST",this.headers ,this.cookie ,data).getData();
        System.out.println(substr);
    }

    /**
     * 请求API获取json
     * @param url
     * @return
     */
    private String getjson(String url) {
        String rejson = new String();

//        System.out.println(new zHttpRequest(url,"POST","525225",dataMap).getData());
        HashMap<String, String> herd = new HashMap<String, String>();
        herd.put("ContentType","text/xml;charset=utf-8");
        herd.put("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        System.out.println(dataMap.toString());
        System.out.println( zHttpRequest.sendPost(url,dataMap));
        rejson = new zHttpRequest(url,"POST",herd,dataMap).getData();


        return rejson;
    }

    /**
     * 初始化 提交作业和测验的提交数据 于 考试的数据
     * @param uniqueId
     * @param question
     * @param answer
     * @param questionType
     * @param workEt
     * @return
     */
    private HashMap<String,String> iniSubdata(String uniqueId, String question, String answer, Integer questionType,String workEt) {
        HashMap<String,String> subdata = new HashMap<String, String>();
        subdata.put("studentWorkId","");
        subdata.put("questionId", question);
        if (workEt != null){
            subdata.put("workExamType",workEt);
        }
        subdata.put("online","1");
        subdata.put("answer", answer);
        subdata.put("userId","");
        subdata.put("questionType", questionType.toString());
        subdata.put("paperStuQuestionId", "");
        subdata.put("uniqueId",uniqueId);
        系统相关类.发送广播("test",1,"正在 辛苦做题中:"+answer);
        try {


            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return subdata;

    }

    /**
     * 初始化json
     * @param json
     * @return
     */
    private String initjson(String json){
        String rjson;
        int index = json.indexOf("{");
        rjson = json.replace(json.substring(0,index),"");
        return rjson;
    }

    /**
     * 提交测验和作业的答案
     * @param data
     */
    private void submitaw(HashMap<String,String> data){
        String substr = new zHttpRequest(submitaw_url,"POST",this.headers,this.cookie,data).getData();
        System.out.println(substr);
    }

    /**
     * 作业和测验  交卷
     */
    private void confirmSub_WT(HashMap<String,String> data){
        System.out.println(cookie);
        System.out.println(data);
        String re = new zHttpRequest(wKawSave,"POST",this.headers,this.cookie,data).getData();
        System.out.println(re);
    }

    /**
     * 考试确认提交
     */
    private void confirmSub_Ex(HashMap<String,String> data){
        String re = new zHttpRequest(eXawSave,"POST",this.headers,this.cookie,data).getData();
        System.out.println(re);
        System.out.println("提交成功....");
    }

    /**
     * @desc 更新职教云的cookie
     */
    public boolean getcookie(){
        String url = "http://api.qiufengvip.top/newapi/Login";

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("token", this.token);

        zHttpRequest getdata=null;
        try {
            getdata = new zHttpRequest(url, "POST",this.headers,this.cookie, data);
            String yuandata = getdata.getData();
            String code =getSubString(yuandata,"\"code\":\"","\"");
            String cookie = getSubString(yuandata,"\"cookie\":\"","\"");
            String newToken = getSubString(yuandata,"\"newtoken\":\"","\"");
            String userId = getSubString(yuandata,"\"userid\":\"","\"");

            if (code.equals("200")){
                this.cookie = cookie;
                this.newToken = newToken;
                this.userId =userId;
                return true;
            }else {
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
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

}

