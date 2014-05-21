package edu.nyu.dlts.instag

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File

class Crawl(){
  val conf = ConfigFactory.load()
  val db = new Db(conf)
  val client = HttpClients.createDefault()
  val requests = new Requests(client, conf)
  val mediaIds = db.getImageIds
  var count = 0

  db.getAccounts.foreach{account => 
   
    val userUUID = UUID.fromString(account("id"))
    val crawlUUID = UUID.randomUUID
    val crawlDir = new File(conf.getString("instag.data_dir"), crawlUUID.toString)
    
    crawlDir.mkdir

    val images = requests.getImagesById(account("userId"))
    images.foreach{image =>
      if(! mediaIds.contains(image._1)){
	println(image._1)
	count += 1
	val mediaMap = requests.getImageById(image._1)
	mediaMap("accountId") = account("id")
	mediaMap("crawlId") = crawlUUID.toString
	mediaMap("mediaId") = image._1
	mediaMap("imageUrl") = image._2		    
	writeFile(image._2, crawlDir)		    
	db.addImage(mediaMap)
      }
    }
    
    if(crawlDir.list().length == 0){crawlDir.delete}
    else{db.addCrawl(crawlUUID, userUUID)}
  }
  
  def writeFile(url: String, dir: File){
    val file = new File(dir, url.split("/").last)
    new ImageDownload(url, file, client)
  }

}

object Crawl{
  def main(args: Array[String]){
    new Crawl()
  }
}
