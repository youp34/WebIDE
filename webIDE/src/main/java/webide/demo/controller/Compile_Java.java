package webide.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import webide.demo.compile_tool.CustomStringJavaCompiler;
import webide.demo.compile_tool.ExecuteCLangService;
import webide.demo.pojo.code;

@Controller
public class Compile_Java {
    @RequestMapping(value = "/")
    public String hello(){
        return "select";
    }
    @RequestMapping(value = "/java", method = RequestMethod.GET)
    public String java_get(){
        return "compile";
    }
    @RequestMapping(value = "/java", method = RequestMethod.POST)
    public String java_post(@RequestParam("java_source")String java_source, Model model){
        if (java_source.equals("")){
            System.out.println("======================null====================");
        } else {
            code a = new code();
            a.setCoder(java_source);
            CustomStringJavaCompiler.main();
            model.addAttribute("code", a.getCoder());
            model.addAttribute("result", a.getResult());
        }
        return "compile";
    }
    @RequestMapping(value = "/python", method = RequestMethod.GET)
    public String python_get(){
        return "compile_python";
    }
    @RequestMapping(value = "/c", method = RequestMethod.GET)
    public String c_get(){
        return "compile_c";
    }
    @RequestMapping(value = "/c", method = RequestMethod.POST)
    public String c_post(@RequestParam("c_source")String c_source, Model model){
        if (c_source.equals("")){
            System.out.println("=====================null===================");
        } else {
            code a = new code();
            a.setCoder(c_source);
            ExecuteCLangService b = new ExecuteCLangService();
            String c =b.runCLangCode(c_source);
            System.out.println(c);
            /*model.addAttribute("code_c", a.getCoder());
            model.addAttribute("result_c", a.getResult());*/
        }
        return "compile_c";
    }
}
