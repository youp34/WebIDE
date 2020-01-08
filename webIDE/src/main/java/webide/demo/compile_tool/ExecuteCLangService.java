package webide.demo.compile_tool;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.*;


@Service
public class ExecuteCLangService {

    //代码存放路径
    public static final String CODE_PATH = "E:\\webide_code";


    /**
     * @param content C代码
     * */
    private boolean generateCFile(String content){
        BufferedWriter out = null;
        try{
            //写入
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CODE_PATH+"test.c", false)));
            out.write(content);
        }catch (Exception e){
            System.out.println(e.getCause().getMessage());
            return false;
        }finally {
            try {
                out.close();
            }catch (IOException e){
                System.out.println(e.getCause().getMessage());
                return false;
            }
        }
        return true;
    }
    public String runCLangCode(String sourceCode){
        //先生成文件
        generateCFile(sourceCode);
        Executor executor = Executors.newSingleThreadExecutor();
        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                //编译C文件
                String compileResult = execCmd("gcc -o "+CODE_PATH+"test "+CODE_PATH+"test.c",null);
                if (compileResult.equals(""))
                    //编译不出错的情况，replaceAll将\n换成HTML的换行，空格换成HTML的空格
                    return execCmd(CODE_PATH+"test.exe",null).replaceAll("\n","<br/>").replaceAll(" ","&nbsp;");
                else {
                    //编译出错，找到error的位置，返回error及其后的信息
                    int errorIndex = compileResult.indexOf("error");
                    return compileResult.substring(errorIndex).replaceAll("\n","<br/>").replaceAll(" ","&nbsp;");
                }
            }
        });
        executor.execute(futureTask);
        try {
            //编译运行完毕将text.exe的进程kill
            execCmd("taskkill /f /im test.exe",null);
            System.out.println("killed test.exe");
        }catch (Exception e){
            e.printStackTrace();
        }
        String result = "";
        try{
            //设置超时时间
            result=futureTask.get(10, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            System.out.println("Interrupt");
            result = "程序中断，请检查是否有内存冲突等错误";
            // future.cancel(true);
        }catch (ExecutionException e) {
            result = "程序执行错误";
            futureTask.cancel(true);
        }catch (TimeoutException e) {
            result = "时间超限，请检查是否存在无限循环等程序无法自动结束的情况";
        }
        System.out.println("result - "+result);
        return result.equals("")?"没有输出":result;
    }
    /**
     * 执行系统命令, 返回执行结果
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    private String execCmd(String cmd, File dir) throws Exception {
        StringBuilder result = new StringBuilder();

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(cmd, null, dir);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }

        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        return result.toString();
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
}
