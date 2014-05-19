package edu.nyu.dlts.instag

import com.typesafe.config.Config
import java.io.{BufferedReader, StringReader}
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper
import scala.io.Source
import scala.collection.mutable

class Users(client: CloseableHttpClient, conf: Config){

	def getUserById(uId: String): mutable.Map[String, String] ={
		val map = mutable.Map.empty[String, String]
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
}