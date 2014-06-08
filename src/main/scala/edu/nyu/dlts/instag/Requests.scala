package edu.nyu.dlts.instag

import com.typesafe.config.Config
import java.io.{BufferedReader, StringReader}
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper
import scala.io.Source
import scala.collection.mutable.{Map}
import scala.annotation.tailrec

class Requests(client: CloseableHttpClient, conf: Config){
	
	def getUserById(uId: String): Map[String, String] ={
		val map = Map.empty[String, String]
		val get = new HttpGet(conf.getString("instag.endpoint") + "users/" + uId + "/?client_id=" + conf.getString("instag.client_id"))
		val response = client.execute(get)
		val entity = response.getEntity
		val content = Source.fromInputStream(entity.getContent).mkString("")
		EntityUtils.consume(entity)
		response.close
		val mapper = new ObjectMapper
		val reader = new BufferedReader(new StringReader(content))
		val root = mapper.readTree(reader)
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
		    val get = new HttpGet(url)
		    val response = client.execute(get)
		    val entity = response.getEntity
		    val result = Source.fromInputStream(entity.getContent).mkString("")      
		    val mapper = new ObjectMapper();
		    val reader = new BufferedReader(new StringReader(result))
		    val rootNode = mapper.readTree(reader)
		    val pageNode = rootNode.get("pagination")
		    val dataNode = rootNode.get("data")
		    
		    EntityUtils.consume(entity)
		    response.close

		    (0 to dataNode.size - 1).foreach{i => 
		      map(dataNode.get(i).get("id").getTextValue) = dataNode.get(i).get("images").get("standard_resolution").get("url").getTextValue
		    }

		    if(pageNode.size == 2) getResult(pageNode.get("next_url").getTextValue)
	  	}
	  	map
	}

	def getImageById(id: String): Map[String, String] ={
		val map = Map.empty[String, String]
		val url = conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id")
		val get = new HttpGet(url)
		val response = client.execute(get)
	    val entity = response.getEntity
	    val result = Source.fromInputStream(entity.getContent).mkString("")
	    val mapper = new ObjectMapper();
		val reader = new BufferedReader(new StringReader(result))
	    val rootNode = mapper.readTree(reader)
	    val dataNode = rootNode.get("data")	

	   	EntityUtils.consume(entity)
		response.close 
		
		map("createdTime") = dataNode.get("created_time").getTextValue
		if(! dataNode.get("caption").isNull) 
			map("caption") = dataNode.get("caption").get("text").getTextValue
		else 
			map("caption") = ""	
		map
	}

  def getCommentsByMediaId(id: String): Unit = {
    import scala.collection.JavaConversions._
    val url = conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id")
    val get = new HttpGet(url)
    val response = client.execute(get)
    val entity = response.getEntity
    val result = Source.fromInputStream(entity.getContent).mkString("")
    val mapper = new ObjectMapper();
    val reader = new BufferedReader(new StringReader(result))
    val rootNode = mapper.readTree(reader)
    val dataNode = rootNode.get("data").get("comments").get("data")
    dataNode.foreach{comment =>
      println("id: " + comment.get("id").getTextValue())
      println("created_time: " + comment.get("created_time"))
      println("text: " + comment.get("text").getTextValue)
      println("username: " + comment.get("from").get("username").getTextValue())
      println("full name: " + comment.get("from").get("full_name").getTextValue())
      println("id: " + comment.get("from").get("id").getTextValue())
      println()
    }
  }
}
