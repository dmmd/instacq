package edu.nyu.dlts.instag

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID

class Dummy{
  val conf = ConfigFactory.load()
  val client = HttpClients.createDefault()
  val db = new Db(conf)
  val requests = new Requests(client, conf)

}

object Dummy{
  def main(args: Array[String]){
    new Dummy
  }
}
