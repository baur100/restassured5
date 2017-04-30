package org.megaprint;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;

/**
 * Created by Baurz on 4/16/2017.
 */
public class JiraTest {
    Response resp;
    Properties prop=new Properties();
    ITestContext context;
    @BeforeMethod
    public void init() throws FileNotFoundException {
        FileInputStream fis=new FileInputStream("data//env.properties");
        try {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RestAssured.baseURI=prop.getProperty("HOST");
    }
    @Test(priority = 1)
    public void login(ITestContext context){
        resp=given().
                header("Content-Type","application/json").
                body("{ \"username\": \"baurzhan-zh\", \"password\": \"smarot100\" }").
                when().
                post("/rest/auth/1/session").
                then().
                assertThat().
                statusCode(200).
                extract().
                response();
        JsonPath jpath=new JsonPath(resp.asString());
        String temp=jpath.getString("session.name")+"="+jpath.getString("session.value");
        context.setAttribute("sessionId",temp);
    }
    @Test(priority=2)
    public void createIssue(ITestContext context){
        resp=given().
                header("Content-Type","application/json").
                header("Cookie",context.getAttribute("sessionId").toString()).
                body("{\n" +
                        "    \n" +
                        "    \"fields\": {\n" +
                        "        \"project\": {\n" +
                        "            \"key\":\"RES\"\n" +
                        "        },\n" +
                        "        \"summary\":\"Comment for Add/Delete comment\",\n" +
                        "        \"description\":\"Bug report created using REST POST request using POSTMAN\",\n" +
                        "        \"issuetype\":{\n" +
                        "        \t\"name\":\"Bug\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}").
        when().
                post("/rest/api/2/issue").
        then().
                assertThat().
                statusCode(201).
        extract().
                response();
        JsonPath jpath=new JsonPath(resp.asString());
        context.setAttribute("issueId",jpath.getString("id"));
    }
    @Test(priority = 3)
    public void test1(ITestContext context){
        System.out.println(context.getAttribute("issueId").toString());
        System.out.println(context.getAttribute("sessionId").toString());
    }
    @Test(priority = 3)
    public void deleteIssue(ITestContext context){
        given().
                header("Content-Type","application/json").
                header("Cookie",context.getAttribute("sessionId".toString())).
        when().
                delete("/rest/api/2/issue/"+context.getAttribute("issueId").toString()).
        then().
                assertThat().
                statusCode(204);
    }

}
