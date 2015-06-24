package org.opencv.samples.facedetect;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Nixy on 2015/6/21.
 * API
 *
 * boolean   logincheck  (String account,String pwd)  //True for successful
 * void create_account(String account,String pwd,String email)
 * boolean  check_account(String account) // True can use
 * boolean  check_email(String email) // True can use
 * String  get_pwd(String account)
 *void update_record(String account,String record) // put new record into database and arrange it
 */

/*

把這段 放在 mainactivity 的 oncreate中

StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
*/


public class DBfunction {

    public static void out (Object msg){
        Log.i ("info", msg.toString ());
    }

    public static boolean logincheck(String account,String pwd) {

        int index=get_index(account);
        try {
            String result = DBconnector.executeQuery("SELECT `pwd` FROM `player` where `index` ='" + index + "'");
            // out(" t in ");
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String user_pwd=jsonData.getString("pwd");
                //out(user_pwd+" <> "+pwd+"=="+user_pwd.equals(pwd));
                if( user_pwd.equals(pwd)){

                    return true;
                }
            }
        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        return false;
    }

    public static void create_account(String account,String pwd,String email) {
        String result="";
        if(check_account(account)&&check_email(email)) {
            // out("create! " + account + " " + pwd + " " + email);
            result = DBconnector.executeQuery("INSERT INTO `cfhsu_cs_db2014`.`player` (`index`, `account`, `pwd`, `email`, `record`) VALUES (NULL, ' " + account + "', '" + pwd + "', '" + email + "', NULL)");
        }
        return;
    }

    public static boolean check_account(String account) {
        String result="";
        int index=get_index(account);

        if(index==0) {
            return true;
        }
        return false;
    }

    public static boolean check_email(String email) {
        String result="";
        result = DBconnector.executeQuery("SELECT `index` FROM  `cfhsu_cs_db2014`.`player` where email ='" + email + "'");

        String Nullres="null\n";
        //out("email check! "+!result.equals(Nullres));
        return result.equals(Nullres);
    }

    public static String get_pwd(String account) {
        String result="";
        int index=get_index(account);
        result = DBconnector.executeQuery("SELECT `pwd` FROM `cfhsu_cs_db2014`.`player` where `index` ='" + index + "'");
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject pwd = jsonArray.getJSONObject(0);
            result = pwd.getString("pwd");
        }catch (Exception e){
        }
        // out("get pwd!"+ result);
        return result;
    }

    public static void update_record(String account,String record) {
        String result = "";
        String record_stream = "";
        int index = get_index(account);
        result = DBconnector.executeQuery("SELECT `record` FROM `cfhsu_cs_db2014`.`player` where `index` ='" + index + "'");

        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsondata = jsonArray.getJSONObject(0);
            result = jsondata.getString("record");
            //out("in update record ->" + result + "-");
        } catch (Exception e) {
        }
        String Nullres = "null\n";
        if (result.equals(Nullres) || result.equals("null")||result.equals("")) {
            //out("successful");
            record_stream = record;
        } else if (result.length() < 10){
            if(judge_time(record,result))
                record_stream=record+","+result;
            else
                record_stream=result+","+record;
        }else{
            record_stream = insert_record(record,result);
        }
        // out("in update record =*"+record_stream+"*");
        result = DBconnector.executeQuery("UPDATE `cfhsu_cs_db2014`.`player` SET `record` = '" + record_stream + "' WHERE `player`.`index` = '" + index + "' ");

        return ;
    }

    public static String insert_record(String record,String record_stream){

        String [] rcd_temp=record_stream.split(",");

        //out("start insert  -record_stream  split -"+record+"-"+record_stream+"-"+rcd_temp[0]+rcd_temp[1]+"-"+rcd_temp.length);

        for(int i=0;i<rcd_temp.length;i++){
            // out("if for ->"+record+">"+rcd_temp[i]);
            // out(judge_time(record,rcd_temp[i]));
            if(judge_time(record,rcd_temp[i])){
                rcd_temp[i]=record+","+rcd_temp[i];
                // out(rcd_temp[i]);
                break;
            }
            if(i==rcd_temp.length-1){
                rcd_temp[i]=rcd_temp[i]+","+record;
            }
        }
        record_stream ="";
        for(int i=0;i<rcd_temp.length;i++){
            if(i==0) {
                record_stream = rcd_temp[i];
            }else{
                record_stream = record_stream+","+rcd_temp[i];
            }
        }
        //out("in insert  -record_stream  -"+record_stream+"-");
        return  record_stream;
    }
    public static boolean judge_time(String str1,String str2){
        char [] c1=str1.toCharArray();
        char [] c2=str2.toCharArray();
        float time1 = (int)c1[0]*10*60+(int)c1[1]*60+(int)c1[3]*10+(int)c1[4]+(float)((int)c1[6])/10+(float)((int)c1[7])/100;
        float time2 = (int)c2[0]*10*60+(int)c2[1]*60+(int)c2[3]*10+(int)c2[4]+(float)((int)c2[6])/10+(float)((int)c2[7])/100;
        return time1>time2;
    }

    public static String get_record(String account,int index){
        String result="";
        int ac_index=get_index(account);
        result = DBconnector.executeQuery("SELECT `record` FROM `cfhsu_cs_db2014`.`player` where `index` ='" + ac_index + "'");
        // out("1get record !"+account+" : "+ac_index );
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject pwd = jsonArray.getJSONObject(0);
            result = pwd.getString("record");
        }catch (Exception e){
        }

        String Nullres="null\n";
        if(result.equals(Nullres)) {
            return "Nothing!";
        }
        //out("2 get record !"+account+" : "+result );

        if(result.length()<9){
            if(index>1)
                return "Nothing";
            else
                return result;
        }

        String [] rcd_temp=result.split(",");

        if(index-1>rcd_temp.length)
            return "Nothing";
        else
            return rcd_temp[index-1];
    }

    public static int get_index(String account){
        //out("get index"+account);

        try {
            String result = DBconnector.executeQuery("SELECT * FROM `player` ");

            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String user_account=jsonData.getString("account");
                user_account=user_account.replaceAll(" ", "");
                //out("??-"+user_account+"-"+account+"-"+user_account.equals(" "+account));
                if(user_account.equals(account)){
                    String num = jsonData.getString("index");
                    // out(account+ " index ="+num);
                    return Integer.valueOf(num);
                }
            }
        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        return 0;
    }

    public static String [] best_record(int index){
        String [] return_list= new String[index*2];
        for(int i=0;i<index;i++){
            return_list[i]="No player";
            return_list[i+index]="00:00:00";
        }
        try {
            String result = DBconnector.executeQuery("SELECT * FROM `player` ");

            JSONArray jsonArray = new JSONArray(result);
            int length=jsonArray.length();
            String [] record_list= new String[length*2];

            for(int i=0;i<length;i++){
                record_list[i]="";
                record_list[i+length]="00:00:00";
            }

            for (int i = 0; i < length; i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                record_list[i]=jsonData.getString("account").replaceAll(" ", "");
                if(jsonData.getString("record").length()>10) {
                    String [] rcd_temp= jsonData.getString("record").replaceAll(" ", "").split(",");
                    record_list[i + length] = rcd_temp[0];
                }else if(jsonData.getString("record").length()>5){
                    record_list[i + length] = jsonData.getString("record").replaceAll(" ", "");
                }
                //out("ff"+i+": " +record_list[i]+" -- "+record_list[i+length]);
            }

            String temp="";
            for(int i=0;i<length;i++){
                for (int k=0;k<length;k++){
                    if(judge_time(record_list[i+length],record_list[k+length])){
                        temp=record_list[i+length];
                        record_list[i+length]=record_list[k+length];
                        record_list[k+length]=temp;

                        temp=record_list[i];
                        record_list[i]=record_list[k];
                        record_list[k]=temp;
                    }
                }
            }
            for(int i=0;i<index;i++){
                return_list[i]=record_list[i];

                return_list[i+index]=record_list[i+length];
                //out(i+": " +return_list[i]+" -- "+return_list[i+index]);
            }
        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }

        return return_list;
    }


    public static void try_db(String account,String pwd){

        out(" - try database query");
        try{
            String result = DBconnector.executeQuery("SELECT * FROM `player`");

            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                out(jsonData.getString("index") + jsonData.getString("account") + "-" + jsonData.getString("pwd") + "-" + jsonData.getString("record"));
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        }
        out(" -end  try database query");

        out(" - start get index !");
        get_index(account);
        out(" - end get index !");

        out(" - start login check !");
        logincheck(account,pwd);
        out(" - end login check !");

        out(" - start update record !");
        update_record(account,"01:20.20");
        update_record(account,"25:20.35");
        update_record(account,"00:38.47");
        out(" - end update record !");

        out(" - start get record !");
        out(get_record(account, 1));
        out(get_record(account, 2));
        out(get_record(account, 3));
        out(" - end get record !");

        out(judge_time("02:12.32","00:15.36"));
        out(judge_time("00:12.32","00:15.36"));
    }


};
