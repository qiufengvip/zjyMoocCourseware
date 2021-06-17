package test;

import com.qiufeng.zjyMoocCourses;

public class moocCoursesTest {
    public static void main(String[] args) {
        System.out.println("开始");
        zjyMoocCourses zjyMoocCourses= null;
        try {
            zjyMoocCourses = new zjyMoocCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
        zjyMoocCourses.初始化("0d2df77592d188ac2b69cf641a5c50a4","3zdafor6oldcvuunhh7g");
        Thread 职教云mooc线程 = new Thread(zjyMoocCourses);
        职教云mooc线程.start();

    }

}
