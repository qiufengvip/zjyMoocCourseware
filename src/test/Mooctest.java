package test;

import com.qiufeng.MoocTask;

public class Mooctest {
    public static void main(String[] args) {
        MoocTask mooctask = new MoocTask();
        //String token,
        // String courseOpenId,
        // boolean isWork,
        // boolean isTestst,
        // boolean isExamst,
        // boolean isclass
        mooctask.init("e31820d5ef3161deda1f37185d5863fc","vkglzxqkkba",false,true,false,false);
        mooctask.start();
    }
}
