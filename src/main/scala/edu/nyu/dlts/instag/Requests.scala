package edu.nyu.dlts.instag

import com.typesafe.config.Config
import java.io.{BufferedReader, StringReader}
import java.util.UUID
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.JsonNode
import scala.io.Source
import scala.collection.mutable.{MutableList, Map}
import scala.annotation.tailrec

class Requests(client: CloseableHttpClient, conf: Config){
  
  def request(get: HttpGet): JsonNode = {
    val response = client.execute(get)
    val entity = response.getEntity
    val content = Source.fromInputStream(entity.getContent).mkString("")
    EntityUtils.consume(entity)
    response.close
    val mapper = new ObjectMapper
    val reader = new BufferedReader(new StringReader(content))
    mapper.readTree(reader)
  }	
  
  def getUserById(uId: String): Map[String, String] ={
    val map = Map.empty[String, String]
    val get = new HttpGet(conf.getString("instag.endpoint") + "users/" + uId + "/?client_id=" + conf.getString("instag.client_id"))
    val root = request(get)
    map.put("uId", uId)
    map.put("uName", root.get("data").get("username").getTextValue)
    map.put("uFullName", root.get("data").get("full_name").getTextValue)
    map
  }

  def getImagesById(uId: String): Map[String, String] = {
    val url = conf.getString("instag.endpoint") + "/users/" +  uId + "/media/recent?client_id=" + conf.getString("instag.client_id")
    val map = Map.empty[String, String]
    getResult(url)
    @tailrec 
    def getResult(url :String): Unit = {
      val rootNode = request(new HttpGet(url))
      val pageNode = rootNode.get("pagination")
      val dataNode = rootNode.get("data")
      
      (0 to dataNode.size - 1).foreach{i => 
	map(dataNode.get(i).get("id").getTextValue) = dataNode.get(i).get("images").get("standard_resolution").get("url").getTextValue
      }
	
      if(pageNode.size == 2) getResult(pageNode.get("next_url").getTextValue)
    }
    map
  }

  def getImageById(id: String): Map[String, String] ={
    val map = Map.empty[String, String]
    val get = new HttpGet(conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id"))
    val rootNode = request(get)
    val dataNode = rootNode.get("data")	

    map("createdTime") = dataNode.get("created_time").getTextValue
    if(! dataNode.get("caption").isNull) 
      map("caption") = dataNode.get("caption").get("text").getTextValue
    else 
      map("caption") = ""	
    map
  }

  def getCommentsByMediaId(id: String): MutableList[Map[String, String]] = {
    import scala.collection.JavaConversions._
    val list = new MutableList[Map[String, String]]
    val get = new HttpGet(conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id"))
    val rootNode = request(get)  
    if(rootNode.get("meta").get("code").getIntValue == 200){
      val dataNode = rootNode.get("data").get("comments").get("data")
      dataNode.foreach{comment => 
        val map = Map.empty[String, String]
        map("commentId") = comment.get("id").getTextValue
        map("text") = comment.get("text").getTextValue
        map("createdTime") = comment.get("created_time").getTextValue
        map("username") = comment.get("from").get("username").getTextValue
        map("userFullName") = comment.get("from").get("full_name").getTextValue
        map("userId") = comment.get("from").get("id").getTextValue
        list += map
      }
    }
    list
  }
}
