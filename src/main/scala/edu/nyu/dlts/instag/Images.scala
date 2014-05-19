package edu.nyu.dlts.instag

import com.typesafe.config.Config
import org.apache.http.impl.client.CloseableHttpClient

class Images(userId: String, client: CloseableHttpClient, conf: Config){ 

  import org.apache.http.HttpEntity
  import org.apache.http.client.methods.HttpGet
  import org.apache.http.util.EntityUtils  
  import scala.annotation.tailrec
  import scala.collection.mutable.Map

  val url = conf.getString("instarch.endpoint") + "/users/" +  userId + "/media/recent?access_token=" + conf.getString("instarch.token")
  val mediaMap = Map.empty[String, String]
  println("mapping id: " + userId)
  getResult(url)

  @tailrec 
  final def getResult(url :String): Unit = {
    import org.codehaus.jackson.map.ObjectMapper
    import java.io.BufferedReader
    import java.io.StringReader

    val get = new HttpGet(url)
    val response = client.execute(get)
    val entity = response.getEntity
    val result = scala.io.Source.fromInputStream(entity.getContent).mkString("")      
    val mapper = new ObjectMapper();
    val reader = new BufferedReader(new StringReader(result))
    val rootNode = mapper.readTree(reader)
    val pageNode = rootNode.get("pagination")
    val dataNode = rootNode.get("data")
    
    EntityUtils.consume(entity)
    response.close

    (0 to dataNode.size - 1).foreach{i => 
      mediaMap(dataNode.get(i).get("id").getTextValue) = dataNode.get(i).get("images").get("standard_resolution").get("url").getTextValue
    }

    if(pageNode.size == 2) getResult(pageNode.get("next_url").getTextValue)
  }
}