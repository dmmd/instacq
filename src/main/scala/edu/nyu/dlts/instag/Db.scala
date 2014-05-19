package edu.nyu.dlts.instag

import com.typesafe.config.Config
import java.util.{UUID, Date}
import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._
import scala.collection.mutable.{MutableList, Map}

class Db(conf: Config){
  val connection = Database.forURL("jdbc:postgresql://localhost:5432/insg", driver = "org.postgresql.Driver", user="insg", password="insg")	

  val accounts = TableQuery[Accounts]
  val crawls = TableQuery[Crawls]
  val images = TableQuery[Images]

  class Accounts(tag: Tag) extends Table[(UUID, String, String, String)](tag, "ACCOUNTS"){
  	def id = column[UUID]("ID", O.PrimaryKey)
	  def userId = column[String]("USER_ID")
	  def userName = column[String]("USER_NAME")
	  def userFullName = column[String]("USER_FULL_NAME")
	  def * = (id, userId, userName, userFullName)  	
  }

  class Crawls(tag: Tag) extends Table[(UUID, UUID, Timestamp)](tag, "CRAWLS"){
  	def id = column[UUID]("ID", O.PrimaryKey)
  	def accountId = column[UUID]("ACCOUNT_ID")
  	def crawlDate = column[Timestamp]("CRAWL_DATE")
  	def * = (id, accountId, crawlDate)
  	def account = foreignKey("ACC_FK", accountId, accounts)(_.id)
  }

  class Images(tag:Tag) extends Table[(UUID, UUID, UUID, String, String, String, Timestamp, Boolean)](tag, "IMAGES"){
    def id = column[UUID]("ID", O.PrimaryKey)
    def accountId = column[UUID]("ACCOUNT_ID")
    def crawlId = column[UUID]("CRAWL_ID")
    def mediaId = column[String]("MEDIA_ID")
    def imageUrl = column[String]("STANDARD_URL")
    def caption = column[String]("CAPTION", O.DBType("varchar(4000)"))
    def createdDate = column[Timestamp]("CREATED_DATE")
    def isDownloaded = column[Boolean]("IS_DOWNLOADED")  
    def * = (id, accountId, crawlId, mediaId, imageUrl, caption, createdDate, isDownloaded)
    def account = foreignKey("ACC_FK", accountId, accounts)(_.id)
    def crawl = foreignKey("CRL_FK", crawlId, crawls)(_.id)
  }

  def addImage(map: Map[String, String]){
    connection.withSession{implicit session =>
      images += (
        UUID.randomUUID, 
        UUID.fromString(map("accountId")),
        UUID.fromString(map("crawlId")),
        map("mediaId"),
        map("imageUrl"),
        map("caption"),
        timestamp(map("createdTime")),
        false
      )
    }
  }

  def createTables(){
  	connection.withSession{implicit session =>
  		(accounts.ddl ++ crawls.ddl ++ images.ddl).create
  	}
  }

  //acount functions
  def addAccount(map: Map[String, String]){
  	connection.withSession{ implicit session =>
  		accounts += (UUID.randomUUID, map("uId"), map("uName"), map("uFullName"))
  	}
  }

  def getAccountIds(): MutableList[UUID] ={
    val uuids = new MutableList[UUID]

    connection.withSession{ implicit session =>
      val query = for(a <- accounts) yield a.id
      query foreach{q => uuids += q}    
    }
    uuids
  }

  def getAccounts(): MutableList[Map[String, String]] = {
    val list = new MutableList[Map[String, String]]

    connection.withSession{ implicit session =>
      accounts foreach{ case (id, userId, userName, userFullName) =>
        val map = Map.empty[String, String]
        map("id") = id.toString
        map("userId") = userId
        map("userName") = userName
        list += map
      }
    }

    list
  }

  //crawl functions
  def addCrawl(crawlId: UUID, uId: UUID){
    connection.withSession{ implicit session =>
      crawls += (crawlId, uId, timestamp)
    }
  }
  
  //misc functions 
  def timestamp(): Timestamp = {new Timestamp(new Date().getTime())}
  def timestamp(time: String): Timestamp = {new Timestamp(new Date(time.toLong * 1000l).getTime)}
}