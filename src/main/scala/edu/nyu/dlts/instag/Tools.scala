package edu.nyu.dlts.instag

object initDB{
  def main(args: Array[String]){
    val session = new Session
    session.db.createTables
  }
}


object findAccount{
  def main(args: Array[String]){
    val session = new Session
    println("enter name to search for: ")
  }
}
