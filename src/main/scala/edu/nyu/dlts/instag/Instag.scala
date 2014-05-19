package edu.nyu.dlts.instag

class Instag{
	import com.typesafe.config.ConfigFactory
	import org.apache.http.impl.client.HttpClients

	val conf = ConfigFactory.load()
	val db = new Db(conf)
	val client = HttpClients.createDefault

}