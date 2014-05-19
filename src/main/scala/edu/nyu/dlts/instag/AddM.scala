package edu.nyu.dlts.instag

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File
import scala.collection.mutable.Map

object AddMbz{
	def main(args: Array[String]) ={
		val conf = ConfigFactory.load()
		val db = new Db(conf)
		db.createTables

		//add mbz
		val map = Map.empty[String, String]
		map("uId") = "180521710"
		map("uName") = "mbzphotos"
		map("uFullName") = "Mohamed bin Zayed Al Nahyan"
		db.addAccount(map)
	}
}