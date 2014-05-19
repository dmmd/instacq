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
	
	db.getAccounts.foreach{account => 
		val userUUID = UUID.fromString(account("id"))
		val crawlUUID = UUID.randomUUID

		val crawlDir = new File(conf.getString("instag.data_dir"), crawlUUID.toString)
		if(! crawlDir.exists) crawlDir.mkdir

		db.addCrawl(crawlUUID, userUUID)
		val images = requests.getImagesById(account("userId"))

		images.foreach{image =>
			val mediaMap = requests.getImageById(image._1)
			mediaMap("accountId") = account("id")
			mediaMap("crawlId") = crawlUUID.toString
			mediaMap("mediaId") = image._1
			mediaMap("imageUrl") = image._2
			writeFile(image._2, crawlDir)
			db.addImage(mediaMap)
		}

		def writeFile(url: String, dir: File){
			val file = new File(dir, url.split("/").last)
			new ImageDownload(url, file, client)
		}
	}
}