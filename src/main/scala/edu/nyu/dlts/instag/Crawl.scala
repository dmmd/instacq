package edu.nyu.dlts.instag

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File
import grizzled.slf4j._

class Crawl(){
  val conf = ConfigFactory.load()
  val db = new Db(conf)
  val client = HttpClients.createDefault()
  val requests = new Requests(client, conf)
  val mediaIds = db.getImageIds
  var count = 0
  val logger = Logger(classOf[Crawl])
  logger.info("hello")

  db.getAccounts.foreach{account => 
    logger.info("Getting images for " + account("id"))
    val userUUID = UUID.fromString(account("id"))
    val crawlUUID = UUID.randomUUID
    val crawlDir = new File(conf.getString("instag.data_dir"), crawlUUID.toString)
    db.addCrawl(crawlUUID, userUUID)    
    crawlDir.mkdir

    val images = requests.getImagesById(account("userId"))
    images.foreach{image =>
      if(! mediaIds.contains(image._1)){
        logger.info(image._1)
        count += 1
        val mediaMap = requests.getImageById(image._1)
	mediaMap("accountId") = account("id")
        mediaMap("crawlId") = crawlUUID.toString
        mediaMap("mediaId") = image._1
        mediaMap("imageUrl") = image._2		    
        writeFile(image._2, crawlDir)		    
        db.addImage(mediaMap)
        count += 1
      }
    }
    
    if(crawlDir.list().length == 0){
      crawlDir.delete
      db.deleteCrawl(crawlUUID)
    }

    logger.info(count + " images to account: " + account("id"))
  }

  var commentCount = 0
  val images = db.getAllImageIds
  val commentIds = db.getCommentIds()

  logger.info("updating comments")
  images.foreach{image => 
    val imageUUID = image._1
    val imageId = image._2
    val comments = requests.getCommentsByMediaId(imageId)
    if(comments.size > 0){
      comments.foreach{comment =>
        if(! commentIds.contains(comment("commentId"))){
          comment("imageId") = imageUUID.toString
          db.addComment(comment)
          commentCount += 1
        }
      }
    }
  }

  logger.info(commentCount + " comments added")

 
  
  def writeFile(url: String, dir: File){
    val file = new File(dir, url.split("/").last)
    new ImageDownload(url, file, client)
  }
}

object Crawl extends App{new Crawl}
