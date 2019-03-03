package com.psvm.web.controller;

import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author psvm
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "getInfo",method = RequestMethod.GET)
    public Map<String,Object> testJsonReturn(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> returnMap = Maps.newHashMap();
        returnMap.put("name","psvm");
        return  returnMap;

    }


    public static void main(String[] args) {

    }
}
